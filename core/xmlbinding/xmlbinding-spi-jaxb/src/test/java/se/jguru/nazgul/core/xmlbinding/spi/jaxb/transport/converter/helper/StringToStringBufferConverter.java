/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper;

import se.jguru.nazgul.core.reflection.api.conversion.AbstractTypeConverter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringToStringBufferConverter extends AbstractTypeConverter<String, StringBuffer> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isConvertible(final String nonNullInstance) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer convert(final String instance) {
        return new StringBuffer(instance);
    }
}
