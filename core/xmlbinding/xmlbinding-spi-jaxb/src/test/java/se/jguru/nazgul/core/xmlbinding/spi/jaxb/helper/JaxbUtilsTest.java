/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Account;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Beverage;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.ThreePartCereal;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.lang.reflect.Field;
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
        final JAXBContext jaxbContext = JaxbUtils.getJaxbContext(transporter);
        final Schema schema = JaxbUtils.generateTransientXSD(jaxbContext, defaultResolver);
        final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(jaxbContext,
                defaultResolver, defaultResolver, true);

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
        final JAXBContext ctx1 = JaxbUtils.getJaxbContext(transporter1);
        final JAXBContext ctx2 = JaxbUtils.getJaxbContext(transporter2);
        final JAXBContext ctx3 = JaxbUtils.getJaxbContext(transporter3);

        // Assert
        Assert.assertSame(ctx1, ctx2);
        Assert.assertSame(ctx1, ctx3);
    }

    @Test
    public void validateGenerateTransientXSDs() {

        // Assemble
        final ThreePartCereal cereal = new ThreePartCereal("barley", "strawberry", "blueberry", 3, 4);
        final EntityTransporter<ThreePartCereal> transporter = new EntityTransporter<ThreePartCereal>(cereal);
        final JAXBContext context = JaxbUtils.getJaxbContext(transporter);
        final JaxbNamespacePrefixResolver resolver = new JaxbNamespacePrefixResolver();

        // Act
        final Schema result = JaxbUtils.generateTransientXSD(context, resolver);
        final Validator schemaValidator = result.newValidator();

        // Assert
        System.out.println("Got: " + result);
    }
}
