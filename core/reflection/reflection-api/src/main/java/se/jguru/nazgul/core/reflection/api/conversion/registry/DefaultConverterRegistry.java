/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the ConverterRegistry specification.
 * Not intended for clustered operation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    // Internal state
    // From (Class) --> Integer (priority) --> Method/Constructor (Object)
    // Priority --> From --> To --> Method/Constructor [unique within class] (Object)
    private Map<Class, PrioritizedTypeConverter> priority2converterMap;

    /**
     * Default constructor, yielding an empty internal state - i.e. no default
     * converters added.
     */
    public DefaultConverterRegistry() {

        // Create internal state
        priority2converterMap = new HashMap<Class, PrioritizedTypeConverter>();
    }

    /**
     * Adds the provided Converters to this ConverterRegistry.
     *
     * @param converters A List of objects, annotated with @Converter. The priority provided in the annotation
     *                   of each supplied converter will be used when setting up the internal state and the
     *                   converter chain.
     * @throws IllegalArgumentException if any of the converters was not annotated with @Converter, or if the
     *                                  methods/constructors annotated with @Converter did not comply with a converter
     *                                  specification.
     * @see se.jguru.nazgul.core.reflection.api.conversion.Converter
     */
    @Override
    public void add(Object... converters) throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(converters, "Cannot handle null or empty converters argument.");
        final Map<Object, Tuple<List<Method>, List<Constructor<?>>>> validConverters =
                new HashMap<Object, Tuple<List<Method>, List<Constructor<?>>>>();

        // All converters have annotations?
        for(Object current : converters) {

            // Find any converter methods in the supplied converter
            final List<Method> methods = TypeExtractor.getMethods(
                    current.getClass(),
                    ReflectiveConverterFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(current.getClass().getConstructors()),
                    ReflectiveConverterFilter.CONVERTION_CONSTRUCTOR_FILTER);

            if(methods.size() == 0 && constructors.size() == 0) {

                // No converters methods or constructors found within the supplied converter. Complain.
                throw new IllegalArgumentException("Found no @Converter-annotated methods within class ["
                        + current.getClass().getName() + "].");
            }

            // Map the current converter to its converter methods and constructors, respectively.
            validConverters.put()
        }

        // All should be well. Insert the appropriate relations into the priority2converterMap.
        for(Object current : validConverters) {
        }
    }

    /**
     * Removes the supplied converter instance from this ConverterRegistry.
     *
     * @param converter The converter to remove.
     */
    @Override
    public void remove(Object converter) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From, To, C extends To> C convert(From source, Class<To> desiredType) throws IllegalArgumentException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From, To> Class<To> getResultingType(Class<From> sourceType, Comparator<Class<?>> sortingCriterion) throws IllegalArgumentException {
        return null;
    }

    //
    // Private helpers
    //

}
