/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.NullConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedString;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTransportTypeConverterRegistryTest {

    // Shared state
    final DefaultTransportTypeConverterRegistry unitUnderTest = new DefaultTransportTypeConverterRegistry();

    @Test
    public void validateDefaultOperation() {

        // Assemble
        final String value1 = "FooBar!";
        final StringBuffer value2 = new StringBuffer("Irrelevant");

        // Act
        final TransportTypeConverter stringTypeConverter = unitUnderTest.getPackagingTransportTypeConverter(value1);
        final TransportTypeConverter stringBufferTypeConverter =
                unitUnderTest.getPackagingTransportTypeConverter(value2);
        final TransportTypeConverter jaxbAnnotatedStringConverter =
                unitUnderTest.getRevivingTypeConverter(new JaxbAnnotatedString(value1));
        final TransportTypeConverter stringBufferTypeReviveConverter = unitUnderTest.getRevivingTypeConverter(value2);

        // Assert
        Assert.assertNotNull(stringTypeConverter);
        Assert.assertNull(stringBufferTypeConverter);
        Assert.assertNotNull(jaxbAnnotatedStringConverter);
        Assert.assertNull(stringBufferTypeReviveConverter);
    }

    @Test
    public void validateNotAddingTypeConverterTwice() {

        // Assemble
        final List<TransportTypeConverter> list = getTransportConverterList(unitUnderTest);
        final int defaultSize = list.size();

        // Act
        unitUnderTest.addTransportTypeConverter(new NullConverter());

        // Assert
        Assert.assertEquals(defaultSize, list.size());
    }

    //
    // Private helpers
    //

    private List<TransportTypeConverter> getTransportConverterList(
            final DefaultTransportTypeConverterRegistry registry) {

        try {
            Field convertersField = DefaultTransportTypeConverterRegistry.class.getDeclaredField("converters");
            convertersField.setAccessible(true);
            return (List<TransportTypeConverter>) convertersField.get(registry);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire converters field", e);
        }
    }
}
