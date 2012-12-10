/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Filter implementations for use in discovering Converter implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see Converter
 */
public class ReflectiveConverterFilter {

    /**
     * Singleton instance of the ConversionMethodFilter class.
     */
    public static final ConversionMethodFilter CONVERSION_METHOD_FILTER = new ConversionMethodFilter();

    /**
     * Singleton instance of this ConvertionConstructorFilter type.
     */
    public static final ConvertionConstructorFilter CONVERTION_CONSTRUCTOR_FILTER = new ConvertionConstructorFilter();

    /**
     * Filter implementation detecting properly annotated Converter methods.
     */
    private static class ConversionMethodFilter implements Filter<Method> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean accept(final Method candidate) {
            return candidate.getAnnotation(Converter.class) != null
                    && candidate.getParameterTypes().length == 1
                    && candidate.getReturnType() != Void.TYPE;
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
}
