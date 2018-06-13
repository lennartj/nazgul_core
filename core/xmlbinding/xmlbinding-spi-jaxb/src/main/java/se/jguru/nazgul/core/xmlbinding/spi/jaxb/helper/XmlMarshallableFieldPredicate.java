/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.TypeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * <p>Predicate implementation which defines which fields should be accepted as XML types for marshalling
 * (i.e. whose types should be included in JAXBContext initializations).
 * All introspection must be dynamic, to evaluate the runtime implementation classes.
 * For example, the following statement:</p>
 * <pre>
 * <code>
 *
 * // Internal state
 * private ASuperClass value = new ASubClass(...);
 * </code>
 * </pre>
 * <p>... implies that the {@code ASubClass}, rather than the {@code ASuperClass}
 * type should be inspected for marshall-ability via JAXB/XML.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class XmlMarshallableFieldPredicate implements Predicate<Field> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(XmlMarshallableFieldPredicate.class);

    // Internal state
    private Map<Class<?>, SortedSet<Field>> type2XmlMarshallableFieldMap = new ConcurrentHashMap<>();
    private static final XmlMarshallableFieldPredicate INSTANCE = new XmlMarshallableFieldPredicate();

    /**
     * Hide the utility constructor.
     */
    private XmlMarshallableFieldPredicate() {
        // Do nothing
    }

    /**
     * Tests if the type of the supplied candidate Field should be included within a JAXBContext.
     * Candidate Fields should comply with the following to be
     */
    @Override
    @SuppressWarnings("all")
    public boolean test(final Field candidate) {

        // #0) Fail fast; handle nulls.
        if (candidate == null) {
            return false;
        }

        // #1) Transient or XmlTransient fields are not marshallable via JAXB.
        final Class<?> candidateType = candidate.getType();
        if (Modifier.isTransient(candidate.getModifiers())) {

            if (log.isTraceEnabled()) {
                log.trace("Field [" + candidate + "] is transient, and hence ignored.");
            }

            // All Done.
            return false;
        }

        // #2) XmlTransient fields are also ignored for marshalling.
        if (candidate.isAnnotationPresent(XmlTransient.class)) {

            if (log.isTraceEnabled()) {
                log.trace("Field [" + candidate + "] is annotated with @XmlTransient, and hence ignored.");
            }

            // All Done.
            return false;
        }

        // #3) Primitives and Strings are handled by JAXB natively, so need not be investigated dynamically.
        if (candidateType.isPrimitive() || String.class.isAssignableFrom(candidateType)) {

            if (log.isTraceEnabled()) {
                log.trace("Field [" + candidate + "] is a native JDK type, and need not be added specially.");
            }

            // All Done.
            return false;
        }

        // All done.
        return true;
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
    public static SortedSet<Field> getMarshallableFields(@NotNull final Object object) {

        // Check sanity
        Validate.notNull(object, "object");

        // Map the class of the submitted object.
        // ... or only map it if it was JAXB annotated?
        final Class<?> type = object.getClass();
        INSTANCE.mapClass(type);

        final SortedSet<Field> toReturn = new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR);
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {

            // Ensure that we do not create a NPE here.
            final SortedSet<Field> fieldList = INSTANCE.type2XmlMarshallableFieldMap.get(current);
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

            // Already Mapped?
            if (type2XmlMarshallableFieldMap.get(current) != null) {
                return;
            }

            // Is the current Class annotated with @XmlTransient?
            final SortedSet<Field> fieldsInType = aType.isAnnotationPresent(XmlTransient.class)
                    ? new TreeSet<>()
                    : TypeExtractor.getFields(current, INSTANCE);

            // Map the fields of the supplied Class.
            type2XmlMarshallableFieldMap.put(current, fieldsInType);
        }
    }
}
