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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.api.NamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Account;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Beverage;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Foo;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbXmlBinderTest {

    // Shared state
    final JaxbXmlBinder unitUnderTest = new JaxbXmlBinder();

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamespacePrefixResolver() {

        // Act & Assert
        new JaxbXmlBinder(null);
    }

    @Test
    public void validateMarshallingToXml() throws Exception {

        // Assemble
        final Person person1 = new Person("Lennart", 44);
        final Person person2 = new Person("Malin", 478);
        final String expected = XmlTestUtils.readFully("data/xml/simpleMarshalling.xml");

        // Act
        final String result = unitUnderTest.marshal(person1, person2);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingWithTypeConversion() throws Exception {

        // Assemble
        final Person person1 = new Person("Lennart", 44);
        final Person person2 = new Person("Malin", 478);
        final String expected = XmlTestUtils.readFully("data/xml/marshallingWithTypeConversion.xml");

        // Act
        final String result = unitUnderTest.marshal(person1, "FooBar!", null, person2);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshallingFromXml() {

        // Assemble
        final List<Object> expected = new ArrayList<Object>();
        expected.add(new Person("Lennart", 44));
        expected.add("FooBar!");
        expected.add(null);
        expected.add(new Person("Malin", 478));

        final String data = "data/xml/marshallingWithTypeConversion.xml";
        final Reader input = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(data)));

        // Act
        final List<Object> result = unitUnderTest.unmarshal(input);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullReader() {

        // Act & Assert
        unitUnderTest.unmarshal(null);
    }

    @Test
    public void validateDefaultNamespacePrefixResolver() {

        // Act
        final NamespacePrefixResolver resolver = unitUnderTest.getNamespacePrefixResolver();

        // Assert
        Assert.assertNotNull(resolver);
        Assert.assertTrue(resolver instanceof JaxbNamespacePrefixResolver);
    }

    @Test
    public void validateMarshallingWithSeparateNamespaces() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("data/xml/compoundNamespaceEntities.xml");
        final NamespacePrefixResolver resolver = unitUnderTest.getNamespacePrefixResolver();
        resolver.put("http://some/good/beverage", "drink");
        resolver.put("http://www.jguru.se/foo", "foo");

        final Beverage ale = new Beverage("Chimay Bleue");
        final Foo aFoo = new Foo("Bar!");
        final Person person = new Person("Lennart", 42);

        // Act
        final String result = unitUnderTest.marshal(ale, "FooBar!", person, aFoo);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(data, result).identical());
    }

    @Test
    public void validateUnmarshallingSingleInstance() {

        // Assemble
        final String data = "data/xml/simpleUnmarshalling.xml";
        final Reader input = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(data)));

        // Act
        final Account result = unitUnderTest.unmarshalInstance(input);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals("SavingsAccount", result.getName());
        Assert.assertEquals(42.42, result.getBalance(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnmarshallingInstanceYieldingMoreThanOneInstance() {

        // Assemble
        final String data = "data/xml/unmarshallingWithTypeConversion.xml";
        final Reader input = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(data)));

        // Act & Assert
        unitUnderTest.unmarshalInstance(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnmarshallingANonEntityTransporterInstance() {

        // Assemble
        final String data = "data/xml/notAnEntityTransporter.xml";
        final Reader input = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(data)));

        // Act & Assert
        unitUnderTest.unmarshalInstance(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnmarshallingIncorrectlyFormedXmlData() {

        // Assemble
        final String data = "data/xml/incorrectXmlData.xml";
        final Reader input = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(data)));

        // Act & Assert
        unitUnderTest.unmarshal(input);
    }
}
