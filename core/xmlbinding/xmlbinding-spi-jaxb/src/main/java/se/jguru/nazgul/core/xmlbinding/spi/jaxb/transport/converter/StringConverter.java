/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedString;

/**
 * Converter for {@code String} values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringConverter extends AbstractConditionalTransportTypeConverter
        implements TransportTypeConverter<String, JaxbAnnotatedString> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canReviveInstance(final Object nonNullInstance) {
        return nonNullInstance instanceof JaxbAnnotatedString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canPackageInstance(final Object nonNullInstance) {
        return nonNullInstance instanceof String;
    }

    /**
     * Converts the provided instance to a transport type, ready for transmission in serialized form.
     *
     * @param toConvert The original type to be converted to transport form.
     * @return A transport type.
     */
    @Override
    public JaxbAnnotatedString packageForTransport(final String toConvert) {
        return new JaxbAnnotatedString(toConvert);
    }

    /**
     * Converts the provided transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert The transport type to be converted.
     * @return A clone of the OriginalType's instance.
     */
    @Override
    public String reviveAfterTransport(final JaxbAnnotatedString toConvert) {
        return toConvert.getValue();
    }

    /**
     * {@inheritDoc}

    @Override
    public int compareTo(final Object that) {
        return that != null && that instanceof StringConverter ? 0 : -1;
    }
     */
}
