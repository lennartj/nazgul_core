/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
     * Singleton instance of this ConvertionConstructorFilter type.
     */
    public static final Filter<Constructor<?>> CONVERTION_CONSTRUCTOR_FILTER = new ConvertionConstructorFilter();

    /**
     * Hidden constructor.
     */
    private ReflectiveConverterFilter() {}

    /**
     * Filter implementation detecting properly annotated Converter methods.
     */
    private static class ConversionMethodFilter implements Filter<Method> {

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
            if(parameterTypes.length != 1 || candidate.getReturnType() == Void.TYPE) {

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
    private static class ConvertionConstructorFilter implements Filter<Constructor<?>> {

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
    private static class ConditionalConverterMethodFilter implements Filter<Method> {

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
}
