/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(DefaultConverterRegistry.class);

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

        // Find the best PrioritizedTypeConverter instance for the supplied source.
        final Class<From> sourceType = (Class<From>) source.getClass();
        PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(sourceType);

        if (typeConverter == null) {

            // Resort to fuzzy logic to acquire the PrioritizedTypeConverter
            final SortedMap<Integer, PrioritizedTypeConverter> prioritized = getPrioritizedConverters(sourceType);
            typeConverter = prioritized.get(prioritized.firstKey());
        }

        // No TypeConverter found at all?
        if (typeConverter == null) {

            // Holler a tad...
            log.info("TypeConverter not found. [" + desiredType.getSimpleName() + " --> " + typeConverter + "]");
            return null;
        }

        // Dig out the corresponding optimalToType for the acquired TypeConverter.
        final Class<To> optimalToType = getOptimalToType(typeConverter, desiredType);

        log.debug("Found TypeConverter [" + desiredType.getSimpleName()
                + (optimalToType == desiredType ? "" : " (fuzzy: " + optimalToType.getSimpleName() + ")")
                + " --> " + typeConverter + "]");

        // Convert the inbound data, and return.
        return (To) typeConverter.convert(source, optimalToType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <From> Set<Class<?>> getPossibleConversions(final Class<From> sourceType) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(sourceType, "Cannot handle null sourceType argument.");

        // Acquire the TypeConverter instance
        final SortedMap<Integer, PrioritizedTypeConverter> converterMap = getPrioritizedConverters(sourceType);

        if (converterMap.size() == 0) {
            return new HashSet<Class<?>>();
        }

        // Delegate and return.
        final Set<Class<?>> toReturn = new HashSet<Class<?>>();
        for (Integer currentIndex : converterMap.keySet()) {

            final PrioritizedTypeConverter<From> current = converterMap.get(currentIndex);

            // Acquire the available target types for the current PrioritizedTypeConverter
            final Set<Class<?>> availableTargetTypes = current.getAvailableTargetTypes();
            for (Class<?> currentTargetType : availableTargetTypes) {

                // Don't add twice.
                if (!toReturn.contains(currentTargetType)) {
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

    private <From, To> Class<To> getOptimalToType(final PrioritizedTypeConverter<From> typeConverter,
                                                  final Class<To> requestedToType) {

        // Get the available To/target types for the supplied typeConverter.
        final Set<Class<?>> availableTargetTypes = typeConverter.getAvailableTargetTypes();

        // First - is the requestedToType available?
        if (availableTargetTypes.contains(requestedToType)) {

            // Debug somewhat.
            log.debug("Exact conversion [" + typeConverter.getSourceType().getSimpleName() + " --> "
                    + requestedToType.getSimpleName() + "] found in " + typeConverter);

            return requestedToType;
        }

        // Get the best available To/target type, related to the supplied requestedToType.
        SortedMap<Integer, Class<?>> prioritizedToTypes = new TreeMap<Integer, Class<?>>(CollectionAlgorithms.map(
                availableTargetTypes, new ClassPriorityTransformer(requestedToType)));

        final Class<To> optimalToClass = (Class<To>) prioritizedToTypes.get(prioritizedToTypes.firstKey());

        // Debug somewhat.
        log.debug("Fuzzy conversion [" + typeConverter.getSourceType().getSimpleName() + " --> "
                + optimalToClass.getSimpleName() + " (" + requestedToType.getSimpleName() + ")] found in "
                + typeConverter);

        // All done.
        return optimalToClass;
    }

    /**
     * Acquires a SortedMap holding all available PrioritizedTypeConverter instances
     * which can convert from the supplied sourceType.
     *
     * @param sourceType The type to convert from.
     * @param <From>     The type to convert from.
     * @return a SortedMap holding all available PrioritizedTypeConverter instances
     *         which can convert from the supplied sourceType, mapped to their priority
     *         as defined by the algorithm found in the TypeClosenessTransformer class.
     */
    private <From> SortedMap<Integer, PrioritizedTypeConverter> getPrioritizedConverters(
            final Class<From> sourceType) {

        // Create the List of converters to return
        final SortedMap<Integer, PrioritizedTypeConverter> toReturn =
                new TreeMap<Integer, PrioritizedTypeConverter>();

        // Exact match?
        PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(sourceType);
        if (typeConverter != null) {

            // Exact match found for the type.
            // Ignore fuzzy logic type resolution.
            toReturn.put(0, typeConverter);

        } else {

            // No exact match found for the type desired.
            // Perform compound fuzzy logic matching to acquire all matching converters.

            // Filter out all TypeConverters which can convert the supplied sourceType.
            final Map<Class<?>, PrioritizedTypeConverter> candidates = CollectionAlgorithms.filter(
                    sourceTypeToTypeConvertersMap,
                    new Filter<Tuple<Class<?>, PrioritizedTypeConverter>>() {
                        @Override
                        public boolean accept(final Tuple<Class<?>, PrioritizedTypeConverter> candidate) {

                            // Only accept candidates able to convert from the supplied sourceType/fromType.
                            return candidate.getKey().isAssignableFrom(sourceType);
                        }
                    });

            // Now, prioritize the found sourceTypes
            final ClassPriorityTransformer transformer = new ClassPriorityTransformer(sourceType);
            final SortedMap<Integer, Class<?>> prioritizedFromTypes = new TreeMap<Integer, Class<?>>(
                    CollectionAlgorithms.map(candidates.keySet(), transformer));

            // Populate the return map
            for (Integer current : prioritizedFromTypes.keySet()) {
                toReturn.put(current, candidates.get(prioritizedFromTypes.get(current)));
            }
        }

        // Debug somewhat
        log.debug("Source type [" + sourceType.getSimpleName() + "] yields converters: " + toReturn);

        // All done.
        return toReturn;
    }

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
