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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Account;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.ThreePartCereal;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.DefaultJaxbConverterRegistry;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.JaxbAnnotatedTrivialCharSequence;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequence;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper.TrivialCharSequenceConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbUtilsTest {

    // Shared state
    final JaxbNamespacePrefixResolver defaultResolver = new JaxbNamespacePrefixResolver();

    @Before
    public void resetJaxbUtilsCache() throws Exception {

        Field cacheField = JaxbUtils.class.getDeclaredField("jaxbContextCache");
        cacheField.setAccessible(true);
        ConcurrentMap<String, JAXBContext> jaxbCache = (ConcurrentMap<String, JAXBContext>) cacheField.get(null);
        jaxbCache.clear();
    }

    @Test
    public void validateAcquiringDefaultInstances() {

        // Assemble
        final EntityTransporter<Object> transporter = new EntityTransporter<Object>();

        // Act
        final JAXBContext jaxbContext = JaxbUtils.getJaxbContext(transporter, true);
        final Schema schema = JaxbUtils.generateTransientXSD(jaxbContext).getKey();
        final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(jaxbContext, defaultResolver, true);

        // Assert
        Assert.assertNotNull(jaxbContext);
        Assert.assertNotNull(schema);
        Assert.assertNotNull(marshaller);
    }

    @Test
    public void validateReuseOfJaxbContext() {

        // Assemble
        final Person lennart = new Person("Lennart", 43);
        final Account account = new Account("savings", 35.50);

        final EntityTransporter<Object> transporter1 = new EntityTransporter<Object>();
        transporter1.addItem(lennart);
        transporter1.addItem(account);

        final EntityTransporter<Object> transporter2 = new EntityTransporter<Object>();
        transporter2.addItem(lennart);

        final EntityTransporter<Object> transporter3 = new EntityTransporter<Object>();
        transporter3.addItem(account);

        // Act
        final JAXBContext ctx1 = JaxbUtils.getJaxbContext(transporter1, true);
        final JAXBContext ctx2 = JaxbUtils.getJaxbContext(transporter2, true);
        final JAXBContext ctx3 = JaxbUtils.getJaxbContext(transporter3, true);

        // Assert
        Assert.assertSame(ctx1, ctx2);
        Assert.assertSame(ctx1, ctx3);
    }

    @Test
    public void validateGenerateTransientXSDs() throws Exception {

        // Assemble
        final ThreePartCereal cereal = new ThreePartCereal("barley", "strawberry", "blueberry", 3, 4);
        final EntityTransporter<ThreePartCereal> transporter = new EntityTransporter<ThreePartCereal>(cereal);
        final JAXBContext context = JaxbUtils.getJaxbContext(transporter, true);
        final Map<String, List<SAXParseException>> schemaValidationErrors =
                new TreeMap<String, List<SAXParseException>>();

        final ErrorHandler loggingSaxErrorHandler = new ErrorHandler() {

            private void addException(final SAXParseException exception, final String key) {
                List<SAXParseException> exceptionList = schemaValidationErrors.get(key);
                if (exceptionList == null) {
                    exceptionList = new ArrayList<SAXParseException>();
                    schemaValidationErrors.put(key, exceptionList);
                }

                exceptionList.add(exception);
            }

            @Override
            public void warning(final SAXParseException exception) throws SAXException {
                addException(exception, "warning");
            }

            @Override
            public void error(final SAXParseException exception) throws SAXException {
                addException(exception, "error");
            }

            @Override
            public void fatalError(final SAXParseException exception) throws SAXException {
                addException(exception, "fatalError");
            }
        };

        // Act
        final Schema result = JaxbUtils.generateTransientXSD(context).getKey();

        final Validator schemaValidator = result.newValidator();
        schemaValidator.setErrorHandler(loggingSaxErrorHandler);

        final Marshaller marshaller = context.createMarshaller();
        marshaller.setSchema(result);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final StringWriter xmlResult = new StringWriter();
        marshaller.marshal(transporter, xmlResult);

        schemaValidator.validate(new StreamSource(new StringReader(xmlResult.toString())));

        // Assert
        Assert.assertEquals(0, schemaValidationErrors.size());
    }

    @Test
    public void validateOnlyTransportTypesAreEmittedAsMoxyChokesOtherwise() {

        // Assemble
        final TrivialCharSequence toWrapForTransport = new TrivialCharSequence(new StringBuffer("FooBar!"));
        final DefaultJaxbConverterRegistry registry = new DefaultJaxbConverterRegistry();
        registry.addConverters(new TrivialCharSequenceConverter());

        final List<Object> transportableObjects = new ArrayList<Object>();
        final SortedSet<String> transportTypes = new TreeSet<String>();

        // Act
        JaxbUtils.extractJaxbTransportData(toWrapForTransport, registry, transportableObjects, transportTypes);

        // Assert
        Assert.assertEquals(1, transportableObjects.size());
        Assert.assertEquals(JaxbAnnotatedTrivialCharSequence.class, transportableObjects.get(0).getClass());

        Assert.assertEquals(1, transportTypes.size());
        Assert.assertEquals(JaxbAnnotatedTrivialCharSequence.class.getName(), transportTypes.iterator().next());
    }
}
