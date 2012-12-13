/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.ConditionalTransportTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Abstract AbstractConditionalTransportTypeConverter implementation which
 * delegates the actual XML marshalling and unmarshalling to a wrapped
 * XmlAdapter instance.
 *
 * @param <OriginalType>  The type that JAXB doesn't know how to handle.
 *                        An adapter is written to allow this type to be used as an in-memory
 *                        representation through the {@code TransportType}.
 * @param <TransportType> The type used in transport (i.e. that JAXB knows how to handle out of the box).
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractXmlAdapterTypeConverter<TransportType, OriginalType>
        extends AbstractConditionalTransportTypeConverter
        implements TransportTypeConverter<OriginalType, TransportType>, ConditionalTransportTypeConverter, Comparable {

    // Internal state
    private Class<TransportType> transportTypeClass;
    private Class<OriginalType> originalTypeClass;

    /**
     * Creates a new AbstractXmlTypeConverterAdapter instance which delegates all converting
     * to an internal XmlAdapter instance.
     *
     * @param transportTypeClass The class of the (JAXB) transport type.
     * @param originalTypeClass  The class of the original type.
     * @see #getAdapter()
     */
    protected AbstractXmlAdapterTypeConverter(final Class<TransportType> transportTypeClass,
                                              final Class<OriginalType> originalTypeClass) {

        // Check sanity
        Validate.notNull(transportTypeClass, "Cannot handle null transportTypeClass argument.");
        Validate.notNull(originalTypeClass, "Cannot handle null originalTypeClass argument.");

        // Assign internal state
        this.transportTypeClass = transportTypeClass;
        this.originalTypeClass = originalTypeClass;
    }

    /**
     * @return An XmlAdapter instance converting between the TransportType and OriginalType.
     *         Cannot be {@code null}.
     */
    protected abstract XmlAdapter<TransportType, OriginalType> getAdapter();

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canPackageInstance(final Object nonNullInstance) {
        return originalTypeClass.isAssignableFrom(nonNullInstance.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean canReviveInstance(final Object nonNullInstance) {
        return transportTypeClass.isAssignableFrom(nonNullInstance.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportType packageForTransport(final OriginalType toConvert) {
        try {
            return getAdapter().marshal(toConvert);
        } catch (Exception e) {
            throw new IllegalStateException("Could not package " + originalTypeClass.getName()
                    + " instance for transport.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OriginalType reviveAfterTransport(final TransportType toConvert) {
        try {
            return getAdapter().unmarshal(toConvert);
        } catch (Exception e) {
            throw new IllegalStateException("Could not revive " + transportTypeClass.getName()
                    + " instance after transport.", e);
        }
    }
}
