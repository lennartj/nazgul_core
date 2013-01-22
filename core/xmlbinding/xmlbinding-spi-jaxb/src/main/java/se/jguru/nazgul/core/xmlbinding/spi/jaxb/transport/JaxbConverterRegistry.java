/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Usage specification for a registry holding JAXB converters.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface JaxbConverterRegistry {

    /**
     * Adds the supplied converters to this DefaultJaxbConverterRegistry.
     *
     * @param converters The converter instances to add.
     * @throws IllegalArgumentException if any of the supplied converters had no
     *                                  Converter-annotated methods or constructors.
     */
    void addConverters(final Object... converters) throws IllegalArgumentException;

    /**
     * Acquires the TransportType for the supplied originalType.
     *
     * @param originalType    The original (i.e. non-transport) type for which we want to acquire
     *                        the corresponding TransportType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The Class of the TransportType for the supplied OriginalType.
     */
    <TransportType, OriginalType> Class<TransportType> getTransportType(Class<OriginalType> originalType);

    /**
     * Acquires the OriginalType for the supplied transportType.
     *
     * @param transportType   The [JAXB annotated] TransportType type for which we want to acquire
     *                        the corresponding OriginalType.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The type previously converted to the supplied TransportType.
     * @return The Class of the OriginalType for the supplied TransportType.
     * @throws IllegalArgumentException if the transportType was {@code JaxbAnnotatedNull}.
     */
    <OriginalType, TransportType> Class<OriginalType> getOriginalType(Class<TransportType> transportType)
            throws IllegalArgumentException;

    /**
     * Converts the provided instance to a transport type, ready for transmission in serialized form.
     *
     * @param source          The object to convert.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The converted object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    <OriginalType, TransportType> TransportType packageForTransport(OriginalType source)
            throws IllegalArgumentException;

    /**
     * Converts the provided transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert       The transport type to be converted.
     * @param <TransportType> The JAXB-annotated TransportType corresponding to the supplied originalType;
     * @param <OriginalType>  The OriginalType for which we would like to acquire the JAXB-annotated TransportType.
     * @return The resurrected object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    <OriginalType, TransportType> OriginalType resurrectAfterTransport(TransportType toConvert)
            throws IllegalArgumentException;
}
