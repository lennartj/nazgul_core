/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Filter utility implementations for use in discovering Converter implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see Converter
 */
public final class ReflectiveConverterFilter {

    /**
     * Singleton instance of the ConversionMethodFilter class.
     */
    public static final Filter<Method> CONVERSION_METHOD_FILTER = new ConversionMethodFilter();

    /**
     * Singleton instance of the ConversionConstructorFilter type.
     */
    public static final Filter<Constructor<?>> CONVERSION_CONSTRUCTOR_FILTER = new ConversionConstructorFilter();

    /**
     * Singleton instance of a Transformer yielding the {@code getShortName()} result for Classes.
     */
    public static final Transformer<Class<?>, String> SHORT_CLASSNAME_TRANSFORMER
            = new ClassToClassNameTransformer(true);

    /**
     * Singleton instance of a Transformer yielding the {@code getName()} result for Classes.
     */
    public static final Transformer<Class<?>, String> FULL_CLASSNAME_TRANSFORMER
            = new ClassToClassNameTransformer(false);

    /**
     * Helper method to extract a Tuple of converter methods and constructors, as found within the supplied
     * object instance.
     *
     * @param object The object which should be inspected for
     * @return {@code null} if the object was {@code null} or no converter methods and constructors
     *         were found within the supplied object. Otherwise returns a populated Tuple.
     */
    public static Tuple<List<Method>, List<Constructor<?>>> getConverterMethodsAndConstructors(
            final Object object) {

        // Create the return variable
        Tuple<List<Method>, List<Constructor<?>>> toReturn = null;

        if (object != null) {

            // Find any converter methods in the supplied converter
            final List<Method> methods = TypeExtractor.getMethods(
                    object.getClass(), ReflectiveConverterFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(object.getClass().getConstructors()),
                    ReflectiveConverterFilter.CONVERSION_CONSTRUCTOR_FILTER);

            // Only return a non-null value if we found some converter methods/constructors.
            if (methods.size() != 0 || constructors.size() != 0) {
                toReturn = new Tuple<List<Method>, List<Constructor<?>>>(methods, constructors);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Hidden constructor.
     */
    private ReflectiveConverterFilter() {
    }

    /**
     * Filter implementation detecting properly annotated Converter methods.
     */
    private static final class ConversionMethodFilter implements Filter<Method> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final Method candidate) {

            final Converter converter = candidate.getAnnotation(Converter.class);
            if (converter == null) {
                return false;
            }

            // Check the converter method itself.
            final Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes.length != 1 || candidate.getReturnType() == Void.TYPE) {

                // This is not a proper converter method.
                return false;
            }

            // If we have a conditionalConverterMethod defined, it must be on the form
            //
            // public boolean someMethodName(final FromType aFromType);
            //
            final String conditionalConverterMethod = converter.conditionalConversionMethod();
            boolean hasConditionalConverterMethod = !conditionalConverterMethod.equals(Converter.NO_CONVERTER_METHOD);

            if (hasConditionalConverterMethod) {

                final Class<?> fromType = parameterTypes[0];
                final ConditionalConverterMethodFilter filter = new ConditionalConverterMethodFilter(
                        fromType,
                        converter.conditionalConversionMethod());

                // Acquire the potential conversionMethods
                final List<Method> conversionMethods = TypeExtractor.getMethods(candidate.getDeclaringClass(), filter);

                // All done.
                return conversionMethods.size() > 0;
            }

            // No problems found.
            return true;
        }
    }

    /**
     * Filter implementation detecting properly annotated Converter constructors.
     */
    private static final class ConversionConstructorFilter implements Filter<Constructor<?>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final Constructor<?> candidate) {
            return candidate.getAnnotation(Converter.class) != null
                    && candidate.getParameterTypes().length == 1;
        }
    }

    /**
     * Filter implementation detecting proper conditional converter methods.
     */
    private static final class ConditionalConverterMethodFilter implements Filter<Method> {

        // Internal state
        private Class<?> requiredArgumentType;
        private String methodName;

        private ConditionalConverterMethodFilter(final Class<?> fromType, final String methodName) {
            this.requiredArgumentType = fromType;
            this.methodName = methodName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final Method candidate) {

            // Check return type
            boolean booleanReturnType = candidate.getReturnType() == Boolean.TYPE
                    || candidate.getReturnType() == Boolean.class;

            // Method should have 1 single argument of the correct type.
            boolean correctArgumentType = candidate.getParameterTypes().length == 1
                    && candidate.getParameterTypes()[0].isAssignableFrom(requiredArgumentType);

            // Check name
            boolean matchingName = !methodName.equals(Converter.NO_CONVERTER_METHOD)
                    && candidate.getName().equals(methodName);

            // All done.
            return matchingName && booleanReturnType && correctArgumentType;
        }
    }

    /**
     * Transformer implementation acquiring class names from class instances.
     */
    private static final class ClassToClassNameTransformer implements Transformer<Class<?>, String> {

        // Internal state
        private boolean shortForm;

        private ClassToClassNameTransformer(final boolean shortForm) {
            this.shortForm = shortForm;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String transform(final Class<?> input) {
            return shortForm ? input.getSimpleName() : input.getName();
        }
    }
}
