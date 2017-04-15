/*
 * #%L
 * Nazgul Project: nazgul-core-configuration-model
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.configuration.model.common;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.configuration.model.helpers.MockProperty;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.io.StringReader;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringPropertyTest {

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullKey() {

        // Act & Assert
        new StringProperty(null, "irrelevant");
    }

    @Test
    public void validateNoExceptionOnNullValue() {

        // Assemble
        final String aValue = null;
        final String key = "aKey";

        // Act
        final StringProperty unitUnderTest = new StringProperty(key, aValue);

        // Assert
        Assert.assertNull(unitUnderTest.getValue());
        Assert.assertEquals(unitUnderTest.getKey(), key);
        Assert.assertEquals(String.class, unitUnderTest.getValueType());
    }

    @Test
    public void validateEquality() {

        // Assemble
        final StringProperty fooBar = new StringProperty("foo", "bar");
        final StringProperty fooBaz = new StringProperty("foo", "baz");
        final StringProperty anotherFooBar = new StringProperty("foo", "bar");
        final StringProperty aNullProp = new StringProperty("nullValue", null);
        final StringProperty anotherNullProp = new StringProperty("nullValue", null);
        final StringProperty anUnequalNullProp = new StringProperty("well...", null);

        // Act & Assert
        Assert.assertFalse(fooBar.equals(null));
        Assert.assertFalse(fooBar.equals(new Date()));
        Assert.assertTrue(aNullProp.equals(anotherNullProp));
        Assert.assertFalse(aNullProp.equals(anUnequalNullProp));
        Assert.assertTrue(fooBar.equals(fooBar));
        Assert.assertFalse(fooBar.equals(fooBaz));
        Assert.assertTrue(fooBar.equals(anotherFooBar));

        Assert.assertEquals(fooBar.hashCode(), anotherFooBar.hashCode());
        Assert.assertNotEquals(fooBar.hashCode(), fooBaz.hashCode());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final StringProperty unitUnderTest = new StringProperty("key", "value");
        final String expected = XmlTestUtils.readFully("testdata/aStringProperty.xml");

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
        final StringProperty unitUnderTest = new StringProperty("key", "value");
        final String data = XmlTestUtils.readFully("testdata/aStringProperty.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final List<?> unmarshalled = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        final StringProperty emp = (StringProperty) unmarshalled.get(0);
        emp.validateEntityState();

        // System.out.println("Got: " + emp);
        Assert.assertEquals(unitUnderTest, emp);
        Assert.assertEquals(String.class, unitUnderTest.getValueType());

        // Act & Assert #2: Mutability
        unitUnderTest.setValue("FooBar!");
        Assert.assertNotEquals(unitUnderTest, emp);
    }

    @Test
    public void validateLoadingValueType() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/aStringProperty.xml");
        final JaxbXmlBinder binder = new JaxbXmlBinder();
        final StringProperty unmarshalled = (StringProperty) binder.unmarshal(new StringReader(data)).get(0);

        // Act
        final MockProperty unitUnderTest1 = new MockProperty(String.class.getName(), false);
        final Class<String> valueType = unmarshalled.getValueType();

        // Assert
        Assert.assertNotNull(valueType);
        Assert.assertEquals(String.class, unitUnderTest1.getValueType());
    }
}
