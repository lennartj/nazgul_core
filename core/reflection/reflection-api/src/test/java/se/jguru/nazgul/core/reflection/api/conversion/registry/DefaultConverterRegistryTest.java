/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultConverterRegistryTest {

    // Shared state
    private DefaultConverterRegistry unitUnderTest;

    @Before
    public void setupSharedState() {
        unitUnderTest = new DefaultConverterRegistry();
    }

    @Test
    public void validateBlankState() {

        // Act
        final Set<Class<?>> result = unitUnderTest.getPossibleConversions(String.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void validateNoExceptionOnNullConversionToType() {

        // Act
        final Set<Class<?>> result = unitUnderTest.getPossibleConversions(null);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    //
    // Private helpers
    //

    private Map<Class<?>, PrioritizedTypeConverter> getConvertersMap(final DefaultConverterRegistry instance) {

        try {
            Field mapField = DefaultConverterRegistry.class.getDeclaredField("sourceTypeToTypeConvertersMap");
            return (Map<Class<?>, PrioritizedTypeConverter>) mapField.get(instance);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not acquire field.", e);
        }
    }
}
