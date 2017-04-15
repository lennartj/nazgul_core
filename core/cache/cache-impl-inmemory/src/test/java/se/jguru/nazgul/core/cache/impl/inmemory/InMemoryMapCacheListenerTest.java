/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-inmemory
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */


package se.jguru.nazgul.core.cache.impl.inmemory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.clustering.api.UUIDGenerator;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InMemoryMapCacheListenerTest {

    // Shared state
    private InMemoryMapCache unitUnderTest;
    private IdGenerator idGenerator;
    private ConcurrentMap<String, Serializable> cache;
    private ConcurrentMap<String, CacheListener<String, Serializable>> listeners;

    @Before
    public void setupSharedState() {

        idGenerator = new UUIDGenerator();
        cache = new ConcurrentHashMap<String, Serializable>();
        listeners = new ConcurrentHashMap<String, CacheListener<String, Serializable>>();
        unitUnderTest = new InMemoryMapCache(idGenerator, 2000L, cache, listeners, 10, true);
    }

    @Test
    public void validateSilentRemovalOfNonExistentCacheListener() {

        // Assemble
        final MockCacheListener listener1 = new MockCacheListener("listener1");
        final MockCacheListener listener2 = new MockCacheListener("listener2");

        // Act & Assert
        unitUnderTest.removeListener(listener1.getClusterId());
        unitUnderTest.addListener(listener1);
        unitUnderTest.removeListener(listener2.getClusterId());
    }

    @Test
    public void validateIgnoringAddingIdenticalListeners() {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");

        // Act
        final List<String> before = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> mid = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> after = unitUnderTest.getListenerIds();

        // Assert
        Assert.assertEquals(0, before.size());
        Assert.assertEquals(1, mid.size());
        Assert.assertEquals(listener.getClusterId(), mid.get(0));
        Assert.assertEquals(1, after.size());
        Assert.assertEquals(listener.getClusterId(), after.get(0));
    }

    @Test
    public void validateInMemoryCacheListenerLifecycle() {

        // Assemble
        final MockCacheListener listener = new MockCacheListener("listener");

        // Act
        final List<String> before = unitUnderTest.getListenerIds();
        unitUnderTest.addListener(listener);
        final List<String> mid = unitUnderTest.getListenerIds();
        unitUnderTest.removeListener(listener.getClusterId());
        final List<String> after = unitUnderTest.getListenerIds();

        // Assert
        Assert.assertEquals(0, before.size());
        Assert.assertEquals(1, mid.size());
        Assert.assertEquals(listener.getClusterId(), mid.get(0));
        Assert.assertEquals(0, after.size());
    }

    @Test
    public void validateInMemoryCacheListenerAdapterCallbacks() throws Exception {

        // Assemble
        final String key = "key";

        final MockCacheListener listener = new MockCacheListener("testListener");
        unitUnderTest.addListener(listener);

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

        Thread.sleep(150L);

        final List<String> callTrace = listener.callStack;

        Assert.assertEquals(3, callTrace.size());
        Assert.assertTrue(callTrace.contains("onPut [" + key + "]: value"));
        Assert.assertTrue(callTrace.contains("onUpdate [" + key + "]: value --> value2"));
        Assert.assertTrue(callTrace.contains("onRemove [" + key + "]: value2"));
    }
}
