/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.reflection.api.conversion.TypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;

/**
 * POJO TransportTypeConverter implementation delegating the actual conversion to TypeConverter instances.
 * This is of use whenever we have generally available TypeConverters.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AggregateTransportTypeConverter<OriginalType, TransportType>
        extends AbstractConditionalTransportTypeConverter
        implements TransportTypeConverter<OriginalType, TransportType> {

    // Internal state
    private TypeConverter<OriginalType, TransportType> packagingConverter;
    private TypeConverter<TransportType, OriginalType> unpackagingConverter;

    /**
     * Compound constructor using the two non-null TypeConverter instances to convert instances
     * for transport and back.
     *
     * @param packagingConverter   The transport packaging TypeConverter, used to convert OriginalType instances
     *                             to TransportType instances before transport.
     * @param unpackagingConverter The transport packaging TypeConverter, used to convert TransportType instances
     *                             to OriginalType instances after transport.
     */
    public AggregateTransportTypeConverter(final TypeConverter<OriginalType, TransportType> packagingConverter,
                                           final TypeConverter<TransportType, OriginalType> unpackagingConverter) {

        // Check sanity
        Validate.notNull(packagingConverter, "Cannot handle null packagingConverter argument.");
        Validate.notNull(unpackagingConverter, "Cannot handle null unpackagingConverter argument.");

        this.packagingConverter = packagingConverter;
        this.unpackagingConverter = unpackagingConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canPackageInstance(final Object nonNullInstance) {

        // Check sanity.
        return packagingConverter.getFromType().isAssignableFrom(nonNullInstance.getClass())
                && packagingConverter.canConvert((OriginalType) nonNullInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canReviveInstance(final Object nonNullInstance) {

        // Check sanity.
        return unpackagingConverter.getFromType().isAssignableFrom(nonNullInstance.getClass())
                && unpackagingConverter.canConvert((TransportType) nonNullInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final TransportType packageForTransport(final OriginalType toConvert) {
        return packagingConverter.convert(toConvert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final OriginalType reviveAfterTransport(final TransportType toConvert) {
        return unpackagingConverter.convert(toConvert);
    }
}
