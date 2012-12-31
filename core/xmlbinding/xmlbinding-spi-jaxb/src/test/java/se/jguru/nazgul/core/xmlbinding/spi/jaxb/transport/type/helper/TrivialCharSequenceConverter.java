/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper;

import se.jguru.nazgul.core.reflection.api.conversion.Converter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TrivialCharSequenceConverter {

    @Converter
    public JaxbAnnotatedTrivialCharSequence convert(final TrivialCharSequence trivialCharSequence) {
        return new JaxbAnnotatedTrivialCharSequence(trivialCharSequence);
    }

    @Converter(priority = Converter.DEFAULT_PRIORITY + 100)
    public TrivialCharSequence convertToTrivialCharSequence(final JaxbAnnotatedTrivialCharSequence transport) {
        return transport.getValue();
    }
}
