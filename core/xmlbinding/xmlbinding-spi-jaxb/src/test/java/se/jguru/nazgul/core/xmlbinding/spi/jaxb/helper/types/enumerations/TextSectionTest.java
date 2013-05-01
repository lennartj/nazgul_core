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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.enumerations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;

import javax.xml.bind.JAXBContext;
import javax.xml.validation.Schema;
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
    public void setupSharedState() {

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
    public void validateMarshallingAndUnmarshalling() {

        // Assemble
        final TextSection unitUnderTest = new TextSection(Adjustment.LEFT, "this is a foo");

        // Act
        final String result = binder.marshal(unitUnderTest);

        // Assert
        System.out.println("Got: " + result);
    }

    @Test
    public void validateGeneratingTransientSchemaForEnumType() throws Exception {

        // Assemble
        final Adjustment unitUnderTest = Adjustment.MIDDLE;
        final EntityTransporter<Adjustment> wrapper = new EntityTransporter<Adjustment>(unitUnderTest);
        final JAXBContext context = JaxbUtils.getJaxbContext(wrapper, true);

        // Act
        final Schema schema = JaxbUtils.generateTransientXSD(context).getKey();

        // Assert
    }
}
