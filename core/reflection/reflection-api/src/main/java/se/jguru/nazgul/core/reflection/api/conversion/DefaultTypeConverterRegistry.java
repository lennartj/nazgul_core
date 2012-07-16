/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple POJO TypeConverterRegistry implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTypeConverterRegistry implements TypeConverterRegistry {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(DefaultTypeConverterRegistry.class);

    // Internal state
    private final Object lock = new Object();
    private Map<Class, List<TypeConverter>> typeConverters;

    /**
     * Default constructor, creating an empty HashMap to store the known typeConverters.
     */
    public DefaultTypeConverterRegistry() {
        this(new HashMap<Class, List<TypeConverter>>());
    }

    /**
     * Compound constructor, using the provided typeConverters map to store all known typeConverters.
     *
     * @param typeConverters The map of all known typeConverters.
     */
    public DefaultTypeConverterRegistry(final Map<Class, List<TypeConverter>> typeConverters) {

        Validate.notNull(typeConverters, "Cannot handle null typeConverters argument.");
        this.typeConverters = typeConverters;
    }

    /**
     * Adds the provided TypeConverter to this TypeConverterRegistry.
     *
     * @param toAdd The TypeConverter to add.
     */
    @Override
    public void addTypeConverter(final TypeConverter toAdd) {

        Validate.notNull(toAdd, "Cannot handle null toAdd parameter.");

        final Class toType = toAdd.getToType();
        final Class fromType = toAdd.getFromType();
        final List<TypeConverter> converterList = getOrCreateTypeConverterList(fromType);

        synchronized (lock) {

            for (TypeConverter current : converterList) {
                if (current.getToType().equals(toType)) {
                    throw new IllegalArgumentException("TypeConverter [" + fromType.getName() + "-->"
                            + toType.getName() + "] already registered.");
                }
            }

            // Fair to add the new TypeConverter
            converterList.add(toAdd);
        }
    }

    /**
     * Converts the provided source object to the desired type.
     *
     * @param source      The object to convert.
     * @param desiredType The type to which the source object should be converted.
     * @param <To>        The resulting type.
     * @param <From>      The source type.
     * @return The converted object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    @Override
    public <From, To, C extends To> C convert(From source, Class<To> desiredType) throws IllegalArgumentException {

        // Check sanity
        if (source == null) {
            return null;
        }

        Validate.notNull(desiredType, "Cannot handle null desiredType argument for non-null source.");
        final Class<From> fromType = (Class<From>) source.getClass();

        // Get the proper TypeConverter.
        final TypeConverter<? extends From, ? extends To> typeConverter = getTypeConverter(fromType, desiredType);
        if (typeConverter == null) {
            throw new IllegalStateException("No " + getTypeConverterDescription(fromType, desiredType)
                    + " registered.");
        }

        // Convert and return.
        return (C) ((TypeConverter<From, ? extends To>) typeConverter).convert(source);
    }

    //
    // Private helpers
    //

    /**
     * Retrieves or creates the List of TypeConverters which can translate from the provided type.
     *
     * @param fromType   The type from which all returned TypeConverters should be able to translate.
     * @param <FromType> The type from which all returned TypeConverters should be able to translate.
     * @return The List holding all matching TypeConverters.
     */
    private <FromType> List<TypeConverter> getOrCreateTypeConverterList(final Class<FromType> fromType) {

        List<TypeConverter> toReturn;

        synchronized (lock) {
            toReturn = typeConverters.get(fromType);

            if (toReturn == null) {
                // Try to find a non-exact match, i.e. a converter which handles a subtype of the provided fromType.
                for (Class<?> current : typeConverters.keySet()) {
                    if (current.isAssignableFrom(fromType)) {
                        toReturn = typeConverters.get(current);
                        break;
                    }
                }
            }

            if (toReturn == null) {
                // No converter found. Create an empty TypeConverterList.
                toReturn = new ArrayList<TypeConverter>();
                typeConverters.put(fromType, (List<TypeConverter>) toReturn);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Retrieves or creates the a TypeConverter which can translate from the fromType to the toType.
     *
     * @param fromType The type from which the returned TypeConverter should convert.
     * @param toType   The type from which the returned TypeConverter should convert.
     * @return The TypeConverter instance matching the supplied types, or {@code null} if no such
     *         converter was registered.
     */
    private <From, To> TypeConverter<? extends From, ? extends To> getTypeConverter(
            final Class<From> fromType, final Class<To> toType) {

        final List<TypeConverter> converterList = getOrCreateTypeConverterList(fromType);
        for (TypeConverter<? extends From, ?> current : converterList) {
            if (toType.equals(current.getToType())) {
                // Exact ToType match. All done.
                return (TypeConverter<? extends From, ? extends To>) current;
            }
        }

        for (TypeConverter<? extends From, ?> current : converterList) {
            if (toType.isAssignableFrom(current.getToType())) {
                // Subtype ToType match. All done.
                return (TypeConverter<? extends From, ? extends To>) current;
            }
        }

        // Nothing found.
        return null;
    }

    private <From, To> String getTypeConverterDescription(final Class<From> fromType, final Class<To> toType) {
        return "TypeConverter[" + fromType.getName() + " --> " + toType.getName() + "]";
    }
}
