/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import se.jguru.nazgul.core.algorithms.api.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.predicate.Tuple;
import se.jguru.nazgul.core.algorithms.api.predicate.common.ClassnameToClassTransformer;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JAXB utility methods to simplify complex JAXB-related tasks.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class JaxbUtils {

    /**
     * The namespace prefix key for external JAXB distribution.
     */
    private static String EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY = "com.sun.xml.bind.namespacePrefixMapper";
    private static final ClassnameToClassTransformer THREADLOCAL_TRANSFORMER = new ClassnameToClassTransformer();
    private static final SchemaFactory DEFAULT_SCHEMA_FACTORY =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    // Internal state
    private static ConcurrentMap<SortedClassNameSetKey, JAXBContext> jaxbContextCache =
            new ConcurrentHashMap<SortedClassNameSetKey, JAXBContext>();

    /**
     * Acquires a properly configured JAXB marshaller from the provided JAXBContext.
     * <p/>
     * <pre><code>
     *  // Acquire the JAXBContext for the types found within an EntityTransporter.
     *  final NamespacePrefixMapper mapper = ...
     *  final JAXBContext ctx = JaxbUtils.getJaxbContext(anEntityTransporter);
     * <p/>
     *  // Now acquire the marshaller
     *  final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(ctx, resolver);
     * <p/>
     *  // ... and use it ...
     *  marshaller.marshal(someInstance, someOutputTarget);
     * </code></pre>
     *
     * @param ctx                   The properly set up JAXBContext, holding all required class definitions.
     * @param namespacePrefixMapper A JAXB NamespacePrefixMapper to be used by the provided marshaller.
     *                              Cannot be {@code null}.
     * @param resourceResolver      The LSResourceResolver used to resolve XSD resources in the generated Schema.
     * @param validate              if {@code true}, performs validation - implying that the resourceResolver
     *                              must be non-null.
     * @return A standard JAXB Marshaller for human-readable (as opposed to extreme compactness) XML marshalling.
     * @throws NullPointerException if the {@code namespacePrefixMapper} was {@code null}.
     */
    public static Marshaller getHumanReadableStandardMarshaller(final JAXBContext ctx,
                                                                final NamespacePrefixMapper namespacePrefixMapper,
                                                                final LSResourceResolver resourceResolver,
                                                                final boolean validate)
            throws NullPointerException {

        Validate.notNull(namespacePrefixMapper, "Cannot handle null namespacePrefixMapper argument.");

        try {

            // Acquire the properly configured Marshaller.
            final Marshaller toReturn = ctx.createMarshaller();
            toReturn.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            toReturn.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            toReturn.setProperty(EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY, namespacePrefixMapper);

            // Should we validate what we write?
            if (validate) {
                Validate.notNull(resourceResolver, "Cannot handle null resourceResolver argument if validating.");
                toReturn.setSchema(generateTransientXSD(ctx, resourceResolver));
            }

            return toReturn;
        } catch (final JAXBException e) {
            throw new IllegalStateException("Could not create marshaller", e);
        }
    }

    /**
     * Retrieves a JAXBContext instance, geared to converting all class types found within the
     * provided transporter.
     *
     * @param transporter The EntityTransporter which should be marshalled to XML.
     * @return A JAXBContext able to marshal the provided transporter.
     * @throws NullPointerException if the transporter argument was {@code null}.
     */
    public static JAXBContext getJaxbContext(final EntityTransporter transporter) throws NullPointerException {

        // Check sanity
        Validate.notNull(transporter, "Cannot handle null transporter argument.");

        // Does a cached JAXBContext exist?
        final Tuple<SortedClassNameSetKey, JAXBContext> cachedContext = getCachedJaxbContext(transporter.getClassInformation());
        if (cachedContext.getValue() != null) {
            return cachedContext.getValue();
        }

        // Load all relevant classes
        transporter.getClassInformation().add(EntityTransporter.class.getName());
        List<Class> loadedClasses = (List<Class>) CollectionAlgorithms.transform(
                transporter.getClassInformation(), THREADLOCAL_TRANSFORMER);

        try {

            // Create the JAXBContext and cache it.
            JAXBContext toReturn = JAXBContext.newInstance(loadedClasses.toArray(new Class[loadedClasses.size()]));
            jaxbContextCache.put(cachedContext.getKey(), toReturn);

            return toReturn;
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not create JAXBContext", e);
        }
    }

    /**
     * Acquires a JAXB Schema from the provided JAXBContext.
     *
     * @param ctx              The context for which am XSD should be constructed.
     * @param resourceResolver The LSResourceResolver used to resolve XSD resources in the generated Schema.
     * @return The constructed XSD from the provided JAXBContext.
     * @throws NullPointerException     if ctx was {@code null}.
     * @throws IllegalArgumentException if a JAXB-related exception occurred while extracting the schema.
     */
    public static Schema generateTransientXSD(final JAXBContext ctx, final LSResourceResolver resourceResolver)
            throws NullPointerException, IllegalArgumentException {

        // Check sanity
        Validate.notNull(ctx, "Cannot handle null ctx argument.");
        Validate.notNull(resourceResolver, "Cannot handle null resourceResolver argument.");

        final List<ByteArrayOutputStream> schemaSnippets = new ArrayList<ByteArrayOutputStream>();

        try {
            ctx.generateSchema(new SchemaOutputResolver() {
                @Override
                public Result createOutput(final String namespaceUri, final String suggestedFileName)
                        throws IOException {

                    // Create the result ByteArrayOutputStream
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    schemaSnippets.add(out);

                    // Target the result to the generated ByteArrayOutputStream.
                    StreamResult streamResult = new StreamResult(out);
                    streamResult.setSystemId("");

                    // All done.
                    return streamResult;
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire Schema snippets.", e);
        }

        // Convert to an array of Source.
        StreamSource[] schemaSources = new StreamSource[schemaSnippets.size()];
        for (int i = 0; i < schemaSources.length; i++) {
            ByteArrayOutputStream tmp = schemaSnippets.get(i);
            // Schema printout: System.out.append("Generated schema: " + new String(tmp.toByteArray()));
            schemaSources[i] = new StreamSource(new ByteArrayInputStream(tmp.toByteArray()), "");
        }

        try {

            // All done.
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(resourceResolver);
            final Schema schema = DEFAULT_SCHEMA_FACTORY.newSchema(schemaSources);
            final Validator validator = schema.newValidator();
            validator.setResourceResolver(resourceResolver);

            return schema;

        } catch (final SAXException e) {
            throw new IllegalArgumentException("Could not create Schema from snippets.", e);
        }
    }

    //
    // Private helpers
    //

    /**
     * Creating new JAXBContexts is an expensive operation; caching a few pre-created ones and re-using
     * them speeds up the process of marshalling and unmarshalling considerably.
     *
     * @param classInformation The classes from an EntityTransporter for which a JAXBContext should be acquired.
     * @return A Tuple holding the best-match cache key for the EntityTransporter, and the corresponding
     *         cached JAXBContext. The JAXBContext value might be {@code null}, indicating that a
     *         cached JAXBContext was not found.
     */
    private static Tuple<SortedClassNameSetKey, JAXBContext> getCachedJaxbContext(final List<String> classInformation) {

        // Create a sorted clone.
        final SortedSet<String> copy = new TreeSet<String>(classInformation);
        copy.add(EntityTransporter.class.getName());
        final SortedClassNameSetKey key = new SortedClassNameSetKey(copy);

        JAXBContext jaxbContext = jaxbContextCache.get(key);
        if (jaxbContext != null) {

            // Exact existing match. All done.
            return new Tuple<SortedClassNameSetKey, JAXBContext>(key, jaxbContext);
        }

        // Search for matching/compatible SortedClassNameSetKeys.
        // The first matching one can be used for JAXBContext.
        Tuple<SortedClassNameSetKey, JAXBContext> toReturn = null;
        for (SortedClassNameSetKey current : jaxbContextCache.keySet()) {
            if (current.containsAll(copy)) {
                toReturn = new Tuple<SortedClassNameSetKey, JAXBContext>(current, jaxbContextCache.get(current));
            }
        }

        if (toReturn == null) {
            // No cache entry found. Create one.
            toReturn = new Tuple<SortedClassNameSetKey, JAXBContext>(key, null);
        }

        // All done.
        return toReturn;
    }
}
