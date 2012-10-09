/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.DefaultTransportTypeConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportTypeConverterRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DateTimeConverterTest {

    // Shared state
    private TransportTypeConverterRegistry defaultRegistry;
    private JaxbXmlBinder binder;
    private DateTime timestamp = new DateTime(2012, 2, 3, 4, 5);

    @Before
    public void setupSharedState() {

        binder = new JaxbXmlBinder();
        defaultRegistry = new DefaultTransportTypeConverterRegistry();
        EntityTransporter.setTransportTypeConverterRegistry(defaultRegistry);
        defaultRegistry.addTransportTypeConverter(new DateTimeConverter());
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String marshalled = readFully("data/xml/marshalledDateTime.xml");

        // Act
        final List<Object> result = binder.unmarshal(new StringReader(marshalled));

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(timestamp, result.get(0));
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = readFully("data/xml/marshalledDateTime.xml");

        // Act
        final String marshalled = binder.marshal(timestamp);

        // Assert
        Assert.assertEquals(expected, marshalled);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnUnmarshallingIncorrectXml() {

        // Assemble
        final String marshalled = readFully("data/xml/incorrectMarshalledDateTime.xml");

        // Act
        binder.unmarshal(new StringReader(marshalled));
    }

    //
    // Private helpers
    //

    private String readFully(final String path) {

        final InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        final BufferedReader tmp = new BufferedReader(new InputStreamReader(in));
        final StringBuilder toReturn = new StringBuilder();

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }
}
