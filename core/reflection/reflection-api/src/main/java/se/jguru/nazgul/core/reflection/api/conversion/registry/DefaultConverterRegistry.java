/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
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
@SuppressWarnings({"rawtypes", "unchecked"})
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
        for (Map.Entry<Object, Tuple<List<Method>, List<Constructor<?>>>> currentEntry : validConverters.entrySet()) {

            final Tuple<List<Method>, List<Constructor<?>>> methodsAndConstructors = currentEntry.getValue();

            // Map the source types of all Methods & Constructors
            for (Method currentMethod : methodsAndConstructors.getKey()) {

                final Class<?> currentType = currentMethod.getParameterTypes()[0];
                addCurrentConverter(sourceTypeToConverterInstanceMap, currentEntry.getKey(), currentType);
            }

            for (Constructor<?> currentConstructor : methodsAndConstructors.getValue()) {

                final Class<?> currentType = currentConstructor.getParameterTypes()[0];
                addCurrentConverter(sourceTypeToConverterInstanceMap, currentEntry.getKey(), currentType);
            }
        }

        // Create PrioritizedTypeConverters for all source types.
        for (Map.Entry<Class<?>, Set<Object>> current : sourceTypeToConverterInstanceMap.entrySet()) {

            PrioritizedTypeConverter prioritizedTypeConverter = new PrioritizedTypeConverter(
                    current.getKey(), current.getValue().toArray());

            // Finally, add the PrioritizedTypeConverter instance.
            sourceTypeToTypeConvertersMap.put(current.getKey(), prioritizedTypeConverter);
        }
    }

    /**
     * Throws {@code UnsupportedOperationException}.
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
        final Class<From> sourceType = source == null ? null : (Class<From>) source.getClass();
        PrioritizedTypeConverter<From> typeConverter = sourceTypeToTypeConvertersMap.get(sourceType);

        if (typeConverter == null) {

            // Resort to fuzzy logic to acquire the PrioritizedTypeConverter
            final SortedMap<Integer, PrioritizedTypeConverter> prioritized = getPrioritizedConverters(sourceType);

            // Get the least divergent type converter
            typeConverter = prioritized.get(prioritized.lastKey());
        }

        // No TypeConverter found at all?
        if (typeConverter == null) {

            // Holler a tad...
            log.info("TypeConverter not found. [" + desiredType.getSimpleName() + " --> " + typeConverter + "]");
            return null;
        }

        // Dig out the corresponding optimalToType for the acquired TypeConverter.
        // We may acquire a null response here, if no optimalToType can be found.
        final Class<To> optimalToType = getOptimalToType(typeConverter, desiredType);

        To toReturn = null;
        if (optimalToType != null) {

            log.debug("Found TypeConverter [" + desiredType.getSimpleName()
                    + (optimalToType == desiredType ? "" : " (fuzzy: " + optimalToType.getSimpleName() + ")")
                    + " --> " + typeConverter + "]");

            // Convert the data
            toReturn = (To) typeConverter.convert(source, optimalToType);
        }

        // All done.
        return toReturn;
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
        for (Map.Entry<Integer, PrioritizedTypeConverter> currentEntry : converterMap.entrySet()) {

            // Acquire the available target types for the current PrioritizedTypeConverter
            final Set<Class<?>> availableTargetTypes = currentEntry.getValue().getAvailableTargetTypes();
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
        for (Map.Entry<String, Class<?>> current : sortedClassNames.entrySet()) {
            builder.append(sourceTypeToTypeConvertersMap.get(sortedClassNames.get(current.getKey()))).append("\n");
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
        final SortedMap<Integer, List<Class<?>>> prioritizedTypes = createPrioritizedClassMap(
                requestedToType, availableTargetTypes);

        // Unrelated?
        if (prioritizedTypes.size() == 0) {
            return null;
        }

        // Several optimal types available?
        final List<Class<?>> bestChoiceToTypes = prioritizedTypes.get(prioritizedTypes.firstKey());
        final Class<To> optimalToClass = (Class<To>) getOptimalClosestType(bestChoiceToTypes);

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
     * which can convert from the supplied sourceType, mapped to their priority
     * as defined by the algorithm found in the TypeClosenessTransformer class.
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

                            // The sourceType can be null, which generates an exception in the isAssignableFrom method.
                            // Perform normal sanity checking to handle null sourceType values.
                            if (sourceType != null) {

                                // Only accept candidates able to convert from the supplied sourceType/fromType.
                                return candidate.getKey().isAssignableFrom(sourceType);

                            } else {

                                // sourceType is null here, so the candidate should only 'accept'
                                // here if the internal TypeConverter can handle nulls.
                                final PrioritizedTypeConverter converter = candidate.getValue();
                                return converter.getSourceType() == Object.class;
                            }
                        }
                    });

            // Now, prioritize the found sourceTypes
            final SortedMap<Integer, List<Class<?>>> prioritizedClassMap = createPrioritizedClassMap(
                    sourceType, candidates.keySet());

            // Populate the return map
            for (Map.Entry<Integer, List<Class<?>>> current : prioritizedClassMap.entrySet()) {

                // For each priority, simply pick the first type returned.
                toReturn.put(current.getKey(), candidates.get(current.getValue().get(0)));
            }
        }

        // Debug somewhat
        final String sourceTypeSimpleName = sourceType == null ? "<null>" : sourceType.getSimpleName();
        log.debug("Source type [" + sourceTypeSimpleName + "] yields converters: " + toReturn);

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

    private Class<?> getOptimalClosestType(final List<Class<?>> initialBestChoices) {

        // Start with the first available best choice type.
        Class<?> optimalToClass = initialBestChoices.get(0);

        // Several choices available?
        if (initialBestChoices.size() > 1) {

            // If there are class types, use the first one of them.
            // Otherwise use the first bestChoiceToType found.
            final List<Class<?>> optimalClasses = CollectionAlgorithms.filter(
                    initialBestChoices,
                    new Filter<Class<?>>() {
                        @Override
                        public boolean accept(final Class<?> candidate) {
                            return !candidate.isInterface();
                        }
                    });

            optimalToClass = optimalClasses.size() > 0 ? optimalClasses.get(0) : initialBestChoices.get(0);

            // Debug somewhat
            List<String> initialBestChoicesClassNames = new ArrayList<String>();
            for (Class<?> current : initialBestChoices) {
                initialBestChoicesClassNames.add(current.getSimpleName());
            }

            log.debug("Optimal type [" + optimalToClass.getSimpleName()
                    + "] selected from choices " + initialBestChoicesClassNames);

        } else {
            log.debug("Single/optimal best choice type: " + initialBestChoices);
        }

        // All done.
        return optimalToClass;
    }

    private <To> SortedMap<Integer, List<Class<?>>> createPrioritizedClassMap(
            final Class<To> requestedToType, final Set<Class<?>> availableTargetTypes) {

        SortedMap<Integer, List<Class<?>>> prioritizedTypes = new TreeMap<Integer, List<Class<?>>>();
        for (Class<?> current : availableTargetTypes) {

            // Find the current priority
            try {
                // The requestedToType can be null; handle this situation.
                final int priority = requestedToType == null
                        ? -1
                        : TypeExtractor.getRelationDifference(current, requestedToType);

                List<Class<?>> currentPriorityTypes = prioritizedTypes.get(priority);
                if (currentPriorityTypes == null) {
                    currentPriorityTypes = new ArrayList<Class<?>>();
                    prioritizedTypes.put(priority, currentPriorityTypes);
                }

                // ... and add the current type.
                currentPriorityTypes.add(current);

            } catch (IllegalArgumentException e) {
                // Ignore this type.
            }
        }
        return prioritizedTypes;
    }
}
