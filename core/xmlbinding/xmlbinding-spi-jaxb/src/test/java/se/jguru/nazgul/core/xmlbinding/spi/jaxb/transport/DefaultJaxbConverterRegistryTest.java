/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedDateTime;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class DefaultJaxbConverterRegistryTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private JaxbXmlBinder binder;

    private String fooBar = "FooBar!";
    private DateTime firstAprilThreePm = new DateTime(2012, 4, 1, 15, 0, DateTimeZone.UTC);
    private int meaningOfLife = 42;

    private List<Object> data1D;
    private List<Object> data2D;
    private DefaultJaxbConverterRegistry unitUnderTest;

    @Override
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

        // Act & Assert
        for(Map.Entry<Object, Class<? extends Serializable>> current : expectedTransportTypeMap.entrySet()) {

            final Class<?> expectedType = expectedTransportTypeMap.get(current.getKey());
            final Class<?> actualType = unitUnderTest.getTransportType(current.getKey().getClass());

            Assert.assertEquals("JaxbConverterRegistry: " + unitUnderTest,
                    expectedType, actualType);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnGettingOriginalTypeUsingNonAnnotatedTransportType() {

        // Act & Assert
        unitUnderTest.getOriginalType(StringBuffer.class);
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
    public void validateNormalConversionWith2dCollection() throws Exception {

        // Assemble
        final String expectedResult = XmlTestUtils.readFully("data/xml/marshalled2dCollection.xml");

        // Act
        final JaxbAnnotatedCollection transportForm = unitUnderTest.packageForTransport(data2D);
        final String result = binder.marshal(transportForm);

        // Assert
        Assert.assertNotNull("Received null transportForm:\n" + unitUnderTest.toString(), transportForm);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expectedResult, result).identical());
    }

    @Test
    public void validateNormalConversionWith1dCollection() throws Exception {

        // Assemble
        final String expectedResult = XmlTestUtils.readFully("data/xml/marshalled1dCollection.xml");

        // Act
        final JaxbAnnotatedCollection transportForm = unitUnderTest.packageForTransport(data1D);
        final String result = binder.marshal(transportForm);

        // Assert
        Assert.assertNotNull("Received null transportForm:\n" + unitUnderTest.toString(), transportForm);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expectedResult, result).identical());
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
}
