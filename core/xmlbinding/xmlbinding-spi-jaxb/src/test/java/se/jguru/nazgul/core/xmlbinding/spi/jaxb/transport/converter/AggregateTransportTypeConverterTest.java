/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.reflection.api.conversion.TypeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.PrioritizedTypeConverter;

import java.util.Date;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AggregateTransportTypeConverterTest {

    // Shared state
    private TypeConverter<String, StringBuffer> fromStringConverter;
    private TypeConverter<StringBuffer, String> fromStringBufferConverter;
    private AggregateTransportTypeConverter<String, StringBuffer> unitUnderTest;

    @Before
    public void setupSharedState() {

        // Parse and create converters
        final PrioritizedTypeConverter<String> conv1 = new PrioritizedTypeConverter<String>(String.class);
        final PrioritizedTypeConverter<StringBuffer> conv2 = new PrioritizedTypeConverter<StringBuffer>(
                StringBuffer.class);

        conv1.add(this);
        conv2.add(this);

        fromStringConverter = conv1.getTypeConverters(StringBuffer.class).get(0);
        fromStringBufferConverter = conv2.getTypeConverters(String.class).get(0);

        // Create the aggregate converter
        unitUnderTest = new AggregateTransportTypeConverter<String, StringBuffer>(
                fromStringConverter, fromStringBufferConverter);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPackagingConverter() {

        // Act & Assert
        new AggregateTransportTypeConverter<String, StringBuffer>(null, fromStringBufferConverter);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullUnpackagingConverter() {

        // Act & Assert
        new AggregateTransportTypeConverter<String, StringBuffer>(fromStringConverter, null);
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

    //
    // Converter implementation methods
    //

    @Converter
    public StringBuffer convertToStringBuffer(final String aString) {
        return new StringBuffer(aString);
    }

    @Converter
    public String convertToString(final StringBuffer aStringBuffer) {
        return aStringBuffer.toString();
    }
}
