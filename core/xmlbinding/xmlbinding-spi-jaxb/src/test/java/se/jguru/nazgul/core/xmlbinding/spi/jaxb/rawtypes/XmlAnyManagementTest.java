/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.rawtypes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Foo;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.rootelements.Age;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.rootelements.Animal;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.rootelements.Legend;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XmlAnyManagementTest {

    private static final String EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY = "com.sun.xml.bind.namespacePrefixMapper";

    // Shared state
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    @Before
    public void setupSharedState() throws Exception {

        final JAXBContext ctx = JAXBContext.newInstance(Foo.class,
                Animal.class,
                Legend.class,
                Age.class,
                JaxbAnnotatedCollection.class,
                ArrayList.class);
        marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final JaxbNamespacePrefixResolver resolver = new JaxbNamespacePrefixResolver();
        resolver.put("http://www.jguru.se/legends", "legends");
        resolver.put("http://some.com/foo/bar", "foobar");
        marshaller.setProperty(EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY, resolver);

        // DomHandler
        unmarshaller = ctx.createUnmarshaller();
    }

    @Test
    public void validateMarshallingTypedXmlAnyObject() throws Exception {

        // Assemble
        final String expected = "data/xml/typedXmlAnyObject.xml";
        final Legend legend = new Legend("Voluspa", new Age(true, 8));

        // Act
        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(legend, resultWriter);

        // Assert
        // System.out.println("Got: " + resultWriter.toString());
        Assert.assertTrue(
                XmlTestUtils.compareXmlIgnoringWhitespace(
                        resultWriter.toString(),
                        XmlTestUtils.readFully(expected)).identical());
    }

    @Test
    public void validateUnmarshallingTypedAnyObject() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/typedXmlAnyObject.xml";
        final String data = XmlTestUtils.readFully(resourcePath);

        // Act
        final Legend result = (Legend) unmarshaller.unmarshal(new StringReader(data));

        // Assert
        Assert.assertEquals("Voluspa", result.getName());

        final Age age = (Age) result.getInfo();
        Assert.assertNotNull(age);
        Assert.assertEquals(8, age.getCentury());
        Assert.assertTrue(age.isApproximate());
    }

    @Test
    public void validateMarshallingSingleObjectContainingXmlAny() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/xmlAnyInJaxbType.xml";

        final Foo aFoo = new Foo("bar");
        final QName qName = new QName("http://some.com/foo/bar", "foo", "fooPrefix");

        final Animal lion = new Animal();
        lion.setName("Lennart");
        lion.setRelatedInfo(new JAXBElement<>(qName, Foo.class, aFoo));

        // Act
        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(lion, resultWriter);

        /*
        <animal xmlns:foobar="http://some.com/foo/bar" xmlns:legends="http://www.jguru.se/legends" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:core="http://www.jguru.se/nazgul/core" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <name>Lennart</name>
            <foobar:foo>
                <name>bar</name>
            </foobar:foo>
        </animal>
         */

        // Assert
        // System.out.println("Got: " + resultWriter.toString());
        Assert.assertTrue(
                XmlTestUtils.compareXmlIgnoringWhitespace(
                        resultWriter.toString(),
                        XmlTestUtils.readFully(resourcePath)).identical());
    }

    @Test
    public void validateUnmarshallingNonLaxXmlAny() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/xmlAnyInJaxbType.xml";
        final String data = XmlTestUtils.readFully(resourcePath);

        // Act
        final Animal result = (Animal) unmarshaller.unmarshal(new StringReader(data));
        final Element theFooElement = (Element) result.getRelatedInfo();
        final Element theNameElement = (Element) theFooElement.getFirstChild();

        // Assert
        Assert.assertEquals("Lennart", result.getName());
        Assert.assertEquals("foo", theFooElement.getLocalName());
        Assert.assertEquals("http://some.com/foo/bar", theFooElement.getNamespaceURI());

        Assert.assertEquals("name", theNameElement.getLocalName());
        Assert.assertEquals("bar", theNameElement.getTextContent());
    }
}
