/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.DebugCacheListener;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.EventInfo;

import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AbstractHazelcastCacheListenerAdapterTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnIncorrectConstruction() {

        // Assemble
        final CacheListener<String, String> incorrectNullCacheListener = null;

        // Act & Assert
        new StringKeyedHazelcastListenerAdapter<String>(incorrectNullCacheListener);
    }

    @Test
    public void validateEvictionStateEventOrder() {

        // Assemble
        final DebugCacheListener<String> cacheListener = new DebugCacheListener<String>("testId1");
        final StringKeyedHazelcastListenerAdapter<String> unitUnderTest
                = new StringKeyedHazelcastListenerAdapter<String>(cacheListener);

        // Act
        unitUnderTest.entryAdded(getEntryEvent(EntryEventType.ADDED));
        unitUnderTest.entryRemoved(getEntryEvent(EntryEventType.REMOVED));
        unitUnderTest.entryUpdated(getEntryEvent(EntryEventType.UPDATED));
        unitUnderTest.entryEvicted(getEntryEvent(EntryEventType.UPDATED));
        // unitUnderTest.instanceCreated(new InstanceEvent(InstanceEvent.InstanceEventType.CREATED, null));
        // unitUnderTest.instanceDestroyed(new InstanceEvent(InstanceEvent.InstanceEventType.DESTROYED, null));

        // Assert
        Assert.assertSame(cacheListener, unitUnderTest.getCacheListener());
        final TreeMap<Integer, EventInfo> map = cacheListener.eventId2EventInfoMap;

        // {1=put:key:value, 2=remove:key:value, 3=update:key:value, 4=autonomousEvict:key:value}
        Assert.assertEquals("put:key:value", map.get(1).toString());
        Assert.assertEquals("remove:key:value", map.get(2).toString());
        Assert.assertEquals("update:key:value", map.get(3).toString());
        Assert.assertEquals("autonomousEvict:key:value", map.get(4).toString());
    }

    @Test
    public void validateIdentityManagement() {

        // Assemble
        final DebugCacheListener<String> cacheListener1 = new DebugCacheListener<String>("testId1");
        final DebugCacheListener<String> cacheListener2 = new DebugCacheListener<String>("testId1");
        final StringKeyedHazelcastListenerAdapter<String> unitUnderTest1
                = new StringKeyedHazelcastListenerAdapter<String>(cacheListener1);
        final StringKeyedHazelcastListenerAdapter<String> unitUnderTest2
                = new StringKeyedHazelcastListenerAdapter<String>(cacheListener2);
        final AbstractHazelcastCacheListenerAdapter incorrectNull = null;

        // Act
        final boolean result1 = unitUnderTest1.equals(unitUnderTest2);
        final boolean result2 = unitUnderTest1.equals(incorrectNull);
        final boolean result3 = unitUnderTest1.equals("incorrectType");

        // Assert
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
        Assert.assertFalse(result3);
    }

    //
    // Private helpers
    //

    private EntryEvent<String, String> getEntryEvent(EntryEventType type) {
        return new EntryEvent<String, String>("irrelevantSource", null, type.getType(), "key", "value");
    }
}
