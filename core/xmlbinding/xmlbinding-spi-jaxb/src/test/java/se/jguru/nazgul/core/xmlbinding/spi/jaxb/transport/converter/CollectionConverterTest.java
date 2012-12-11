package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.joda.time.DateTime;
import org.junit.Assert;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class CollectionConverterTest {

    // Shared state
    private TransportTypeConverterRegistry defaultRegistry;
    private JaxbXmlBinder binder;

    private String fooBar = "FooBar!";
    private DateTime firstAprilThreePm = new DateTime(2012, 4, 1, 15, 0);
    private int meaningOfLife = 42;

    private List<Object> data1D;
    private List<Object> data2D;
    private List<Object> data3D;

    @Before
    public void setupSharedState() {

        binder = new JaxbXmlBinder();
        defaultRegistry = new DefaultTransportTypeConverterRegistry();
        EntityTransporter.setTransportTypeConverterRegistry(defaultRegistry);
        defaultRegistry.addTransportTypeConverter(new CollectionConverter());

        // Create the data structures
        data1D = new ArrayList<Object>();
        data2D = new LinkedList<Object>();
        data3D = new ArrayList<Object>();

        // data1D
        data1D.add(fooBar);
        data1D.add(firstAprilThreePm);
        data1D.add(meaningOfLife);

        // data2D
        data2D.add(fooBar);
        data2D.add(firstAprilThreePm);
        data2D.add(data1D);
        data2D.add(meaningOfLife);
        data2D.add(null);

        // data3d
        data3D.add(fooBar);
        data3D.add(data2D);
    }

    @Test
    public void validate1dUnmarshalling() {

        // Assemble
        final String marshalled = readFully("data/xml/marshalled1dCollection.xml");

        // Act
        final List<Object> result = binder.unmarshal(new StringReader(marshalled));

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(data1D, result.get(0));
    }

    @Test
    public void validate1dMarshalling() {

        // Assemble
        final String expected = readFully("data/xml/marshalled1dCollection.xml");

        // Act
        final String marshalled = binder.marshal(data1D);

        // Assert
        Assert.assertEquals(expected, marshalled);
    }

    @Test
    public void validate3dUnmarshalling() {

        // Assemble
        final String marshalled = readFully("data/xml/marshalled3dCollection.xml");

        // Act
        final List<Object> result = binder.unmarshal(new StringReader(marshalled));

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(data3D, result.get(0));
    }

    @Test
    public void validate3dMarshalling() {

        // Assemble
        final String expected = readFully("data/xml/marshalled3dCollection.xml");

        // Act
        final String marshalled = binder.marshal(data3D);

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
