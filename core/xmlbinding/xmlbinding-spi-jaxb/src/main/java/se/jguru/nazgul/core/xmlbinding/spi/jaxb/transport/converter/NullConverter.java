/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

/**
 * Converter for {@code null} values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NullConverter implements TransportTypeConverter<Object, JaxbAnnotatedNull>, Comparable {

    /**
     * Acquires a result defining if this TransportTypeConverter can
     * be used to package the provided instance for [Serialized] transport.
     * The "Package" process simply implies converting the provided instance
     * into a transportType instance holding the state of the original instance.
     *
     * @param instance The instance for which to check packaging capability.
     * @return <code>true</code> if this TransportTypeConverter can
     *         package the provided instance for transport and <code>false</code> otherwise.
     */
    @Override
    public boolean canPackageForTransport(final Object instance) {
        return instance == null;
    }

    /**
     * Acquires a result defining if this TransportTypeConverter can be used to revive the
     * provided instance after transport. The "Revival" process simply implies converting
     * the TransportType instance back to its original class, type and state.
     *
     * @param instance The instance to revive.
     * @return <code>true</code> if this TransportTypeConverter can revive the provided
     *         instance from a transport form and <code>false</code> otherwise.
     */
    @Override
    public boolean canReviveAfterTransport(final Object instance) {
        return instance instanceof JaxbAnnotatedNull;
    }

    /**
     * Converts the provided, non-null, instance to a transport type, ready for transmission in serialized form.
     *
     * @param toConvert The original type to be converted to transport form.
     * @return A transport type.
     */
    @Override
    public JaxbAnnotatedNull packageForTransport(final Object toConvert) {
        return JaxbAnnotatedNull.getInstance();
    }

    /**
     * Converts the provided, non-null, transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert The transport type to be converted.
     * @return A clone of the OriginalType's instance.
     */
    @Override
    public Object reviveAfterTransport(final JaxbAnnotatedNull toConvert) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object that) {
        return that != null && that instanceof NullConverter ? 0 : -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return compareTo(obj) == 0;
    }
}
