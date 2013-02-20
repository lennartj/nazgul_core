/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

/**
 * Generic type conversion specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TypeConverter<From, To> {

    /**
     * @return The target type of this TypeConverter.
     */
    Class<To> getToType();

    /**
     * @return The source type of this TypeConverter.
     */
    Class<From> getFromType();

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param instance The instance which should be validated for conversion.
     * @return {@code true} if this JaxbTransportTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    boolean canConvert(From instance);

    /**
     * Converts the provided instance to the resulting type.
     *
     * @param instance The instance to convert.
     * @return The converted instance of type {@code To}.
     */
    To convert(From instance);
}
