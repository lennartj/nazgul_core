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
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class IntegerPropertyTest {

    @Test(expected = InternalStateValidationException.class)
    public void validateExceptionOnNullKey() {

        // Act & Assert
        new IntegerProperty(null, 42);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final IntegerProperty unitUnderTest = new IntegerProperty("key", 42);
        final String expected = XmlTestUtils.readFully("testdata/anIntegerProperty.xml");

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
        final IntegerProperty unitUnderTest = new IntegerProperty("key", 42);
        final String data = XmlTestUtils.readFully("testdata/anIntegerProperty.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final List<?> unmarshalled = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        final IntegerProperty emp = (IntegerProperty) unmarshalled.get(0);
        emp.validateEntityState();

        // System.out.println("Got: " + emp);
        Assert.assertEquals(unitUnderTest, emp);
        Assert.assertEquals(Integer.class, unitUnderTest.getValueType());

        // Act & Assert #2: Mutability
        unitUnderTest.setValue(43);
        Assert.assertNotEquals(unitUnderTest, emp);
    }
}
