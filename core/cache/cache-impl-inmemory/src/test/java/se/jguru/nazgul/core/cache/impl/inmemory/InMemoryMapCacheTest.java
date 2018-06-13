/*-
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-inmemory
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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



package se.jguru.nazgul.core.cache.impl.inmemory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.clustering.api.UUIDGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InMemoryMapCacheTest {

    // Shared state
    private InMemoryMapCache unitUnderTest;
    private ConcurrentMap<String, Serializable> cache;
    private ConcurrentMap<String, CacheListener<String, Serializable>> listeners;

    @Before
    public void setupSharedState() {

        cache = new ConcurrentHashMap<String, Serializable>();
        listeners = new ConcurrentHashMap<String, CacheListener<String, Serializable>>();
        unitUnderTest = new InMemoryMapCache(UUIDGenerator.getInstance(),
                2000L,
                cache,
                listeners,
                10,
                true);
    }

    @Test
    public void validateNormalCacheLifecycle() {

        // Assemble
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

    @Test
    public void validateNullResultWhenRemovingNonexistentElement() {

        // Assemble
        final String key = "key";

        // Act
        final Serializable result = unitUnderTest.remove(key);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateIterationOverAllKnownKeys() {

        // Assemble
        final Map<String, String> data = new HashMap<String, String>();
        data.put("fooo", "bar");
        data.put("gnat", "baz");

        for (String current : data.keySet()) {
            unitUnderTest.put(current, data.get(current));
        }

        // Act & Assert
        for (String current : unitUnderTest) {
            Assert.assertEquals(unitUnderTest.get(current), data.get(current));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnNotFakingTransactions() {

        // Assemble
        final InMemoryMapCache defaultConstructedCache = new InMemoryMapCache();
        final AbstractTransactedAction transactedAction = new AbstractTransactedAction("Irrelevant") {
            @Override
            public void doInTransaction() throws RuntimeException {
                // Nothing sensible here.
            }
        };

        // Act & Assert
        defaultConstructedCache.performTransactedAction(transactedAction);
    }

    @Test
    public void validateSwiftSerialization() throws Exception {

        // Assemble
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        final InMemoryMapCache anotherCache = new InMemoryMapCache();

        final List<String> messages = new ArrayList<String>();
        final AbstractTransactedAction transactedAction = new AbstractTransactedAction("Irrelevant") {
            @Override
            public void doInTransaction() throws RuntimeException {
                messages.add("Performed transacted action.");
            }
        };


        // Act
        unitUnderTest.writeExternal(oos);

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        anotherCache.readExternal(ois);
        anotherCache.performTransactedAction(transactedAction);

        // Assert
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals("Performed transacted action.", messages.get(0));
    }
}
