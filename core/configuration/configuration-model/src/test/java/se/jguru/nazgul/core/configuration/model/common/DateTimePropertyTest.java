/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
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
package se.jguru.nazgul.core.configuration.model.common;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DateTimePropertyTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private DateTime dateTime1;
    private DateTime dateTime2;

    @Before
    public void setupSharedState() {
        dateTime1 = new DateTime(2013, 5, 6, 7, 8, 9);
        dateTime2 = new DateTime(2013, 6, 7, 8, 9, 10);
    }

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullKey() {

        // Act & Assert
        new DateTimeProperty(null, dateTime1);
    }

    @Test
    public void validateNoExceptionOnNullValue() {

        // Assemble
        final String key = "aKey";

        // Act
        final DateTimeProperty unitUnderTest = new DateTimeProperty(key, null);

        // Assert
        Assert.assertNull(unitUnderTest.getValue());
        Assert.assertEquals(unitUnderTest.getKey(), key);
        Assert.assertEquals(DateTime.class, unitUnderTest.getValueType());
    }

    @Test
    public void validateEquality() {

        // Assemble
        final DateTimeProperty fooBar = new DateTimeProperty("foo", dateTime1);
        final DateTimeProperty fooBaz = new DateTimeProperty("foo", dateTime2);
        final DateTimeProperty anotherFooBar = new DateTimeProperty("foo", dateTime1);
        final DateTimeProperty nullValueProperty = new DateTimeProperty("foo", null);

        // Act & Assert
        Assert.assertFalse(fooBar.equals(null));
        Assert.assertFalse(fooBar.equals(""));
        Assert.assertFalse(fooBar.equals(nullValueProperty));
        Assert.assertFalse(nullValueProperty.equals(fooBar));
        Assert.assertTrue(fooBar.equals(fooBar));
        Assert.assertFalse(fooBar.equals(fooBaz));
        Assert.assertTrue(fooBar.equals(anotherFooBar));

        Assert.assertEquals(fooBar.hashCode(), anotherFooBar.hashCode());
        Assert.assertNotEquals(fooBar.hashCode(), fooBaz.hashCode());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final DateTimeProperty unitUnderTest = new DateTimeProperty("aDateTimePropertyKey", dateTime1);
        final String expected = XmlTestUtils.readFully("testdata/aDateTimeProperty.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Result: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final DateTimeProperty unitUnderTest = new DateTimeProperty("aDateTimePropertyKey", dateTime1);
        final String data = XmlTestUtils.readFully("testdata/aDateTimeProperty.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final List<?> unmarshalled = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        final DateTimeProperty emp = (DateTimeProperty) unmarshalled.get(0);
        emp.validateEntityState();

        // System.out.println("Got: " + emp);
        Assert.assertTrue(unitUnderTest.equals(emp));
        Assert.assertEquals(unitUnderTest, emp);
        Assert.assertEquals(DateTime.class, unitUnderTest.getValueType());

        // Act & Assert #2: Mutability
        unitUnderTest.setValue(dateTime2);
        Assert.assertNotEquals(unitUnderTest, emp);
    }
}
