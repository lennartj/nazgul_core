/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry.filters;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Constructor;

/**
 * Filter implementation detecting properly annotated Converter constructors.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ConvertionConstructorFilter implements Filter<Constructor<?>> {

    /**
     * Singleton instance of this ConvertionConstructorFilter type.
     */
    public static final ConvertionConstructorFilter CONVERTION_CONSTRUCTOR_FILTER = new ConvertionConstructorFilter();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Constructor<?> candidate) {
        return candidate.getAnnotation(Converter.class) != null
                && candidate.getParameterTypes().length == 1;
    }
}
