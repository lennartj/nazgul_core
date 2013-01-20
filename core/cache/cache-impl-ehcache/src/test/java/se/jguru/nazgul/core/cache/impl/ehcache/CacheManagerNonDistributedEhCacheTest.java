/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CacheManagerNonDistributedEhCacheTest {

    @Test
    public void validateLifecycleUsingPredefinedCacheManager() {

        // Assemble
        final String config = "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
        final CacheManager mgr = NonDistributedEhCache.getCacheManager(config);
        final NonDistributedEhCache unitUnderTest = new NonDistributedEhCache(mgr);
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
