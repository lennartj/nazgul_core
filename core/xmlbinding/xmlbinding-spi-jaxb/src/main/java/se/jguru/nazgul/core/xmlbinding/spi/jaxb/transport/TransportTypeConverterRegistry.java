/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * TransportType converter registry specification, used as a generic TransportType conversion service.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TransportTypeConverterRegistry extends TransportMetaData {

    /**
     * Adds the provided TransportTypeConverter instance to this TransportTypeConverterRegistry.
     *
     * @param toAdd The TransportTypeConverter to add/register with this TypeConverterRegistry.
     * @throws IllegalArgumentException if toAdd was null.
     */
    public void addTransportTypeConverter(final TransportTypeConverter toAdd) throws IllegalArgumentException;

    /**
     * Retrieves a registered TransportTypeConverter which can convert the
     * provided instance to a transport type instance, ready for transport.
     *
     * @param instance The instance for which a transport-packaging TransportTypeConverter should be retrieved.
     * @return A TransportTypeConverter appropriate for converting the provided instance to a transport
     *         type - or <code>null</code> should none be found.
     */
    public TransportTypeConverter getPackagingTransportTypeConverter(final Object instance);

    /**
     * Retrieves a registered TransportTypeConverter which can convert the
     * provided transport type instance back to its original type.
     *
     * @param instance The instance for which a transport-reviving TransportTypeConverter should be retrieved.
     * @return A TransportTypeConverter appropriate for reviving the provided transport type
     *         instance into its original type and state - or <code>null</code> should none be found.
     */
    public TransportTypeConverter getRevivingTypeConverter(final Object instance);
}

