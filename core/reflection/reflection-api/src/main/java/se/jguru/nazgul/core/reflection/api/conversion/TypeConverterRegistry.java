/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

import java.util.Comparator;

/**
 * Type converter registry specification, used as a generic type conversion service.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TypeConverterRegistry {

    /**
     * Adds the provided TypeConverter to this TypeConverterRegistry.
     *
     * @param toAdd The TypeConverter to add.
     */
    void addTypeConverter(TypeConverter toAdd);

    /**
     * Converts the provided source object to the desired type.
     *
     * @param source      The object to convert.
     * @param desiredType The type to which the source object should be converted.
     * @param <To>        The resulting type.
     * @param <From>      The source type.
     * @param <C>         The exact return type, subtype of To.
     * @return The converted object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    <From, To, C extends To> C convert(From source, Class<To> desiredType) throws IllegalArgumentException;
}
