/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Account;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.TransportMetaData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
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
}
