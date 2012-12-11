/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.common.ClassnameToClassTransformer;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportMetaData;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverterRegistry;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Set<Class<?>> loadedClasses = new HashSet<Class<?>>();
        for (Object current : transporter.getClassInformation()) {
            loadedClasses.add(THREADLOCAL_TRANSFORMER.transform((String) current));
        }

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
            // log.info("Generated schema: " + new String(tmp.toByteArray()));
            schemaSources[i] = new StreamSource(new ByteArrayInputStream(tmp.toByteArray()), "");
        }

        try {

            // All done.
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(resourceResolver);
            return DEFAULT_SCHEMA_FACTORY.newSchema(schemaSources);

        } catch (final SAXException e) {
            throw new IllegalArgumentException("Could not create Schema from snippets.", e);
        }
    }

    /**
     * Extracts relevant data from the supplied {@code toWrapAndPackageForTransport} object,
     * appending the JAXB-transportable objects into the {@code resultingTransportableObjects} List,
     * and appending the transport type Class names into the {@code resultingTransportTypes} List.
     *
     * @param toWrapAndPackageForTransport  The instance from which transport data should be extracted
     *                                      and appended onto the resultingTransportableObjects and
     *                                      resultingTransportTypes Lists, respectively.
     * @param registry                      The TransportTypeConverterRegistry instance used to extract
     *                                      TransportTypeConverter instances, in turn used to translate
     *                                      non-JAXB-annotated types to JaxbAnnotatedTypes for transport.
     * @param resultingTransportableObjects The non-null List holding JAXB-convertible objects.
     * @param resultingTransportTypes       The non-null SortedSet holding the class names of all
     *                                      transport types.
     */
    public static void extractJaxbTransportData(final Object toWrapAndPackageForTransport,
                                                final TransportTypeConverterRegistry registry,
                                                final List<Object> resultingTransportableObjects,
                                                final SortedSet<String> resultingTransportTypes) {

        // Check sanity
        Validate.notNull(registry, "Cannot handle null TransportTypeConverterRegistry argument.");
        Validate.notNull(resultingTransportableObjects, "Cannot handle null resultingTransportableObjects argument.");
        Validate.notNull(resultingTransportTypes, "Cannot handle null resultingTransportTypes argument.");
        Object added = toWrapAndPackageForTransport;

        // ### 1) Convert the current type if required.
        TransportTypeConverter packagingConverter =
                registry.getPackagingTransportTypeConverter(toWrapAndPackageForTransport);
        if (packagingConverter != null) {

            added = packagingConverter.packageForTransport(toWrapAndPackageForTransport);
        }

        // ### 2) Add the item itself.
        ((List<Object>) resultingTransportableObjects).add(added);

        // ### 3) Add the JAXB-annotated transport classes found within
        //        the added object, via the use of its TypeExtractor methods.
        if (added instanceof ClassInformationHolder) {
            for (String current : ((ClassInformationHolder) added).getClassInformation()) {

                if (!resultingTransportTypes.contains(current)) {
                    resultingTransportTypes.add(current);
                }
            }
        }

        // ### 4) Add the class of the added object itself.
        String addedClass = added.getClass().getName();
        if (!resultingTransportTypes.contains(addedClass)) {
            resultingTransportTypes.add(addedClass);
        }
    }

    /**
     * Acquires the TransportType className for the supplied (non-null) originalType, by
     * asking the supplied TransportMetadata instance. If the TransportMetaData
     * instance does not know of any TransportType for the supplied originalType,
     * the Class name of the originalType is returned.
     *
     * @param originalType The non-transport type for which the class name of the corresponding
     *                     JAXB-annotated TransportType should be acquired.
     * @param metadata     The TransportMetaData instance which should be asked for data type conversion.
     * @return the TransportType className for the supplied (non-null) originalType, by
     *         asking the supplied TransportMetadata instance. If the TransportMetaData
     *         instance does not know of any TransportType for the supplied originalType,
     *         the Class name of the originalType is returned.
     */
    public static String getTransportClassName(final Class<?> originalType,
                                               final TransportMetaData metadata) {

        // Check sanity
        Validate.notNull(originalType, "Cannot handle null originalType argument.");
        Validate.notNull(metadata, "Cannot handle null metadata argument.");

        // Extract and assign converted types to the typeNames
        final Class<?> transportType = metadata.getTransportType(originalType);
        if (transportType == null) {

            // Transport type conversion not possible.
            return originalType.getName();
        }

        // Transport type conversion required.
        return transportType.getName();
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
    private static Tuple<SortedClassNameSetKey, JAXBContext> getCachedJaxbContext(
            final SortedSet<String> classInformation) {

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
