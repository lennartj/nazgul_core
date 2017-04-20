/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.reflection.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.TypeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods to retrieve, filter and handle types.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class TypeExtractor {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(TypeExtractor.class);

    /**
     * Hidden utility class constructor.
     */
    private TypeExtractor() {
        // Do nothing
    }

    /**
     * Acquires all interfaces from the provided class which matches the provided selector's acceptance criteria.
     *
     * @param clazz    The class from which to derive all appropriate interfaces.
     * @param selector an optional (i.e. nullable) Predicate. If provided, the Predicate is used to
     *                 filter all interfaces implemented by the supplied Class.
     * @return All interfaces implemented by the provided class, and which
     * matched the supplied selector's acceptance criteria.
     */
    @NotNull
    public static Set<Class<?>> getInterfaces(@NotNull final Class<?> clazz,
                                              final Predicate<Class<?>> selector) {

        // Check sanity
        Validate.notNull(clazz, "clazz");

        // Extract all interfaces
        final SortedSet<Class<?>> allInterfaces = TypeAlgorithms.getAllTypesFor(clazz).getAllInterfaces();

        // Delegate
        return selector == null
                ? allInterfaces
                : allInterfaces.stream().filter(selector).collect(Collectors.toSet());
    }

    /**
     * Acquires all methods from the provided class which matches the provided selector.
     *
     * @param clazz    The class from which to retrieve all relevant methods.
     * @param selector an optional (i.e. nullable) Predicate. If provided, the Predicate is used to
     *                 filter all Methods found within the supplied Class.
     * @return All methods (including private ones) found by the provided class, and
     * which matched the supplied selector's acceptance criteria.
     */
    public static SortedSet<Method> getMethods(@NotNull final Class<?> clazz,
                                               final Predicate<Method> selector) {

        // Check sanity
        Validate.notNull(clazz, "clazz");

        // Acquire all methods found within the class of the provided instance.
        final SortedSet<Method> declaredMethods = TypeAlgorithms.getAllTypesFor(clazz).getMethods(true);

        // All Done.
        if(selector == null) {
            return declaredMethods;
        }
        
        return declaredMethods
                .stream()
                .filter(selector)
                .collect(Collectors.toCollection(TypeAlgorithms.SORTED_METHOD_SUPPLIER));
    }

    /**
     * Acquires all fields from the given class which matches the provided selector.
     *
     * @param clazz    The class from which to retrieve all relevant fields.
     * @param selector The selector defining which fields to filter out.
     * @return All fields (including private ones) within the provided instance
     * or its superclasses, and which matched the supplied selector's acceptance criteria.
     */
    public static SortedSet<Field> getFields(@NotNull final Class<?> clazz,
                                             final Predicate<Field> selector) {

        // Check sanity
        Validate.notNull(clazz, "clazz");

        // Acquire all methods found within the class of the provided instance.
        final SortedSet<Field> declaredFields = TypeAlgorithms.getAllTypesFor(clazz).getFields(true);

        // All Done.
        if(selector == null) {
            return declaredFields;
        }

        return declaredFields
                .stream()
                .filter(selector)
                .collect(Collectors.toCollection(() -> new TreeSet<>(TypeAlgorithms.MEMBER_COMPARATOR)));
    }

    /**
     * Finds the number of types between related classes source and target.
     *
     * @param source The source type.
     * @param target The target type.
     * @return The number of class hops (i.e. class/superclass relations) between the
     * source and target types; if source is a superclass of target, the value
     * is negative, and otherwise positive.
     * @throws IllegalArgumentException if source and target classes are not related.
     */
    public static int getRelationDifference(final Class<?> source, final Class<?> target)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(source, "Cannot handle null source argument.");
        Validate.notNull(target, "Cannot handle null target argument.");

        if (source.isAssignableFrom(target)) {

            int depth = 0;

            for (Class<?> current = target; current != null; current = current.getSuperclass()) {

                // Same type?
                if (current == source) {
                    break;
                }

                // Interface implemented by the current class?
                // Does the current class implement the desiredType, or *is* it the desiredType?
                List<Class<?>> interfacesImplemented = new ArrayList<Class<?>>();
                Collections.addAll(interfacesImplemented, current.getInterfaces());

                // Done?
                if (interfacesImplemented.contains(source)) {
                    depth--;
                    break;
                }

                // Decrease one.
                depth -= 2;
            }

            // All done.
            return depth;
        }

        if (target.isAssignableFrom(source)) {

            int depth = 0;

            for (Class<?> current = source; current != null; current = current.getSuperclass()) {

                // Same type?
                if (current == target) {
                    break;
                }

                // Interface implemented by the current class?
                // Does the current class implement the desiredType, or *is* it the desiredType?
                List<Class<?>> interfacesImplemented = new ArrayList<Class<?>>();
                Collections.addAll(interfacesImplemented, current.getInterfaces());

                // Done?
                if (interfacesImplemented.contains(target)) {
                    depth++;
                    break;
                }

                // Decrease one.
                depth += 2;
            }

            // All done.
            return depth;

        }

        // Complain.
        throw new IllegalArgumentException("Types [" + source.getSimpleName() + "] and [" + target.getSimpleName()
                + "] are not related.");
    }
}
