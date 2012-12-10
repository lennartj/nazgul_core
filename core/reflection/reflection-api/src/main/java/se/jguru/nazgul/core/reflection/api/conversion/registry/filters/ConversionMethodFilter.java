/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry.filters;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Method;

/**
 * Filter implementation detecting properly annotated Converter methods.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ConversionMethodFilter implements Filter<Method> {

    /**
     * Singleton instance of the ConversionMethodFilter class.
     */
    public static final ConversionMethodFilter CONVERSION_METHOD_FILTER = new ConversionMethodFilter();

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
