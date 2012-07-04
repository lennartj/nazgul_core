/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FallbackTypeConverter extends AbstractTypeConverter<String, StringBuffer> {

    /**
     * Default constructor, extracting type information for convenience generic type methods.
     */
    public FallbackTypeConverter() {
        super(String.class, StringBuffer.class);
    }

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param nonNullInstance The instance which should be validated for conversion.
     * @return {@code true} if this AbstractTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    @Override
    protected boolean isConvertible(String nonNullInstance) {
        return true;
    }

    /**
     * Converts the provided instance to the resulting type.
     *
     * @param instance The instance to convert.
     * @return The converted instance of type {@code To}.
     */
    @Override
    public StringBuffer convert(String instance) {
        return new StringBuffer(instance);
    }
}
