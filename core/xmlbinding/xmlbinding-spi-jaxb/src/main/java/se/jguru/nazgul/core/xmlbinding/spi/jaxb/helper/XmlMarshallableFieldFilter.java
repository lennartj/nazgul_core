/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;

import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter implementation which defines which fields should be accepted as
 * XML types for marshalling (i.e. whose types should be included in
 * JAXBContext initializations). All introspection must be dynamic, to
 * evaluate the runtime implementation classes. For example, the situation
 * <code>
 * // Internal state
 * private ASuperClass value = new ASubClass(...);
 * </code>
 * implies that the {@code ASubClass}, rather than the {@code ASuperClass}
 * type should be inspected for marshall-ability via JAXB/XML.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class XmlMarshallableFieldFilter implements Filter<Field> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(XmlMarshallableFieldFilter.class);

    // Internal state
    private Map<Class<?>, List<Field>> type2XmlMarshallableFieldMap = new ConcurrentHashMap<Class<?>, List<Field>>();
    private static final XmlMarshallableFieldFilter INSTANCE = new XmlMarshallableFieldFilter();

    /**
     * Hide the utility constructor.
     */
    private XmlMarshallableFieldFilter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Field candidate) {

        final Class<?> candidateType = candidate.getType();

        // Transient or XmlTransient fields are not marshallable via JAXB.
        final boolean isTransient = Modifier.isTransient(candidate.getModifiers());
        final boolean isXmlTransient = candidate.isAnnotationPresent(XmlTransient.class);

        // Primitives and Strings are handled by JAXB natively, so
        // need not be investigated dynamically.
        final boolean isPrimitiveOrString = candidateType.isPrimitive() || String.class.isAssignableFrom(candidateType);

        // Combine into a single condition
        final boolean shouldReject = isTransient || isXmlTransient || isPrimitiveOrString;

        if (log.isDebugEnabled()) {
            log.debug("Field [" + candidate + "] - isTransient [" + isTransient + "], isXmlTransient ["
                    + isXmlTransient + "], isPrimitiveOrString [" + isPrimitiveOrString
                    + "] ==> shouldReject [" + shouldReject + "]");
        }

        // All done.
        return !shouldReject;
    }

    /**
     * Retrieves a List holding all Fields within the class of the supplied object (including all superclasses,
     * except java.lang.Object) which should be queried for dynamic class names in order to compile
     * a complete Set of classes to be used within a JAXBContext.
     *
     * @param object The object on whose class extraction of Fields should be done.
     * @return a List holding all Fields within the class of the supplied object which should
     * be included in XmlMarshalling.
     */
    public static List<Field> getMarshallableFields(final Object object) {

        // Check sanity
        Validate.notNull(object, "Cannot handle null object argument.");

        final Class<?> type = object.getClass();
        INSTANCE.mapClass(type);

        final List<Field> toReturn = new ArrayList<Field>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {

            // Ensure that we do not create a NPE here.
            final List<Field> fieldList = INSTANCE.type2XmlMarshallableFieldMap.get(current);
            if (fieldList != null) {
                toReturn.addAll(fieldList);
            }
        }

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private void mapClass(final Class<?> aType) {

        // Don't map java.lang.Object.
        for (Class<?> current = aType; current != Object.class; current = current.getSuperclass()) {
            if (type2XmlMarshallableFieldMap.get(current) != null) {
                return;
            }

            // Is aType itself annotated with @XmlTransient?
            final List<Field> fieldsInType = aType.isAnnotationPresent(XmlTransient.class)
                    ? Arrays.<Field>asList()
                    : TypeExtractor.getFields(current, INSTANCE);

            // Map the fields of the supplied Class.
            type2XmlMarshallableFieldMap.put(current, fieldsInType);
        }
    }
}
