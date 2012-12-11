/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        for(Class<?> current : sourceTypeToConverterInstanceMap.keySet()) {

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
        final PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(source.getClass());
        if(typeConverter == null) {
            return null;
        }

        // Delegate and convert.
        return typeConverter.convert(source, desiredType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From> Set<Class<?>> getPossibleConversions(final Class<From> sourceType) throws IllegalArgumentException {

        // Acquire the TypeConverter instance
        final PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(sourceType);
        if(typeConverter == null) {
            return new HashSet<Class<?>>();
        }

        // Delegate and return.
        return typeConverter.getAvailableTargetTypes();
    }
    //
    // Private helpers
    //

    private void addCurrentConverter(final Map<Class<?>, Set<Object>> sourceTypeToConverterInstanceMap,
                                     final Object value,
                                     final Class<?> key) {

        // Get or create the current converter set
        Set<Object> currentConverterSet = sourceTypeToConverterInstanceMap.get(key);
        if(currentConverterSet == null) {
            currentConverterSet = new HashSet<Object>();
            sourceTypeToConverterInstanceMap.put(key, currentConverterSet);
        }

        // Add if the current value is not present within the currentConverterSet.
        if(!currentConverterSet.contains(value)) {
            currentConverterSet.add(value);
        }
    }

}
