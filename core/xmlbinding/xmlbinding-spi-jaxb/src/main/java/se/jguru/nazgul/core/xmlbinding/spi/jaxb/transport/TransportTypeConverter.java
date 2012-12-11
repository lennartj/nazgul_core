/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

/**
 * Type converter definition to package instances within
 * transport types for the purposes of marshalling using an XML binder - and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TransportTypeConverter<OriginalType, TransportType> extends ConditionalTransportTypeConverter {

    /**
     * Converts the provided instance to a transport type, ready for transmission in serialized form.
     *
     * @param toConvert The original type to be converted to transport form.
     * @return A transport type.
     */
    TransportType packageForTransport(OriginalType toConvert);

    /**
     * Converts the provided transport type instance (back) to its original type, ready for normal use.
     *
     * @param toConvert The transport type to be converted.
     * @return A clone of the OriginalType's instance.
     */
    OriginalType reviveAfterTransport(TransportType toConvert);
}
