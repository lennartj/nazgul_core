/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper;

import se.jguru.nazgul.core.reflection.api.conversion.AbstractTypeConverter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringBufferToStringConverter extends AbstractTypeConverter<StringBuffer, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isConvertible(final StringBuffer nonNullInstance) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert(final StringBuffer instance) {
        return instance.toString();
    }
}
