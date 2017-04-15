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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.enumerations;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.ls.LSResourceResolver;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;
import java.io.StringReader;
import java.util.Locale;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TextSectionTest {

    // Shared state
    private JaxbXmlBinder binder;
    private JaxbNamespacePrefixResolver resolver;
    private Locale originalDefault;

    @Before
    public void setupSharedState() throws Exception {

        resolver = new JaxbNamespacePrefixResolver();
        resolver.put("http://some.other.namespace", "someOther");

        binder = new JaxbXmlBinder(resolver);
        originalDefault = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void teardownSharedState() {
        Locale.setDefault(originalDefault);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final TextSection unitUnderTest = new TextSection(Adjustment.LEFT, "this is a foo");
        final String expected = XmlTestUtils.readFully("data/xml/textSection.xml");

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final TextSection expected = new TextSection(Adjustment.LEFT, "this is a foo");
        final String data = XmlTestUtils.readFully("data/xml/textSection.xml");

        // Act
        final TextSection result = binder.unmarshalInstance(new StringReader(data));

        // Assert
        Assert.assertNotSame(expected, result);
        Assert.assertEquals(expected.getAdjustment(), result.getAdjustment());
        Assert.assertEquals(expected.getText(), result.getText());
    }

    @Test
    public void validateGeneratingTransientSchemaForEnumType() throws Exception {

        // Assemble
        final Adjustment unitUnderTest = Adjustment.MIDDLE;
        final EntityTransporter<Adjustment> wrapper = new EntityTransporter<Adjustment>(unitUnderTest);
        final JAXBContext context = JaxbUtils.getJaxbContext(wrapper, true);

        // Act
        final Tuple<Schema, LSResourceResolver> tuple = JaxbUtils.generateTransientXSD(context);

        // Assert
        Assert.assertNotNull(tuple.getKey());
        Assert.assertNotNull(tuple.getValue());
    }
}
