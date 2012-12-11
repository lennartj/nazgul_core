/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.CollectionConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.DateTimeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.NullConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.StringConverter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract TransportTypeConverterRegistry implementation, holding a List of
 * TransportTypeConverter instances to which all work is delegated.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTransportTypeConverterRegistry implements TransportTypeConverterRegistry {

    // Internal state
    private List<TransportTypeConverter> converters = new ArrayList<TransportTypeConverter>();
    private Map<Class<?>, Class<?>> originalType2TransportTypeMap = new HashMap<Class<?>, Class<?>>();
    private Map<Class<?>, Class<?>> transportType2OriginalTypeMap = new HashMap<Class<?>, Class<?>>();

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

        final List<Method> toTransport = TypeExtractor.getMethods(toAdd.getClass(), new Filter<Method>() {
            @Override
            public boolean accept(final Method candidate) {
                return candidate.getName().equals("packageForTransport")
                        && candidate.getReturnType() != Void.TYPE
                        && candidate.getParameterTypes().length == 1;
            }
        });

        final List<Method> toOriginal = TypeExtractor.getMethods(toAdd.getClass(), new Filter<Method>() {
            @Override
            public boolean accept(final Method candidate) {
                return candidate.getName().equals("reviveAfterTransport")
                        && candidate.getReturnType() != Void.TYPE
                        && candidate.getParameterTypes().length == 1;
            }
        });

        final Class<?> transportType = toTransport.get(0).getReturnType();
        final Class<?> originalType = toOriginal.get(0).getReturnType();

        if (!originalType2TransportTypeMap.containsKey(originalType)) {
            originalType2TransportTypeMap.put(originalType, transportType);
            transportType2OriginalTypeMap.put(transportType, originalType);
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
    public TransportTypeConverter getPackagingTransportTypeConverter(final Object instance) {

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
    public TransportTypeConverter getRevivingTypeConverter(final Object instance) {

        for (TransportTypeConverter current : converters) {
            if (current.canReviveAfterTransport(instance)) {
                return current;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OriginalType, TransportType> Class<OriginalType> getOriginalType(
            final Class<TransportType> transportType) {
        return (Class<OriginalType>) transportType2OriginalTypeMap.get(transportType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <TransportType, OriginalType> Class<TransportType> getTransportType(
            final Class<OriginalType> originalType) {
        return (Class<TransportType>) originalType2TransportTypeMap.get(originalType);
    }
}
