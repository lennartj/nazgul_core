/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheClusterIdGeneratorTest {

    @Test(expected = NullPointerException.class)
    public void validateNullPointerExceptionIfNotSettingCacheCluster() {

        // Assemble
        final EhCacheClusterIdGenerator unitUnderTest = new EhCacheClusterIdGenerator();

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isIdentifierAvailable());
        unitUnderTest.getIdentifier();
    }

    @Test
    public void validateIdRetrieval() {

        // Assemble
        final EhCacheClusterIdGenerator unitUnderTest = new EhCacheClusterIdGenerator();
        // unitUnderTest.setCacheManager();

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isIdentifierAvailable());
        unitUnderTest.getIdentifier();
    }
}
