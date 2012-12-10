/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;

import java.util.Collection;

/**
 * Converter for {@code java.util.Collection} values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CollectionConverter<T extends Collection> extends AbstractConditionalTransportTypeConverter
        implements TransportTypeConverter<T, JaxbAnnotatedCollection<T>> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canPackageInstance(final Object nonNullInstance) {
        return nonNullInstance instanceof Collection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canReviveInstance(final Object nonNullInstance) {
        return nonNullInstance instanceof JaxbAnnotatedCollection;
    }

    /**
     * Converts the provided instance to a transport type, ready for transmission in serialized form.
     *
     * @param toConvert The original type to be converted to transport form.
     * @return A transport type.
     */
    @Override
    public JaxbAnnotatedCollection<T> packageForTransport(final T toConvert) {
        return new JaxbAnnotatedCollection<T>(toConvert);
    }

    /**
     * Converts the provided transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert The transport type to be converted.
     * @return A clone of the OriginalType's instance.
     */
    @Override
    public T reviveAfterTransport(JaxbAnnotatedCollection<T> toConvert) {
        return toConvert.getValue();
    }
}
