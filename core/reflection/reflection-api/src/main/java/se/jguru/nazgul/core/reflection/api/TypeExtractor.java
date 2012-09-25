/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
import java.util.List;

/**
 * Type filtering utility class.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class TypeExtractor {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(TypeExtractor.class);

    /**
     * Acquires all interfaces from the provided class which matches
     * the provided selector's acceptance criteria.
     *
     * @param clazz    The class from which to derive all appropriate interfaces.
     * @param selector The selector defining which interfaces to filter out.
     * @return All interfaces implemented by the provided class, and which
     *         matched the supplied selector's acceptance criteria.
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
     *         which matched the supplied selector's acceptance criteria.
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
     *         or its superclasses, and which matched the supplied selector's acceptance criteria.
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
}
