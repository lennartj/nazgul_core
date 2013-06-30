/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
