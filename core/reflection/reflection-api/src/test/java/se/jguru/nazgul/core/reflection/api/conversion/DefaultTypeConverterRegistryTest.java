/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.reflection.api.conversion.helpers.FallbackTypeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.helpers.ListToStringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTypeConverterRegistryTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullMap() {

        // Act & Assert
        new DefaultTypeConverterRegistry(null);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNoConverterRegistered() {

        // Assemble
        final DefaultTypeConverterRegistry unitUnderTest = new DefaultTypeConverterRegistry();

        // Act & Assert
        unitUnderTest.convert("aString", StringBuffer.class);
    }

    @Test
    public void validateStandardConversion() {

        // Assemble
        final String source = "toConvert";
        final DefaultTypeConverterRegistry unitUnderTest = new DefaultTypeConverterRegistry();

        // Act
        unitUnderTest.addTypeConverter(new FallbackTypeConverter());
        final StringBuffer result = unitUnderTest.convert(source, StringBuffer.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(source, result.toString());
    }

    @Test
    public void validateTypeConversionUsingFuzzyConverterMatching() {

        // Assemble
        final List<String> toConvert = new ArrayList<String>();
        toConvert.add("foo");
        final DefaultTypeConverterRegistry unitUnderTest = new DefaultTypeConverterRegistry();

        // Act
        unitUnderTest.addTypeConverter(new ListToStringConverter());
        final String result = unitUnderTest.convert(toConvert, String.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(toConvert.toString(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingAlreadyRegisteredTypeConverter() {

        // Assemble
        final DefaultTypeConverterRegistry unitUnderTest = new DefaultTypeConverterRegistry();

        // Act & Assert
        unitUnderTest.addTypeConverter(new ListToStringConverter());
        unitUnderTest.addTypeConverter(new ListToStringConverter());
    }
}
