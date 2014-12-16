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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.DefaultJaxbConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequence;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequenceConverter;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractJaxbAnnotatedTransportTypeTest {

    // Shared state
    private JaxbXmlBinder binder;

    @Before
    @After
    public void setupSharedState() {

        binder = new JaxbXmlBinder();
        EntityTransporter.setTransportTypeConverterRegistry(new DefaultJaxbConverterRegistry());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnknownClassInformationOnPopulation() {

        // Assemble
        final StringBuffer buf = new StringBuffer();
        buf.append("FooBar!");

        final TrivialCharSequence sequence = new TrivialCharSequence(buf);

        // Act & Assert
        binder.marshal(sequence);
    }

    @Test
    public void validateTransportConversionUsingCustomConverter() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("data/xml/marshalledTrivialCharSequence.xml");
        EntityTransporter.getRegistry().addConverters(new TrivialCharSequenceConverter());

        final StringBuffer buf = new StringBuffer();
        buf.append("FooBar!");

        final TrivialCharSequence sequence = new TrivialCharSequence(buf);

        // Act
        final String result = binder.marshal(sequence);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }
}
