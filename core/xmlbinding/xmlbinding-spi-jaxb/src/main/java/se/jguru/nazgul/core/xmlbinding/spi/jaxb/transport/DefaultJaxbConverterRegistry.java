/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;
import se.jguru.nazgul.core.reflection.api.conversion.registry.DefaultConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.StandardConverters;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import javax.xml.bind.annotation.XmlType;

/**
 * Default JaxbConverterRegistry implementation, using reflection Converter
 * instances to convert back and forth between JAXB-annotated transport types.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultJaxbConverterRegistry implements JaxbConverterRegistry {

    // Internal state
    private ConverterRegistry registry;

    /**
     * Default constructor, creating a default ConverterRegistry to
     * which most calls are delegated.
     */
    public DefaultJaxbConverterRegistry() {

        // Create internal state
        this.registry = new DefaultConverterRegistry();

        // Add the standard converters
        addConverters(new StandardConverters());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConverters(final Object... converters) throws IllegalArgumentException {
        registry.add(converters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TransportType, OriginalType> Class<TransportType> getTransportType(final Class<OriginalType> originalType) {

        // While JaxbAnnotatedNull is used to transport null values, its *type* cannot be null.
        Validate.notNull(originalType, "Cannot handle null originalType argument.");

        // If the OriginalType is annotated with @XmlElement, simply return it.
        if (originalType.isAnnotationPresent(XmlType.class)) {
            return (Class<TransportType>) originalType;
        }

        // Find something in our registry?
        for (Class<?> current : registry.getPossibleConversions(originalType)) {

            // Is this a transport type?
            if (current.isAnnotationPresent(XmlType.class)) {
                return (Class<TransportType>) current;
            }
        }

        // No converter found.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> Class<OriginalType> getOriginalType(
            final Class<TransportType> transportType) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(transportType, "Cannot handle null transportType argument.");
        Validate.isTrue(transportType != JaxbAnnotatedNull.class,
                "Cannot acquire OriginalType for JaxbAnnotatedNull argument.");

        // If the transportType is not annotated with XmlType ... complain.
        if (!transportType.isAnnotationPresent(XmlType.class)) {
            throw new IllegalArgumentException("Supplied transportType [" + transportType.getName()
                    + "] is not annotated with XmlType.");
        }

        // Find something in our registry?
        for (Class<?> current : registry.getPossibleConversions(transportType)) {

            // Is this a non-transport type?
            if (!current.isAnnotationPresent(XmlType.class)) {
                return (Class<OriginalType>) current;
            }
        }

        // None found.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> TransportType packageForTransport(final OriginalType source)
            throws IllegalArgumentException {

        // Null values --> JaxbAnnotatedNull
        if (source == null) {
            return (TransportType) JaxbAnnotatedNull.getInstance();
        }

        // Find the transport type of the source.
        final Class<TransportType> transportTypeClass = getTransportType(source.getClass());

        // All done
        return transportTypeClass == null ? (TransportType) source : registry.convert(source, transportTypeClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> OriginalType resurrectAfterTransport(final TransportType transport)
            throws IllegalArgumentException {

        // No null values are accepted here (since they must have been converted
        // into JaxbAnnotatedNull instances).
        Validate.notNull(transport, "Cannot resurrect null transportType instance.");

        // Handle actual null cases
        if (transport instanceof JaxbAnnotatedNull) {
            return null;
        }

        // Find the original type of the transport
        final Class<OriginalType> originalType = getOriginalType(transport.getClass());

        // All done
        return originalType == null ? (OriginalType) transport : registry.convert(transport, originalType);
    }
}
