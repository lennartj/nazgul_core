/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.EntryEvent;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.CacheListener;

import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheListenerAdapterTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnIncorrectConstruction() {

        // Assemble
        final CacheListener incorrectNullCacheListener = null;

        // Act & Assert
        new HazelcastCacheListenerAdapter(incorrectNullCacheListener);
    }

    @Test
    public void validateFoo() {

        // Assemble
        final DebugCacheListener cacheListener = new DebugCacheListener("testId1");
        final HazelcastCacheListenerAdapter unitUnderTest = new HazelcastCacheListenerAdapter(cacheListener);

        // Act
        unitUnderTest.entryAdded(getEntryEvent(EntryEvent.TYPE_ADDED));
        unitUnderTest.entryRemoved(getEntryEvent(EntryEvent.TYPE_REMOVED));
        unitUnderTest.entryUpdated(getEntryEvent(EntryEvent.TYPE_UPDATED));
        unitUnderTest.entryEvicted(getEntryEvent(EntryEvent.TYPE_UPDATED));
        // unitUnderTest.instanceCreated(new InstanceEvent(InstanceEvent.InstanceEventType.CREATED, null));
        // unitUnderTest.instanceDestroyed(new InstanceEvent(InstanceEvent.InstanceEventType.DESTROYED, null));

        // Assert
        Assert.assertSame(cacheListener, unitUnderTest.getCacheListener());
        final TreeMap<Integer, DebugCacheListener.EventInfo> map = cacheListener.eventId2KeyValueMap;

        // {1=put:key:value, 2=remove:key:value, 3=update:key:value, 4=autonomousEvict:key:value}
        Assert.assertEquals("put:key:value", map.get(1).toString());
        Assert.assertEquals("remove:key:value", map.get(2).toString());
        Assert.assertEquals("update:key:value", map.get(3).toString());
        Assert.assertEquals("autonomousEvict:key:value", map.get(4).toString());
    }

    @Test
    public void validateIdentityManagement() {

        // Assemble
        final DebugCacheListener cacheListener1 = new DebugCacheListener("testId1");
        final DebugCacheListener cacheListener2 = new DebugCacheListener("testId1");
        final HazelcastCacheListenerAdapter unitUnderTest1 = new HazelcastCacheListenerAdapter(cacheListener1);
        final HazelcastCacheListenerAdapter unitUnderTest2 = new HazelcastCacheListenerAdapter(cacheListener2);
        final HazelcastCacheListenerAdapter incorrectNull = null;

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

    private EntryEvent<String, String> getEntryEvent(int type) {
        return new EntryEvent<String, String>("irrelevantSource", null,
                EntryEvent.TYPE_ADDED, "key", "value");
    }
}
