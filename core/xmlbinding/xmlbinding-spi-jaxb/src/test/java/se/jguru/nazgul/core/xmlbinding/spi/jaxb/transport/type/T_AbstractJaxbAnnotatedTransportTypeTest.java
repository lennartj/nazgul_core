/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.DefaultJaxbConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequence;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequenceConverter;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractJaxbAnnotatedTransportTypeTest {

    // Shared state
    private JaxbXmlBinder binder;

    @Before
    @After
    public void setupSharedState() {

        binder = new JaxbXmlBinder();
        EntityTransporter.setTransportTypeConverterRegistry(new DefaultJaxbConverterRegistry());
    }

    @Test
    public void validateNoExceptionOnUnknownClassInformationOnPopulation() {

        // Assemble
        final StringBuffer buf = new StringBuffer();
        buf.append("FooBar!");

        final TrivialCharSequence sequence = new TrivialCharSequence(buf);

        // Act & Assert
        binder.marshal(sequence);
    }

    @Test
    public void validateTransportConversionUsingCustomConverter() {

        // Assemble
        final String expected = XmlTestUtils.readFully("data/xml/marshalledTrivialCharSequence.xml");
        EntityTransporter.getRegistry().addConverters(new TrivialCharSequenceConverter());

        final StringBuffer buf = new StringBuffer();
        buf.append("FooBar!");

        final TrivialCharSequence sequence = new TrivialCharSequence(buf);

        // Act
        final String result = binder.marshal(sequence);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }
}
