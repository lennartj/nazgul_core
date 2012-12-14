package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedDateTime;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DefaultJaxbConverterRegistryTest {

    // Shared state
    private JaxbXmlBinder binder;

    private String fooBar = "FooBar!";
    private DateTime firstAprilThreePm = new DateTime(2012, 4, 1, 15, 0);
    private int meaningOfLife = 42;

    private List<Object> data1D;
    private List<Object> data2D;

    private List<? extends Class<? extends Serializable>> standardTransportTypes = Arrays.asList(
            JaxbAnnotatedNull.class, JaxbAnnotatedCollection.class,
            JaxbAnnotatedDateTime.class, JaxbAnnotatedString.class);
    private List<Object> sourceObjects = Arrays.asList(null, data2D, firstAprilThreePm, fooBar);

    private DefaultJaxbConverterRegistry unitUnderTest;

    @Before
    public void setupSharedState() {

        // Create the xml binder.
        binder = new JaxbXmlBinder();

        // Create the registry
        unitUnderTest = new DefaultJaxbConverterRegistry();

        // Create the data structures
        data1D = new ArrayList<Object>();
        data2D = new LinkedList<Object>();

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
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSourceType() {

        // Act & Assert
        unitUnderTest.getTransportType(null);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullTransportType() {

        // Act & Assert
        unitUnderTest.getOriginalType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnCheckingOriginalTypeForJaxbAnnotatedNull() {

        // Act & Assert
        unitUnderTest.getOriginalType(JaxbAnnotatedNull.class);
    }

    @Test
    public void validateStandardConverterTypes() {

        // Assemble
        final Map<Object, Class<? extends Serializable>> expectedTransportTypeMap =
                new HashMap<Object, Class<? extends Serializable>>();

        expectedTransportTypeMap.put(data2D, JaxbAnnotatedCollection.class);
        expectedTransportTypeMap.put(firstAprilThreePm, JaxbAnnotatedDateTime.class);
        expectedTransportTypeMap.put(fooBar, JaxbAnnotatedString.class);

        // Act & Assert
        for(Object current : expectedTransportTypeMap.keySet()) {

            final Class<?> expectedType = expectedTransportTypeMap.get(current);
            final Class<?> actualType = unitUnderTest.getTransportType(current.getClass());

            Assert.assertEquals("JaxbConverterRegistry: " + unitUnderTest,
                    expectedType, actualType);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnGettingOriginalTypeUsingNonAnnotatedTransportType() {

        // Act & Assert
        unitUnderTest.getOriginalType(String.class);
    }

    @Test
    public void validateExceptionOnGettingTransportTypeUsingAnnotatedOriginalType() {

        // Assemble
        final Class<JaxbAnnotatedDateTime> dtc = JaxbAnnotatedDateTime.class;

        // Act
        final Class<?> transportType = unitUnderTest.getTransportType(dtc);

        // Assert
        Assert.assertSame(transportType, dtc);

    }

    @Test
    public void validateNullValueOnUnknownOriginalType() {

        // Act
        final Class<?> result = unitUnderTest.getTransportType(SimpleDateFormat.class);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateNormalConversionWith2dCollection() {

        // Assemble
        final String expectedResult = readFully("data/xml/marshalled2dCollection.xml");

        // Act
        final JaxbAnnotatedCollection transportForm = unitUnderTest.packageForTransport(data2D);
        final String result = binder.marshal(transportForm);

        // Assert
        Assert.assertNotNull("Received null transportForm:\n" + unitUnderTest.toString(), transportForm);
        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void validateNormalConversionWith1dCollection() {

        // Assemble
        final String expectedResult = readFully("data/xml/marshalled1dCollection.xml");

        // Act
        final JaxbAnnotatedCollection transportForm = unitUnderTest.packageForTransport(data1D);
        final String result = binder.marshal(transportForm);

        // Assert
        Assert.assertNotNull("Received null transportForm:\n" + unitUnderTest.toString(), transportForm);
        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void validateNullValuesYieldJaxbAnnotatedNullTransportType() {

        // Act
        final Object o = unitUnderTest.packageForTransport(null);

        // Assert
        Assert.assertSame(JaxbAnnotatedNull.getInstance(), o);
    }

    @Test
    public void validateUnregisteredOriginalTypeYieldsSameTransportType() {

        // Assemble
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        // Act
        final Object result = unitUnderTest.packageForTransport(simpleDateFormat);

        // Assert
        Assert.assertSame(simpleDateFormat, result);
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
