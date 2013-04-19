/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.common.ClassnameToClassTransformer;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.JaxbConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    // Our log
    private static final Logger log = LoggerFactory.getLogger(JaxbUtils.class.getName());

    /**
     * The namespace prefix key for external JAXB distribution.
     */
    private static final String EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY = "com.sun.xml.bind.namespacePrefixMapper";
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
     * @param transporter   The EntityTransporter which should be marshalled to XML.
     * @param isMarshalling {@code true} if the supplied JAXBContext is going to be used for marshalling,
     *                      as opposed to unmarshalling.
     * @return A JAXBContext able to marshal the provided transporter.
     * @throws NullPointerException if the transporter argument was {@code null}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JAXBContext getJaxbContext(final EntityTransporter transporter,
                                             final boolean isMarshalling)
            throws NullPointerException {

        // Check sanity
        Validate.notNull(transporter, "Cannot handle null transporter argument.");

        // Does a cached JAXBContext exist?
        final Tuple<SortedClassNameSetKey, JAXBContext> cachedContext
                = getCachedJaxbContext(transporter.getClassInformation());
        if (cachedContext.getValue() != null) {
            return cachedContext.getValue();
        }

        // Load all relevant classes
        Set<Class<?>> loadedClasses = new HashSet<Class<?>>();
        for (Object current : transporter.getClassInformation()) {

            final Class<?> classType = THREADLOCAL_TRANSFORMER.transform((String) current);
            addNonTransientInternalClass(loadedClasses, classType);
        }

        // If we are marshalling, acquire the non-transient internal
        // field types for the
        if (isMarshalling) {

            // The items List should be fully populated here.
            for (Object currentItem : transporter.getItems()) {
                addNonTransientInternalFieldTypes(loadedClasses, currentItem);
            }
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

            if (log.isDebugEnabled()) {
                log.info("Generated schema [" + (i + 1) + "/" + schemaSources.length + "]:\n "
                        + new String(tmp.toByteArray()));
            }

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
                                                final JaxbConverterRegistry registry,
                                                final List<Object> resultingTransportableObjects,
                                                final SortedSet<String> resultingTransportTypes) {

        // Check sanity
        Validate.notNull(registry, "Cannot handle null TransportTypeConverterRegistry argument.");
        Validate.notNull(resultingTransportableObjects, "Cannot handle null resultingTransportableObjects argument.");
        Validate.notNull(resultingTransportTypes, "Cannot handle null resultingTransportTypes argument.");

        // ### 1) Convert the current type if required.
        Object added = toWrapAndPackageForTransport;
        final Class<?> transportType = toWrapAndPackageForTransport == null
                ? JaxbAnnotatedNull.class
                : registry.getTransportType(toWrapAndPackageForTransport.getClass());

        final boolean wrapInTransportType = added == null || added.getClass() != transportType;
        if (wrapInTransportType) {
            added = registry.packageForTransport(toWrapAndPackageForTransport);
        }

        // ### 2) Add the item itself.
        resultingTransportableObjects.add(added);

        // ### 3) Add the JAXB-annotated transport classes found within
        //        the added object, via the use of its TypeExtractor methods.
        if (added instanceof ClassInformationHolder) {
            for (String current : ((ClassInformationHolder) added).getClassInformation()) {

                if (!resultingTransportTypes.contains(current)) {
                    resultingTransportTypes.add(current);
                }
            }
        }

        // ### 4) Add the class of the added object itself. Handle Array types.
        final Class<?> addedClass = added.getClass();
        final boolean addedClassIsArray = addedClass.isArray();
        final String addedClassName = addedClassIsArray
                ? addedClass.getComponentType().getName()
                : addedClass.getName();
        if (!resultingTransportTypes.contains(addedClassName)
                && !(addedClassIsArray && addedClass.getComponentType().isPrimitive())) {
            resultingTransportTypes.add(addedClassName);
        }

        // ### 5) Only add internal structure if the current class is not a AbstractJaxbAnnotatedTransportType.
        //        Acquire internal reflected state as well.
        if (!wrapInTransportType) {

            final Set<Class<?>> internalReflectedTypes = new HashSet<Class<?>>();
            final Set<Class<?>> transportTypes = new HashSet<Class<?>>();

            addNonTransientInternalFieldTypes(internalReflectedTypes, toWrapAndPackageForTransport);
            for (Class<?> current : internalReflectedTypes) {

                // Don't add a null transport type.
                final Class<?> transportTypeOrNull = registry.getTransportType(current);
                final Class<?> toAdd = transportTypeOrNull == null ? current : transportTypeOrNull;
                addNonTransientInternalClass(transportTypes, toAdd);
            }

            // Fire the internally found reflected types through the JaxbConverter
            for (Class<?> current : transportTypes) {
                final String fullyQualifiedClassName = current.getName();
                if (!resultingTransportTypes.contains(fullyQualifiedClassName)) {
                    resultingTransportTypes.add(fullyQualifiedClassName);
                }
            }
        }
    }

    //
    // Private helpers
    //

    /**
     * Adds all field types found in the {@code toReflect} object to the provided types Set.
     * Should any Field in the {@code toReflect} instance be a Collection, Array or Map, any
     * classes found within the collections are added as well.
     *
     * @param types     The resulting types set.
     * @param toReflect The object from whose internal Fields the type information should be extracted.
     */
    @SuppressWarnings("rawtypes")
    private static void addNonTransientInternalFieldTypes(final Set<Class<?>> types, final Object toReflect) {

        // Check sanity
        if (toReflect == null) {
            return;
        }

        // First, add the current Class to the types Set
        final Class<?> theType = toReflect.getClass();
        addNonTransientInternalClass(types, theType);

        // Don't reflect EntityTransporter classes
        if (theType == EntityTransporter.class) {
            return;
        }

        for (Field current : XmlMarshallableFieldFilter.getMarshallableFields(toReflect)) {

            // Get the type of the object stored within the current field
            Object currentValue = get(current, toReflect);
            if (currentValue != null) {

                // Add the class of the currentValue
                final Class<?> currentType = currentValue.getClass();
                addNonTransientInternalClass(types, currentType);

                // Handle Collections, Arrays and Maps, which may contain
                // implementation types which should be included.

                if (currentType.getClass().isArray()) {
                    addNonTransientInternalClass(types, currentType.getComponentType());
                } else if (Collection.class.isAssignableFrom(currentType)) {

                    // This is a collection. Dig out the types of all Elements.
                    for (Object currentElement : Collection.class.cast(get(current, toReflect))) {
                        addNonTransientInternalFieldTypes(types, currentElement);
                    }
                } else if (Map.class.isAssignableFrom(currentType)) {

                    // This is a map. Dig out the types of all Keys and Value.
                    final Map theMap = Map.class.cast(get(current, toReflect));
                    for (Object currentKey : theMap.keySet()) {

                        addNonTransientInternalClass(types, currentKey.getClass());

                        final Object currentMapValue = theMap.get(currentKey);
                        if (currentMapValue != null) {
                            addNonTransientInternalClass(types, currentMapValue.getClass());
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the value of the supplied Field from the given instance.
     *
     * @param aField   The Field whose value should be retrieved.
     * @param anObject The instance whose class contains the supplied Field, and
     *                 from which the value should be retrieved.
     * @return the value of the supplied Field from the given instance.
     */
    private static Object get(final Field aField, final Object anObject) {

        aField.setAccessible(true);
        try {
            return aField.get(anObject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds the given candidate Class to the supplied set of types, given that the candidate
     * passes some trivial checks.
     * <p/>
     * TODO: Convert from source types to JAXB annotated transport types here?
     *
     * @param types     The types set to which the candidate could be added.
     * @param candidate The type to [possibly] add to the given types Set.
     */
    private static void addNonTransientInternalClass(final Set<Class<?>> types, final Class<?> candidate) {

        if (candidate == null) {
            return;
        }

        // Don't add XmlTransient classes
        final boolean isXmlTransient = candidate.getAnnotation(XmlTransient.class) != null;

        // Ignore interfaces
        final boolean isInterface = candidate.isInterface();

        // Ignore primitives and Object
        final boolean isPrimitive = candidate.isPrimitive();
        final boolean isObject = candidate == Object.class;

        // Ignore array types
        final boolean isArray = candidate.isArray();

        if (!isXmlTransient && !isInterface && !isPrimitive && !isObject && !isArray) {
            types.add(candidate);
        }
    }

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
