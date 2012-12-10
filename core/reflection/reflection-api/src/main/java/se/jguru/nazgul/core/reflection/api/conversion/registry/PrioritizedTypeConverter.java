/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.reflection.api.conversion.TypeConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Holder class for a set of Converters
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PrioritizedTypeConverter<From> implements Comparable<PrioritizedTypeConverter> {

    // Internal state
    private Class<From> sourceType;
    private SortedMap<Integer, Map<Class<?>, TypeConverter<From, ?>>> prioritizedTypeConverterMap;

    /**
     * Creates an empty PrioritizedTypeConverter which holds TypeConverter instances with the supplied sourceType.
     *
     * @param sourceType The class to convert from.
     */
    public PrioritizedTypeConverter(final Class<From> sourceType) {
        this(sourceType, null);
    }

    /**
     * Creates a PrioritizedTypeConverter which holds TypeConverter instances with the supplied sourceType.
     * The supplied converters are wrapped into TypeConverter instances, and mapped at the start.
     *
     * @param sourceType The class to convert from.
     * @param converters The supplied converters are wrapped into TypeConverter instances, and mapped at the start.
     */
    public PrioritizedTypeConverter(final Class<From> sourceType,
                                    final Object... converters) {

        // Check sanity
        Validate.notNull(sourceType, "Cannot handle null sourceType argument.");

        // Assign internal state
        this.prioritizedTypeConverterMap = new TreeMap<Integer, Map<Class<?>, TypeConverter<From, ?>>>();
        this.sourceType = sourceType;
        if (converters != null) {
            add(converters);
        }
    }

    /**
     * @return The source type of this ReflectiveMultiConverterHolder instance.
     */
    public final Class<From> getSourceType() {
        return sourceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int compareTo(final PrioritizedTypeConverter that) {

        if (that == null) {
            return Integer.MAX_VALUE;
        }

        // Delegate comparing to the type names of the sourceType.
        return sourceType.getName().compareTo(that.getSourceType().getName());
    }

    /**
     * Validates and adds any converter instances from the supplied array, given that they
     * convert {@code sourceType} instances to some other type.
     *
     * @param converters A list of potential converters, holding methods or constructors converting
     *                   {@code sourceType} instances to some other type. Should any two converter methods (or
     *                   constructors) have identical priority, fromType and toType the result is undefined in the
     *                   sense that an arbitrary converter will be used.
     */
    public void add(Object... converters) {

        // Check sanity
        Validate.notEmpty(converters, "Cannot handle null or empty converters argument.");

        final Set<Object> validConverters = new HashSet<Object>();

        // All converters have annotations?
        outer:
        for (Object current : converters) {

            // Find any converter methods in the supplied converter
            final List<Method> methods = TypeExtractor.getMethods(
                    current.getClass(),
                    ReflectiveConverterFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(current.getClass().getConstructors()),
                    ReflectiveConverterFilter.CONVERTION_CONSTRUCTOR_FILTER);

            if (methods.size() == 0 && constructors.size() == 0) {

                // No converters methods or constructors found within the supplied converter. Complain.
                throw new IllegalArgumentException("Found no @Converter-annotated methods within class ["
                        + current.getClass().getName() + "].");
            }

            for (Method currentMethod : methods) {
                if (currentMethod.getParameterTypes()[0].equals(sourceType)
                        && !validConverters.contains(current)) {
                    validConverters.add(current);

                    // No need to test any further for this converter
                    continue outer;
                }
            }

            for (Constructor<?> currentConstructor : constructors) {
                if (currentConstructor.getParameterTypes()[0].equals(sourceType)
                        && !validConverters.contains(current)) {
                    validConverters.add(current);

                    // No need to test any further for this converter
                    continue outer;
                }
            }
        }

        // Find the Method or Constructor as appropriate for the distilled converters.
        for (Object current : validConverters) {

            // Map all constructor converters.
        }
    }

    /**
     * Acquires the optimum (i.e. best-choice) Converter object able to convert to the supplied targetType Class.
     *
     * @param targetType The type to which the converter should be able to convert.
     * @return The Converter instance found, or {@code null} should no Converter matching the supplied
     *         targetType be added to this ReflectiveMultiConverterHolder.
     */
    public Object getOptimumConverter(final Class<?> targetType) {
        return getConverterWithMinimumPriority(0, targetType);
    }

    /**
     * Acquires a Converter object able to convert to the supplied
     * targetType Class, given the minimum priority supplied.
     *
     * @param minimumPriority The minimum priority of the converter instance returned (inclusive).
     * @param targetType      The type to which the converter should be able to convert.
     * @return The Converter instance found, or {@code null} should no Converter matching the supplied
     *         targetType and minimumPriority be added to this ReflectiveMultiConverterHolder.
     * @throws IllegalArgumentException if {@code minimumPriority &lt; 0}.
     */
    public Object getConverterWithMinimumPriority(int minimumPriority, final Class<?> targetType)
            throws IllegalArgumentException {

        // Check sanity
        Validate.isTrue(minimumPriority >= 0, "Cannot handle negative minimumPriority argument.");
        Validate.notNull(targetType, "Cannot handle null targetType argument.");

        for (Integer current : prioritizedTypeConverterMap.keySet()) {

            if (current >= minimumPriority) {
                final Object converter = prioritizedTypeConverterMap.get(current).get(targetType);
                if (converter != null) {
                    return converter;
                }
            }
        }

        // None found.
        return null;
    }

    //
    // Private helpers
    //

    /**
     * TypeConverter implementation for Constructor-annotated Converters.
     *
     * @param <To> The type to convert to (i.e. targetType).
     */
    class ConstructorTypeConverter<To> implements TypeConverter<From, To> {

        // Internal state
        private Class<To> toType;
        private Converter converterAnnotation;
        private Constructor<To> converterConstructor;

        /**
         * Creates a new ConstructorTypeConverter instance converting to the supplied type.
         *
         * @param toType The type to which instances should be converted.
         * @throws NoSuchMethodException if the supplied toType class does not have a constructor
         *                               accepting a single From instance.
         */
        public ConstructorTypeConverter(final Class<To> toType) throws NoSuchMethodException {

            // Check sanity
            Validate.notNull(toType, "Cannot handle null toType argument.");

            // Assign internal state
            this.toType = toType;
            converterConstructor = toType.getConstructor(PrioritizedTypeConverter.this.getSourceType());
            converterAnnotation = converterConstructor.getAnnotation(Converter.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Class<To> getToType() {
            return toType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final Class<From> getFromType() {
            return PrioritizedTypeConverter.this.getSourceType();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canConvert(final From instance) {
            return instance != null || converterAnnotation.acceptsNullValues();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public To convert(From instance) {
            try {
                return converterConstructor.newInstance(instance);
            } catch (Exception e) {
                final String typeName = instance == null ? "<null>" : instance.getClass().getName();
                throw new IllegalArgumentException("Could not convert [" + typeName + "] to ["
                        + getToType().getName() + "]", e);
            }
        }
    }

    /**
     * TypeConverter implementation for Method-annotated Converters.
     *
     * @param <To> The type to convert to (i.e. targetType).
     */
    class MethodTypeConverter<To> implements TypeConverter<From, To> {

        // Internal state
        private Class<To> toType;
        private Converter converterAnnotation;
        private Method converterMethod;
        private Object converterInstance;

        /**
         *
         * @param converter
         * @param converterMethod The method invoked to convert
         */
        public MethodTypeConverter(final Object converter, final Method converterMethod) {

            // Check sanity
            Validate.notNull(converter, "Cannot handle null converter argument.");
            Validate.notNull(converterMethod, "Cannot handle null converterMethod argument.");

            // Assign internal state
            toType = (Class<To>) converterMethod.getReturnType();
            this.converterInstance = converter;
        }
    }
}
