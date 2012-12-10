/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.Foo;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.FooConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.JaxbAnnotatedFoo;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractXmlAdapterTypeConverterTest {

    // Shared state
    private Foo foo;
    private JaxbAnnotatedFoo unitUnderTest;
    private JaxbXmlBinder binder;

    @Before
    public void setupSharedState() {

        foo = new Foo("foobar!");
        unitUnderTest = new JaxbAnnotatedFoo(foo);
        binder = new JaxbXmlBinder();

        // Add a FooConverter to the registry.
        EntityTransporter.getRegistry().addTransportTypeConverter(new FooConverter());
    }

    @Test
    public void validateCreateUsingClassListConstructor() {

        // Assemble
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<core:entityTransporter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:core=\"http://www.jguru.se/nazgul/core\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <entityClasses>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter</entityClass>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.Foo</entityClass>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.JaxbAnnotatedFoo</entityClass>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull</entityClass>\n" +
                "    </entityClasses>\n" +
                "    <items>\n" +
                "        <item xsi:type=\"core:jaxbAnnotatedFoo\">\n" +
                "            <transportForm>foobar!</transportForm>\n" +
                "        </item>\n" +
                "    </items>\n" +
                "</core:entityTransporter>\n";

        final List<String> classInfo = Arrays.asList(
                Foo.class.getName(), JaxbAnnotatedFoo.class.getName(), JaxbAnnotatedNull.class.getName());
        final JaxbAnnotatedFoo unitUnderTest = new JaxbAnnotatedFoo(new TreeSet<String>(classInfo));
        unitUnderTest.setTransportForm(foo.getValue());

        // Act
        final String result = binder.marshal(unitUnderTest);

        // Assert
        Assert.assertEquals(expected, result);
    }
}
