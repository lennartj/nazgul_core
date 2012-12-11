/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Metadata information specification for JAXB transport type pairs, indicating
 * which TransportType corresponds to a particular OriginalType.
 * The Metadata information implies that an OriginalType instance is converted
 * to a TransportType instance for JAXB serialization, and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public interface TransportMetaData {

    /**
     * Acquires the OriginalType for the supplied TransportType.
     *
     * @param transportType   The JAXB-annotated TransportType corresponding to the supplied for which we want
     *                        to acquire
     *                        the corresponding TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    public <OriginalType, TransportType> Class<OriginalType> getOriginalType(final Class<TransportType> transportType);

    /**
     * Acquires the TransportType for the supplied originalType.
     *
     * @param originalType    The original (i.e. non-transport) type for which we want to acquire
     *                        the corresponding TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    public <TransportType, OriginalType> Class<TransportType> getTransportType(final Class<OriginalType> originalType);
}
