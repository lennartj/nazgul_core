/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CacheManagerNonDistributedEhCacheTest {

    // Shared state
    private CacheManager cacheManager;

    @Before
    public void setupCacheManager() {
        final String config = "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
        cacheManager = AbstractCacheTest.getTokenizedCacheManager(config);
    }

    @After
    public void shutdownCacheManager() {
        cacheManager.shutdown();
    }

    @Test
    public void validateLifecycleUsingPredefinedCacheManager() {

        // Assemble
        final NonDistributedEhCache unitUnderTest = new NonDistributedEhCache(cacheManager);
        final String key = "key";

        // Act
        final boolean keyInCache_before = unitUnderTest.containsKey(key);
        final Serializable before = unitUnderTest.put(key, "value");

        final boolean keyInCache_mid = unitUnderTest.containsKey(key);
        final Serializable mid = unitUnderTest.put(key, "value2");
        final Serializable after = unitUnderTest.get(key);
        final Serializable removed = unitUnderTest.remove(key);

        final boolean keyInCache_removed = unitUnderTest.containsKey(key);
        final Serializable shouldBeNull = unitUnderTest.get(key);

        // Assert
        Assert.assertNull(before);
        Assert.assertEquals("value", mid);
        Assert.assertEquals("value2", after);
        Assert.assertEquals("value2", removed);
        Assert.assertNull(shouldBeNull);
        Assert.assertFalse(keyInCache_before);
        Assert.assertTrue(keyInCache_mid);
        Assert.assertFalse(keyInCache_removed);
    }
}
