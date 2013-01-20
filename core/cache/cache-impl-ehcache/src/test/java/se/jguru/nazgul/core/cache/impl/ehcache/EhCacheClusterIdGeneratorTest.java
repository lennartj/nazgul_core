/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheClusterIdGeneratorTest extends AbstractCacheTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEhCacheConfiguration() {
        return "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
    }

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
        unitUnderTest.setCacheManager(getCache().getCacheInstance().getCacheManager());

        // Act
        Assert.assertTrue(unitUnderTest.isIdentifierAvailable());
        final String identifier = unitUnderTest.getIdentifier();

        // Assert
        Assert.assertNotNull(identifier);
        Assert.assertEquals(getCache().getId(), identifier);
    }
}
