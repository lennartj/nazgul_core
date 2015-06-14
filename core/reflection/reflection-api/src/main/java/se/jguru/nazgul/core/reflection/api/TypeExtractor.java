/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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
package se.jguru.nazgul.core.reflection.api;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type filtering utility class.
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
    }

    /**
     * Acquires all interfaces from the provided class which matches
     * the provided selector's acceptance criteria.
     *
     * @param clazz    The class from which to derive all appropriate interfaces.
     * @param selector The selector defining which interfaces to filter out.
     * @return All interfaces implemented by the provided class, and which
     * matched the supplied selector's acceptance criteria.
     */
    public static List<Class<?>> getInterfaces(final Class<?> clazz, final Filter<Class<?>> selector) {

        // Check sanity
        Validate.notNull(clazz, "Cannot handle null clazz argument.");

        // Extract all interfaces
        @SuppressWarnings("unchecked")
        final List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(clazz);

        // Delegate
        return CollectionAlgorithms.filter(allInterfaces, selector);
    }

    /**
     * Acquires all methods from the provided class which matches the provided selector.
     *
     * @param clazz    The class from which to retrieve all relevant methods.
     * @param selector The selector defining which methods to filter out.
     * @return All methods (including private ones) found by the provided class, and
     * which matched the supplied selector's acceptance criteria.
     */
    public static List<Method> getMethods(final Class<?> clazz, final Filter<Method> selector) {

        // Check sanity
        Validate.notNull(clazz, "Cannot handle null instance argument.");

        // Acquire all methods found within the class of the provided instance.
        final List<Method> methods = new ArrayList<Method>();
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

        // Add Method definitions from all implemented interfaces
        @SuppressWarnings("unchecked")
        final List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(clazz);

        for (Class<?> current : allInterfaces) {
            for (Method currentMethod : Arrays.asList(current.getDeclaredMethods())) {
                if (!methods.contains(currentMethod)) {

                    // Method not already present. Re-add.
                    methods.add(currentMethod);

                } else {

                    // Don't add a method twice.
                    log.debug("Avoiding re-adding [" + currentMethod + "]");
                }
            }
        }

        // Delegate
        return CollectionAlgorithms.filter(methods, selector);
    }

    /**
     * Acquires all fields from the given class which matches the provided selector.
     *
     * @param clazz    The class from which to retrieve all relevant fields.
     * @param selector The selector defining which fields to filter out.
     * @return All fields (including private ones) within the provided instance
     * or its superclasses, and which matched the supplied selector's acceptance criteria.
     */
    public static List<Field> getFields(final Class<?> clazz, final Filter<Field> selector) {

        // Check sanity
        Validate.notNull(clazz, "Cannot handle null class.");

        // Acquire all fields found within the class hierarcy of the provided instance.
        final List<Field> fields = new ArrayList<Field>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        // Add Fields from all superclasses
        @SuppressWarnings("unchecked")
        final List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(clazz);

        for (Class<?> current : allInterfaces) {
            for (Field currentField : Arrays.asList(current.getDeclaredFields())) {
                fields.add(currentField);
            }
        }

        // Delegate
        return CollectionAlgorithms.filter(fields, selector);
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
