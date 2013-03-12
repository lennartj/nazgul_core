/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.reflection.api.conversion.TypeConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Holder class for a set of prioritized TypeConverter instances, all of which convert objects
 * from a single source class/type.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class PrioritizedTypeConverter<From> implements Comparable<PrioritizedTypeConverter> {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(PrioritizedTypeConverter.class);

    // Internal state
    private Class<From> sourceType;
    private final Object lock = new Object();
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
        this.prioritizedTypeConverterMap = Collections.synchronizedSortedMap(
                new TreeMap<Integer, Map<Class<?>, TypeConverter<From, ?>>>());
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
     * Acquires all available targetTypes known to this PrioritizedTypeConverter instance, indicating the closure of
     * all Classes to which this PrioritizedTypeConverter can convert objects.
     *
     * @return An unordered Set holding all available target type objects.
     */
    public Set<Class<?>> getAvailableTargetTypes() {

        final Set<Class<?>> toReturn = new HashSet<Class<?>>();

        for(Map.Entry<Integer, Map<Class<?>, TypeConverter<From, ?>>> prioTypeConverterMap
                : prioritizedTypeConverterMap.entrySet()) {

            // Acquire the current TypeConverter instances.
            final Map<Class<?>, TypeConverter<From, ?>> from2ConverterMap = prioTypeConverterMap.getValue();

            for(Map.Entry<Class<?>, TypeConverter<From, ?>> currentFrom2Converter : from2ConverterMap.entrySet()) {

                final TypeConverter<From, ?> currentTypeConverter = currentFrom2Converter.getValue();
                final Class<?> currentToType = currentTypeConverter.getToType();

                if (!toReturn.contains(currentToType)) {
                    toReturn.add(currentToType);
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
    public void add(final Object... converters) {

        // Check sanity
        Validate.notEmpty(converters, "Cannot handle null or empty converters argument.");

        final Map<Object, Tuple<List<Method>, List<Constructor<?>>>> validConverterMap =
                new HashMap<Object, Tuple<List<Method>, List<Constructor<?>>>>();

        // Validate that all converters are OK before modifying the internal state
        // of this PrioritizedTypeConverter instance.
        outer:
        for (Object current : converters) {

            // Find any converter methods in the supplied converter
            final List<Method> methods = TypeExtractor.getMethods(
                    current.getClass(),
                    ReflectiveConverterFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(current.getClass().getConstructors()),
                    ReflectiveConverterFilter.CONVERSION_CONSTRUCTOR_FILTER);

            if (methods.size() == 0 && constructors.size() == 0) {

                // No converters methods or constructors found within the supplied converter. Complain.
                throw new IllegalArgumentException("Found no @Converter-annotated methods within class ["
                        + current.getClass().getName() + "].");
            }

            // Map any discovered Method and Constructor converters which
            // convert objects from the existing sourceType.
            final List<Method> validConverterMethods = new ArrayList<Method>();
            final List<Constructor<?>> validConverterConstructors = new ArrayList<Constructor<?>>();

            for (Method currentMethod : methods) {
                if (currentMethod.getParameterTypes()[0].equals(sourceType)
                        && !validConverterMap.keySet().contains(current)) {

                    // This is a valid ConverterMethod.
                    validConverterMethods.add(currentMethod);
                }
            }

            for (Constructor<?> currentConstructor : constructors) {
                if (currentConstructor.getParameterTypes()[0].equals(sourceType)
                        && !validConverterMap.keySet().contains(current)) {

                    // This is a valid ConverterConstructor.
                    validConverterConstructors.add(currentConstructor);
                }
            }

            // Map the converters
            validConverterMap.put(current,
                    new Tuple<List<Method>, List<Constructor<?>>>(
                            validConverterMethods,
                            validConverterConstructors));
        }

        // Find the Method or Constructor as appropriate for the distilled converters.
        for(Map.Entry<Object, Tuple<List<Method>, List<Constructor<?>>>> currentEntry : validConverterMap.entrySet()) {

            // Map all constructor converters.
            final Tuple<List<Method>, List<Constructor<?>>> typeConverterTuple = currentEntry.getValue();

            // Start with the constructors
            for (Constructor<?> currentConstructor : typeConverterTuple.getValue()) {

                // Create and map the TypeConverter
                final ConstructorTypeConverter converter = new ConstructorTypeConverter(currentConstructor);
                addTypeConverter(converter);
            }

            for (Method currentMethod : typeConverterTuple.getKey()) {

                try {
                    // Create and map the TypeConverter
                    final MethodTypeConverter converter = new MethodTypeConverter(currentEntry.getKey(), currentMethod);
                    addTypeConverter(converter);

                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("conditionalConverter method not found for converter ["
                            + currentMethod + "]", e);
                }
            }
        }
    }

    /**
     * Retrieves the prioritized list holding all known TypeConverters able to convert between the
     * sourceType to the targetType of this PrioritizedTypeConverter instance.
     *
     * @param targetType The type to which all retrieved TypeConverters should be able to convert to.
     * @param <To>       The target Type desired.
     * @return a prioritized list holding all known TypeConverters able to convert between the
     *         sourceType to the targetType.
     */
    public <To> List<TypeConverter<From, To>> getTypeConverters(final Class<To> targetType) {

        // Check sanity
        Validate.notNull(targetType, "Cannot handle null targetType argument.");

        final List<TypeConverter<From, To>> toReturn = new ArrayList<TypeConverter<From, To>>();

        for (Integer current : prioritizedTypeConverterMap.keySet()) {

            // Acquire the TypeConverter to return.
            final Map<Class<?>, TypeConverter<From, ?>> from2TypeConvMap = prioritizedTypeConverterMap.get(current);

            // Exact match?
            final TypeConverter<From, ?> candidate = from2TypeConvMap.get(targetType);
            if (candidate != null) {
                toReturn.add((TypeConverter<From, To>) candidate);
            }
        }

        for (Integer current : prioritizedTypeConverterMap.keySet()) {

            // Acquire the TypeConverter to return.
            final Map<Class<?>, TypeConverter<From, ?>> from2TypeConvMap = prioritizedTypeConverterMap.get(current);

            // Fuzzy matches go after exact matches
            for(Map.Entry<Class<?>, TypeConverter<From, ?>> currentSourceClass2TypeConverter : from2TypeConvMap.entrySet()) {
                if (currentSourceClass2TypeConverter.getKey().isAssignableFrom(targetType)) {
                    toReturn.add((TypeConverter<From, To>) currentSourceClass2TypeConverter.getValue());
                }
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Performs a standard conversion from the supplied toConvert value to the given toType.
     * The known converters will be attempted in correct priority order; the first TypeConverter
     * instance which can handle the requested conversion will be executed to receive the results.
     *
     * @param toConvert The object which should be converted.
     * @param toType    The type to convert to.
     * @param <To>      The type to convert to.
     * @return {@code null} if toConvert could not be converted, and the resulting [converted] instance otherwise.
     */
    public <To> To convert(final From toConvert, final Class<To> toType) {

        // Check sanity
        Validate.notNull(toType, "Cannot handle null toType argument.");

        // Acquire all possible TypeConverters
        final List<TypeConverter<From, To>> typeConverters = getTypeConverters(toType);

        final String toConvertTypeName = toConvert == null ? "null" : toConvert.getClass().getSimpleName();
        log.warn("Converting [" + toConvertTypeName + " --> " + toType.getSimpleName()
                + "] using " + typeConverters + ". My Converters: " + this.prioritizedTypeConverterMap);

        if (!typeConverters.isEmpty()) {
            for (TypeConverter<From, To> current : typeConverters) {
                if (current.canConvert(toConvert)) {
                    return current.convert(toConvert);
                }
            }
        }

        // Could not convert the supplied value.
        return null;
    }

    /**
     * Prints out a debug information string about the state within this PrioritizedTypeConverter instance.
     *
     * @return The source type and possible target types of this PrioritizedTypeConverter instance.
     *         For simplicity, the target types are sorted in alphabetical order.
     */
    @Override
    public String toString() {

        // Acquire all target types.
        final SortedMap<Integer, SortedSet<String>> priorityTargetTypeMap = new TreeMap<Integer, SortedSet<String>>();
        for (int currentIndex : this.prioritizedTypeConverterMap.keySet()) {

            // Acquire all [simple] class names of the target types.
            final List<String> classNames = CollectionAlgorithms.flatten(
                    prioritizedTypeConverterMap.get(currentIndex),
                    new Transformer<Tuple<Class<?>, TypeConverter<From, ?>>, String>() {
                        @Override
                        public String transform(final Tuple<Class<?>, TypeConverter<From, ?>> input) {

                            // Just dig out the simple class name.
                            return input.getKey().getSimpleName();
                        }
                    });

            // Sort and add the class names.
            priorityTargetTypeMap.put(currentIndex, new TreeSet<String>(classNames));
        }


        // All done.
        return "PrioritizedTypeConverter: [" + getSourceType().getSimpleName() + "] --> ["
                + priorityTargetTypeMap + "]";
    }

    //
    // Internal classes
    //

    /**
     * Abstract reflection-based implementation of the TypeConverter specification.
     *
     * @param <To> The type to convert to (i.e. targetType).
     */
    abstract class AbstractReflectiveTypeConverter<To> implements TypeConverter<From, To> {

        // Internal state
        protected Class<To> toType;
        protected Converter converterAnnotation;

        /**
         * Creates an AbstractReflectiveTypeConverter instance converting to the supplied toType.
         *
         * @param toType The type to which this AbstractReflectiveTypeConverter should convert instances.
         */
        protected AbstractReflectiveTypeConverter(final Class<To> toType) {

            // Check sanity
            Validate.notNull(toType, "Cannot handle null toType argument.");

            // Assign internal state
            this.toType = toType;
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
         * @return The Converter annotation for this AbstractReflectiveTypeConverter.
         */
        public final Converter getConverterAnnotation() {
            return converterAnnotation;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {

            final String typeDescription = this instanceof ConstructorTypeConverter ? "Constructor" : "Method";
            StringBuilder builder = new StringBuilder();
            builder.append(typeDescription).append(" converter ");

            if (getConverterName() != null) {
                builder.append("'" + getConverterName() + "' ");
            }

            builder.append("(" + getFromType().getSimpleName() + " --> " + getToType().getSimpleName() + ") ");
            builder.append("\n  Annotation [" + converterAnnotation.toString() + "]");

            // All done.
            return builder.toString();
        }

        /**
         * @return A simple name for this converter.
         */
        protected String getConverterName() {
            return null;
        }
    }

    /**
     * TypeConverter implementation for Constructor-annotated Converters.
     *
     * @param <To> The type to convert to (i.e. targetType).
     */
    class ConstructorTypeConverter<To> extends AbstractReflectiveTypeConverter<To> {

        // Internal state
        private Constructor<To> converterConstructor;

        /**
         * Creates a new ConstructorTypeConverter instance wrapping the supplied Constructor.
         *
         * @param converterConstructor The constructor to be used in converting objects.
         */
        public ConstructorTypeConverter(final Constructor<To> converterConstructor) {

            // Delegate
            super(converterConstructor.getDeclaringClass());

            // Assign internal state
            this.converterConstructor = converterConstructor;
            converterAnnotation = converterConstructor.getAnnotation(Converter.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public To convert(final From instance) {
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
    class MethodTypeConverter<To> extends AbstractReflectiveTypeConverter<To> {

        // Internal state
        private Method converterMethod;
        private Method conditionalConvertionMethod;
        private Object converterInstance;

        /**
         * Creates a new MethodTypeConverter instance which invokes the supplied converter instance using the
         * given converterMethod.
         *
         * @param converter       The converter instance in which the converter method is invoked.
         * @param converterMethod The method invoked to convert the object.
         */
        public MethodTypeConverter(final Object converter, final Method converterMethod) throws NoSuchMethodException {

            // Delegate
            super((Class<To>) converterMethod.getReturnType());

            // Check sanity
            Validate.notNull(converter, "Cannot handle null converter argument.");

            // Assign internal state
            this.converterInstance = converter;
            this.converterMethod = converterMethod;
            converterAnnotation = converterMethod.getAnnotation(Converter.class);

            // Do we have a defined conditionalConversionMethod?
            if (!converterAnnotation.conditionalConversionMethod().equals(Converter.NO_CONVERTER_METHOD)) {

                // Acquire the method.
                conditionalConvertionMethod = converter.getClass().getMethod(
                        converterAnnotation.conditionalConversionMethod(),
                        PrioritizedTypeConverter.this.getSourceType());
            }
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {

            final StringBuilder builder = new StringBuilder(super.toString());

            if (conditionalConvertionMethod != null) {
                builder.append("\n  Conditional Conversion Method: [" + conditionalConvertionMethod + "]");
            }

            builder.append("\n  Converter instance type [" + converterInstance.getClass().getName() + "]");

            // All done.
            return builder.toString();
        }

        /**
         * @return A simple name for this converter.
         */
        @Override
        protected String getConverterName() {
            return converterMethod.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canConvert(final From instance) {

            // Do we have a complex evaluator?
            if (conditionalConvertionMethod != null) {
                try {
                    return (Boolean) conditionalConvertionMethod.invoke(converterInstance, instance);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not invoke [" + conditionalConvertionMethod + "]", e);
                }
            }

            // Fallback to standard mechanics.
            return instance != null || converterAnnotation.acceptsNullValues();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public To convert(final From instance) {
            try {
                return (To) converterMethod.invoke(converterInstance, instance);
            } catch (Exception e) {
                final String typeName = instance == null ? "<null>" : instance.getClass().getName();
                throw new IllegalArgumentException("Could not convert [" + typeName + "] to ["
                        + getToType().getName() + "]", e);
            }
        }
    }

    //
    // Private helpers
    //

    /**
     * Adds the supplied TypeConverter to the internal state of this PrioritizedTypeConverter, returning
     * any TypeConverter which was replaced (i.e. an earlier map of the same priority and toType).
     *
     * @param toAdd The TypeConverter to add to the internal structure.
     * @param <T>   The exact type of AbstractReflectiveTypeConverter added
     * @return the TypeConverter which was replaced, or {@code null} if none was replaced by this add.
     */
    private <T extends AbstractReflectiveTypeConverter> T addTypeConverter(final T toAdd) {

        // Extract required data
        final int priority = toAdd.converterAnnotation.priority();
        final Class<?> toClass = toAdd.getToType();

        Map<Class<?>, TypeConverter<From, ?>> priorityMap = prioritizedTypeConverterMap.get(priority);

        synchronized (lock) {

            if (priorityMap == null) {
                priorityMap = new HashMap<Class<?>, TypeConverter<From, ?>>();
                prioritizedTypeConverterMap.put(priority, priorityMap);
            }

            // All done.
            return (T) priorityMap.put(toClass, toAdd);
        }
    }
}
