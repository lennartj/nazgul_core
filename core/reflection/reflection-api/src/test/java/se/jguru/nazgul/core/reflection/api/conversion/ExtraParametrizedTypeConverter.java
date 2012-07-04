/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExtraParametrizedTypeConverter<T> extends AbstractTypeConverter<String, Short> {

    // Internal state
    private T anotherParameterType;

    /**
     * Default constructor, extracting type information for convenience generic type methods.
     */
    public ExtraParametrizedTypeConverter() {
    }

    /**
     * Example of a compound constructor.
     */
    public ExtraParametrizedTypeConverter(T someParameter) throws NullPointerException {
        super(String.class, Short.class);

        anotherParameterType = someParameter;
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
    public Short convert(String instance) {
        return Short.decode(instance);
    }

    public T getAnotherParameterType() {
        return anotherParameterType;
    }
}
