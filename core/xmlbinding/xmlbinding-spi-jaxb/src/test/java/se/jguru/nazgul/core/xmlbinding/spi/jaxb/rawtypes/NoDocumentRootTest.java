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

import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Beverage;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Foo;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NoDocumentRootTest {

    private static final String EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY = "com.sun.xml.bind.namespacePrefixMapper";

    // Shared state
    private Marshaller marshaller;

    @Before
    public void setupSharedState() throws Exception {

        final JAXBContext ctx = JAXBContext.newInstance(Foo.class,
                Person.class,
                Beverage.class,
                JaxbAnnotatedCollection.class,
                ArrayList.class);
        marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final JaxbNamespacePrefixResolver resolver = new JaxbNamespacePrefixResolver();
        resolver.put("http://some/good/beverage", "beverage");
        resolver.put("http://www.jguru.se/foo", "foo");
        resolver.put("http://some.com/foo/bar", "foobar");
        resolver.put("http://jguru.se/examples/jaxb/multiple", "multiple");
        marshaller.setProperty(EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY, resolver);
    }

    @Test
    public void validateMarshallingJaxbElement() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/jaxbElementFoo.xml";
        final Foo aFoo = new Foo("someName");
        final QName qName = new QName("http://some.com/foo/bar", "fooRoot", "fooPrefix");
        final JAXBElement<Foo> fooElement = new JAXBElement<>(qName, Foo.class, aFoo);

        // Act
        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(fooElement, resultWriter);

        // Assert
        // System.out.println("Got:\n" + resultWriter.toString());
        final String expected = XmlTestUtils.readFully(resourcePath);
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(resultWriter.toString(), expected);
        Assert.assertTrue("Got non-trivial diff: "
                + XmlTestUtils.getXPathLocationToDifferenceMap(diff), diff.identical());
    }

    @Test
    public void validateMarshallingArbitrarySequenceUsingJaxbElements() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/jaxbArbitrarySequence.xml";
        final QName rootElementQName = new QName(
                "http://jguru.se/examples/jaxb/multiple",
                "sampleSequence",
                "first");

        final List<Object> innerCollection = new ArrayList<Object>();
        innerCollection.add(new Beverage("Beer!"));
        innerCollection.add(new Foo("bar"));
        innerCollection.add(new Beverage("More Beer!"));

        final JaxbAnnotatedCollection<List<Object>> annotatedList =
                new JaxbAnnotatedCollection<List<Object>>(innerCollection);

        final JAXBElement<JaxbAnnotatedCollection<List<Object>>> rootElement = new
                JAXBElement<JaxbAnnotatedCollection<List<Object>>>(
                rootElementQName,
                (Class<JaxbAnnotatedCollection<List<Object>>>) annotatedList.getClass(),
                annotatedList);

        // Act
        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(rootElement, resultWriter);

        // Assert
        // System.out.println("Got: \n\n\n" + resultWriter.toString());
        Assert.assertTrue(
                XmlTestUtils.compareXmlIgnoringWhitespace(
                        resultWriter.toString(),
                        XmlTestUtils.readFully(resourcePath)).identical());
    }
}
