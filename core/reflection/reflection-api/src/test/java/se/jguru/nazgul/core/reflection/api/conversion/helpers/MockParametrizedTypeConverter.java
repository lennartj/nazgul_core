/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.helpers;

import se.jguru.nazgul.core.reflection.api.conversion.AbstractTypeConverter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockParametrizedTypeConverter extends AbstractTypeConverter<String, Integer> {

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param nonNullInstance The instance which should be validated for conversion.
     * @return {@code true} if this AbstractTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    @Override
    protected boolean isConvertible(final String nonNullInstance) {

        try {
            convert(nonNullInstance);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Converts the provided instance to the resulting type.
     *
     * @param instance The instance to convert.
     * @return The converted instance of type {@code To}.
     */
    @Override
    public Integer convert(final String instance) {
        return Integer.parseInt(instance);
    }
}
