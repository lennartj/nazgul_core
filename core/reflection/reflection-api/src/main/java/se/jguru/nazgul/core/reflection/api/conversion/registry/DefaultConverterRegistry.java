/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Default implementation of the ConverterRegistry specification.
 * Not intended for clustered operation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultConverterRegistry implements ConverterRegistry {

    // Internal state
    private Map<Class<?>, PrioritizedTypeConverter> sourceTypeToTypeConvertersMap;

    /**
     * Default constructor, yielding an empty internal state - i.e. no default
     * converters added.
     */
    public DefaultConverterRegistry() {

        // Create internal state
        sourceTypeToTypeConvertersMap = new HashMap<Class<?>, PrioritizedTypeConverter>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Object... converters) throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(converters, "Cannot handle null or empty converters argument.");
        final Map<Object, Tuple<List<Method>, List<Constructor<?>>>> validConverters =
                new HashMap<Object, Tuple<List<Method>, List<Constructor<?>>>>();

        // All converters have annotations?
        for (Object current : converters) {

            final Tuple<List<Method>, List<Constructor<?>>> methodsAndConstructors =
                    ReflectiveConverterFilter.getConverterMethodsAndConstructors(current);

            if (methodsAndConstructors == null) {

                // No converters methods or constructors found within the supplied converter. Complain.
                throw new IllegalArgumentException("Found no @Converter-annotated methods within class ["
                        + current.getClass().getName() + "]. No @Converters added.");
            }

            // Map the current converter to its converter methods and constructors, respectively.
            validConverters.put(current, methodsAndConstructors);
        }

        // Map the respective converters to the sourceTypes of their @Converters.
        final Map<Class<?>, Set<Object>> sourceTypeToConverterInstanceMap = new HashMap<Class<?>, Set<Object>>();
        for (Object current : validConverters.keySet()) {

            // Map the source types of all Methods & Constructors
            for (Method currentMethod : validConverters.get(current).getKey()) {

                final Class<?> currentType = currentMethod.getParameterTypes()[0];
                addCurrentConverter(sourceTypeToConverterInstanceMap, current, currentType);
            }

            for (Constructor<?> currentConstructor : validConverters.get(current).getValue()) {

                final Class<?> currentType = currentConstructor.getParameterTypes()[0];
                addCurrentConverter(sourceTypeToConverterInstanceMap, current, currentType);
            }
        }

        // Create PrioritizedTypeConverters for all source types.
        for (Class<?> current : sourceTypeToConverterInstanceMap.keySet()) {

            PrioritizedTypeConverter prioritizedTypeConverter = new PrioritizedTypeConverter(
                    current,
                    sourceTypeToConverterInstanceMap.get(current).toArray());

            // Finally, add the PrioritizedTypeConverter instance.
            sourceTypeToTypeConvertersMap.put(current, prioritizedTypeConverter);
        }
    }

    /**
     * Removes the supplied converter instance from this ConverterRegistry.
     *
     * @param converter The converter to remove.
     */
    @Override
    public void remove(final Object converter) {
        throw new UnsupportedOperationException("Converter removal not yet implemented.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From, To> To convert(final From source, final Class<To> desiredType)
            throws IllegalArgumentException {

        // Acquire the TypeConverter instance
        PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(source.getClass());
        if (typeConverter == null) {

            // Attempt fuzzy logic matching.
            for (Class<?> current : sourceTypeToTypeConvertersMap.keySet()) {
                if (current.isAssignableFrom(source.getClass())) {
                    typeConverter = sourceTypeToTypeConvertersMap.get(current);
                    break;
                }
            }
        }

        // Convert if possible.
        return typeConverter == null ? null : typeConverter.convert(source, desiredType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From> Set<Class<?>> getPossibleConversions(final Class<From> sourceType) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(sourceType, "Cannot handle null sourceType argument.");

        // Acquire the TypeConverter instance
        final List<PrioritizedTypeConverter> converters = new ArrayList<PrioritizedTypeConverter>();
        PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(sourceType);
        if (typeConverter == null) {

            // No exact match found for the type desired.
            // Perform compound fuzzy logic matching to acquire all matching converters.
            for (Class<?> current : sourceTypeToTypeConvertersMap.keySet()) {
                if (current.isAssignableFrom(sourceType)) {
                    converters.add(sourceTypeToTypeConvertersMap.get(current));
                }
            }
        } else {

            // Exact match found for the type.
            // Ignore fuzzy logic type resolution.
            converters.add(typeConverter);
        }

        if (converters.size() == 0) {
            return new HashSet<Class<?>>();
        }

        // Delegate and return.
        final Set<Class<?>> toReturn = new HashSet<Class<?>>();
        for(PrioritizedTypeConverter current : converters) {

            // Acquire the available target types for the current PrioritizedTypeConverter
            final Set<Class<?>> availableTargetTypes = current.getAvailableTargetTypes();
            for(Class<?> currentTargetType : availableTargetTypes) {

                // Don't add twice.
                if(!toReturn.contains(currentTargetType)) {
                    toReturn.add(currentTargetType);
                }
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        // Create a preamble
        final StringBuilder builder = new StringBuilder("DefaultConverterRegistry ["
                + sourceTypeToTypeConvertersMap.size() + "] source types.\n");

        final SortedMap<String, Class<?>> sortedClassNames = new TreeMap<String, Class<?>>();
        for (Class<?> current : sourceTypeToTypeConvertersMap.keySet()) {
            sortedClassNames.put(current.getSimpleName(), current);
        }

        // Delegate detail printout to the PrioritizedTypeConverter.
        for (String current : sortedClassNames.keySet()) {
            builder.append(sourceTypeToTypeConvertersMap.get(sortedClassNames.get(current))).append("\n");
        }

        // All done.
        return builder.toString();
    }

    //
    // Private helpers
    //

    private void addCurrentConverter(final Map<Class<?>, Set<Object>> sourceTypeToConverterInstanceMap,
                                     final Object value,
                                     final Class<?> key) {

        // Get or create the current converter set
        Set<Object> currentConverterSet = sourceTypeToConverterInstanceMap.get(key);
        if (currentConverterSet == null) {
            currentConverterSet = new HashSet<Object>();
            sourceTypeToConverterInstanceMap.put(key, currentConverterSet);
        }

        // Add if the current value is not present within the currentConverterSet.
        if (!currentConverterSet.contains(value)) {
            currentConverterSet.add(value);
        }
    }

}
