/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UUIDGeneratorTest {

    @Test
    public void validateUniqueUUIDForEachCall() {

        // Assemble
        final UUIDGenerator unitUnderTest = new UUIDGenerator();

        // Act
        final boolean hasResult = unitUnderTest.isIdentifierAvailable();
        final String result1 = unitUnderTest.getIdentifier();
        final String result2 = unitUnderTest.getIdentifier();

        // Assert
        Assert.assertTrue(hasResult);
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);
        Assert.assertFalse(result1.equals(result2));
    }
}
