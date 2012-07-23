/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.reflection.api.conversion.AbstractTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.StringBufferToStringConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper.StringToStringBufferConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import java.util.Date;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AggregateTransportTypeConverterTest {

    // Shared state
    final StringBufferToStringConverter unpackagingConverter = new StringBufferToStringConverter();
    final StringToStringBufferConverter packagingConverter = new StringToStringBufferConverter();
    final AggregateTransportTypeConverter<String, StringBuffer> unitUnderTest =
            new AggregateTransportTypeConverter<String, StringBuffer>(packagingConverter, unpackagingConverter);

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPackagingConverter() {

        // Act & Assert
        new AggregateTransportTypeConverter<String, StringBuffer>(null, unpackagingConverter);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullUnpackagingConverter() {

        // Act & Assert
        new AggregateTransportTypeConverter<String, StringBuffer>(packagingConverter, null);
    }

    @Test
    public void validatePackageForTransport() {

        // Assemble
        final String original = "FooBar!";

        // Act
        final boolean result1 = unitUnderTest.canPackageForTransport(original);
        final boolean result2 = unitUnderTest.canPackageForTransport(null);
        final boolean result3 = unitUnderTest.canPackageForTransport(new Date());

        final StringBuffer packaged = unitUnderTest.packageForTransport(original);

        // Assert
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
        Assert.assertFalse(result3);
        Assert.assertEquals(original, packaged.toString());
    }

    @Test
    public void validateReviveAfterTransport() {

        // Assemble
        final String original = "FooBar!";
        final StringBuffer packaged = new StringBuffer(original);

        // Act
        final boolean result1 = unitUnderTest.canReviveAfterTransport(null);
        final boolean result2 = unitUnderTest.canReviveAfterTransport(original);
        final boolean result3 = unitUnderTest.canReviveAfterTransport(packaged);

        final String revived = unitUnderTest.reviveAfterTransport(packaged);

        // Assert
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
        Assert.assertTrue(result3);
        Assert.assertEquals(original, revived);
    }
}
