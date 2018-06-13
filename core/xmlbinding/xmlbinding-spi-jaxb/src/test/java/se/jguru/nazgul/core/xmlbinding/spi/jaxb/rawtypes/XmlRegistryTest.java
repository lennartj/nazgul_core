/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.rawtypes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.objectfactory.Beer;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.objectfactory.BeverageObjectFactory;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.objectfactory.Beverages;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.objectfactory.Soda;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XmlRegistryTest {

    private static final String EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY = "com.sun.xml.bind.namespacePrefixMapper";

    // Shared state
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    @Before
    public void setupSharedState() throws Exception {

        final JAXBContext ctx = JAXBContext.newInstance(
                Beer.class,
                Soda.class,
                Beverages.class,
                BeverageObjectFactory.class);
        marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final JaxbNamespacePrefixResolver resolver = new JaxbNamespacePrefixResolver();
        resolver.put("http://www.jguru.se/beverages", "beverages");
        marshaller.setProperty(EXTERNAL_JAXB_NAMESPACEPREFIXMAPPER_KEY, resolver);

        // DomHandler
        unmarshaller = ctx.createUnmarshaller();
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = "data/xml/xmlRegistryData.xml";
        final Beverages bevs = new Beverages();
        bevs.getCans().add(new Soda("Vira Blåtira", "Blue"));
        bevs.getCans().add(new Soda("Hallonsoda", "Pink"));
        bevs.getCans().add(new Beer("Falcon", "Lager"));
        bevs.getCans().add(new Beer("Westmalle Tripel", "Trappiste"));

        // Act
        final StringWriter resultWriter = new StringWriter();
        marshaller.marshal(bevs, resultWriter);

        // Assert
        // System.out.println("Got: " + resultWriter.toString());
        Assert.assertTrue(
                XmlTestUtils.compareXmlIgnoringWhitespace(
                        resultWriter.toString(),
                        XmlTestUtils.readFully(expected)).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String resourcePath = "data/xml/xmlRegistryData.xml";
        final String data = XmlTestUtils.readFully(resourcePath);

        // Act
        final Beverages value = (Beverages) unmarshaller.unmarshal(new StringReader(data));
        final List<Object> cans = value.getCans();

        // Assert
        Assert.assertEquals(4, cans.size());

        Assert.assertEquals("Vira Blåtira", ((Soda) cans.get(0)).getName());
        Assert.assertEquals("Hallonsoda", ((Soda) cans.get(1)).getName());
        Assert.assertEquals("Falcon", ((Beer) cans.get(2)).getName());
        Assert.assertEquals("Westmalle Tripel", ((Beer) cans.get(3)).getName());
    }
}
