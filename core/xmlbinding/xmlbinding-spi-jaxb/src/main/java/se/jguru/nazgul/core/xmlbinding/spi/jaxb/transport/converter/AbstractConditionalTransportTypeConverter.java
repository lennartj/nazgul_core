/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.ConditionalTransportTypeConverter;

/**
 * Abstract implementation of a ConditionalTransportTypeConverter.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractConditionalTransportTypeConverter
        implements ConditionalTransportTypeConverter, Comparable {

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean canPackageForTransport(Object instance) {

        // Nulls cannot normally be packaged by AbstractTransportTypeConverter implementations.
        return instance != null && canPackageInstance(instance);
    }

    /**
     * Acquires a result defining if this TransportTypeConverter can
     * be used to package the provided instance for [Serialized] transport.
     * The "Package" process simply implies converting the provided instance
     * into a transportType instance holding the state of the original instance.
     *
     * @param nonNullInstance The non-null instance for which to check packaging capability.
     * @return {@code true} if this AbstractTransportTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    protected abstract boolean canPackageInstance(Object nonNullInstance);

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean canReviveAfterTransport(Object instance) {

        // Nulls cannot normally be revived by AbstractTransportTypeConverter implementations.
        return instance != null && canReviveInstance(instance);
    }

    /**
     * Acquires a result defining if this TransportTypeConverter can be used to revive the
     * provided instance after transport. The "Revival" process simply implies converting
     * the TransportType instance back to its original class, type and state.
     *
     * @param nonNullInstance The non-null instance to check for revival capability.
     * @return {@code true} if this TransportTypeConverter can revive the provided
     *         instance from a transport form and {@code false} otherwise.
     */
    protected abstract boolean canReviveInstance(Object nonNullInstance);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return compareTo(obj) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object that) {
        return that != null && this.getClass().equals(that.getClass()) ? 0 : -1;
    }
}
