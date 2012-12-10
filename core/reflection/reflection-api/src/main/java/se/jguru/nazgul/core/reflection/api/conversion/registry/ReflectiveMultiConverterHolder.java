/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.registry.filters.ConversionMethodFilter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.filters.ConvertionConstructorFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ReflectiveMultiConverterHolder {

    // Internal state
    private Class<?> sourceType;
    private SortedMap<Integer, Map<Class<?>, Object>> prioritizedTargetTypeConverterMap;

    public ReflectiveMultiConverterHolder() {
        this(new TreeMap<Integer, Map<Class<?>, Object>>());
    }

    public ReflectiveMultiConverterHolder(
            final SortedMap<Integer, Map<Class<?>, Object>> prioritizedTargetTypeConverterMap) {

        // Check sanity
        Validate.notNull(prioritizedTargetTypeConverterMap,
                "Cannot handle null prioritizedTargetTypeConverterMap argument.");

        // Assign internal state
        this.prioritizedTargetTypeConverterMap = prioritizedTargetTypeConverterMap;
    }

    public void add(Object... converters) {

        // Check sanity
        Validate.notEmpty(converters, "Cannot handle null or empty converters argument.");

        // All converters have annotations?
        for (Object current : converters) {

            // Find any converter methods in the supplied converter
            final List<Method> methods = TypeExtractor.getMethods(
                    current.getClass(),
                    ConversionMethodFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(current.getClass().getConstructors()),
                    ConvertionConstructorFilter.CONVERTION_CONSTRUCTOR_FILTER);

            if (methods.size() == 0 && constructors.size() == 0) {

                // No converters methods or constructors found within the supplied converter. Complain.
                throw new IllegalArgumentException("Found no @Converter-annotated methods within class ["
                        + current.getClass().getName() + "].");
            }

            // Map the current converter to its converter methods and constructors, respectively.
            validConverters.put()
        }

        // All should be well. Insert the appropriate relations into the priority2converterMap.
        for (Object current : validConverters) {
        }
    }

    public Object getOptimumConverter(final Class<?> targetType) {
        return getConverterWithMinimumPriority(0, targetType);
    }

    public Object getConverterWithMinimumPriority(int minimumPriority, final Class<?> targetType) {

        // Check sanity
        Validate.isTrue(minimumPriority >= 0, "Cannot handle negative minimumPriority argument.");
        Validate.notNull(targetType, "Cannot handle null targetType argument.");

        for (Integer current : prioritizedTargetTypeConverterMap.keySet()) {

            if (current >= minimumPriority) {
                final Object converter = prioritizedTargetTypeConverterMap.get(current).get(targetType);
                if (converter != null) {
                    return converter;
                }
            }
        }

        // None found.
        return null;
    }
}
