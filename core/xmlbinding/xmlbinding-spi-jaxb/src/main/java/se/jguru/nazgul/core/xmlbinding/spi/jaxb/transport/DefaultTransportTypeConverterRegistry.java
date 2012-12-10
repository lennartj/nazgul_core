/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.CollectionConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.DateTimeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.NullConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract TransportTypeConverterRegistry implementation, holding a List of
 * TransportTypeConverter instances to which all work is delegated.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTransportTypeConverterRegistry implements TransportTypeConverterRegistry {

    // Internal state
    private List<TransportTypeConverter> converters = new ArrayList<TransportTypeConverter>();

    /**
     * Default constructor, adding all locally-defined converters.
     */
    public DefaultTransportTypeConverterRegistry() {

        // Add default converters.
        addTransportTypeConverter(new NullConverter());
        addTransportTypeConverter(new StringConverter());
        addTransportTypeConverter(new DateTimeConverter());
        addTransportTypeConverter(new CollectionConverter());
    }

    /**
     * Adds the provided TransportTypeConverter instance to this TransportTypeConverterRegistry.
     *
     * @param toAdd The TransportTypeConverter to add/register with this TypeConverterRegistry.
     * @throws IllegalArgumentException if toAdd was null.
     */
    @Override
    public void addTransportTypeConverter(final TransportTypeConverter toAdd) throws IllegalArgumentException {

        Validate.notNull(toAdd, "Cannot handle null TransportTypeConverter instance.");

        if (!converters.contains(toAdd)) {
            converters.add(toAdd);
        }
    }

    /**
     * Retrieves a registered TransportTypeConverter which can convert the
     * provided instance to a transport type instance, ready for transport.
     *
     * @param instance The instance for which a transport-packaging TransportTypeConverter should be retrieved.
     * @return The first found TransportTypeConverter appropriate for converting the provided instance to
     *         a transport type - or <code>null</code> should none be found.
     */
    @Override
    public TransportTypeConverter getPackagingTransportTypeConverter(Object instance) {

        for (TransportTypeConverter current : converters) {
            if (current.canPackageForTransport(instance)) {
                return current;
            }
        }

        return null;
    }

    /**
     * Retrieves a registered TransportTypeConverter which can convert the
     * provided transport type instance back to its original type.
     *
     * @param instance The instance for which a transport-reviving TransportTypeConverter should be retrieved.
     * @return The first found TransportTypeConverter appropriate for reviving the provided transport type
     *         instance into its original type and state - or <code>null</code> should none be found.
     */
    @Override
    public TransportTypeConverter getRevivingTypeConverter(Object instance) {

        for (TransportTypeConverter current : converters) {
            if (current.canReviveAfterTransport(instance)) {
                return current;
            }
        }

        return null;
    }
}
