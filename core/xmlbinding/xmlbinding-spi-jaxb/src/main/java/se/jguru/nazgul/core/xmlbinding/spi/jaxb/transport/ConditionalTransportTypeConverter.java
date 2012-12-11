/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Type converter condition definition to check if instances can be packaged
 * within JAXB transport types - and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ConditionalTransportTypeConverter {

    /**
     * Acquires a result defining if this ConditionalTransportTypeConverter can
     * be used to package the provided instance for [Serialized] transport.
     * The "Package" process simply implies converting the provided instance
     * into a transportType instance holding the state of the original instance.
     *
     * @param instance The instance for which to check packaging capability.
     * @return <code>true</code> if this ConditionalTransportTypeConverter can
     *         package the provided instance for transport and <code>false</code> otherwise.
     */
    boolean canPackageForTransport(Object instance);

    /**
     * Acquires a result defining if this ConditionalTransportTypeConverter can be used to revive the
     * provided instance after transport. The "Revival" process simply implies converting
     * the TransportType instance back to its original class, type and state.
     *
     * @param instance The instance to revive.
     * @return <code>true</code> if this ConditionalTransportTypeConverter can revive the provided
     *         instance from a transport form and <code>false</code> otherwise.
     */
    boolean canReviveAfterTransport(Object instance);
}
