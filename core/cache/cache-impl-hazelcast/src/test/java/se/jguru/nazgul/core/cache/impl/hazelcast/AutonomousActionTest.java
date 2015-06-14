/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.DebugCacheListener;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AutonomousActionTest extends AbstractHazelcastCacheTest {

    private static final String LOGBACK_CONFIGURATION_PATH = "config/logging/logback-test.xml";
    private static final Logger log = LoggerFactory.getLogger(AutonomousActionTest.class);

    // Shared state
    final String quickEvictionMapId = "quickEvictionMap";
    final String key = "key";
    final String value = "value";
    final String quickEvictConfigFile = "config/hazelcast/QuickEvictionConfig.xml";

    @BeforeClass
    public static void initialize() {
        configureLogging(LOGBACK_CONFIGURATION_PATH);
    }

    @Test
    public void validateQuickEvictionConfigurationFile() {

        // Assemble
        final int expectedMaxSize = 4;
        final Config config = HazelcastCacheMember.readConfigFile(quickEvictConfigFile);

        // Act
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);
        final int maxSize = mapConfig.getMaxSizeConfig().getSize();

        // Assert
        Assert.assertEquals("The configuration file is corrupt. The map '" + quickEvictionMapId + "' must be defined.",
                quickEvictionMapId, mapConfig.getName());
        Assert.assertEquals("Corrupt configuration file. The map '" + quickEvictionMapId
                + "' must define max-size as " + expectedMaxSize + ".", expectedMaxSize, maxSize);
    }

    @Test
    public void validateEvictionInLifecycle() throws InterruptedException {

        // Assemble
        final Config config = HazelcastCacheMember.readConfigFile(quickEvictConfigFile);
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);
        final MaxSizeConfig maxSizeConfig = mapConfig.getMaxSizeConfig();

        Assert.assertEquals(4, maxSizeConfig.getSize());
        Assert.assertEquals(20, mapConfig.getEvictionPercentage());

        final int expectedRemainingAfterEviction = (int) (mapConfig.getMaxSizeConfig().getSize()
                * (100.0d - (double) mapConfig.getEvictionPercentage()) / 100.0d) - 1;

        final HazelcastCacheMember cache = new HazelcastCacheMember(config);
        final Map<String, String> quickEvictionMap = cache.getDistributedMap(quickEvictionMapId);
        final DebugCacheListener unitUnderTest1 = new DebugCacheListener("listener_1");

        final boolean success = cache.addListenerFor(quickEvictionMap, unitUnderTest1);
        Assert.assertTrue("Could not add DebugCacheListener to the quickEvictionMap.", success);
        Thread.sleep(200);

        // Act
        putValues(mapConfig.getMaxSizeConfig().getSize() + 1, quickEvictionMap);
        Thread.sleep(200);
        final TreeMap<String, String> result = getValueMap(quickEvictionMap);

        System.out.println("" + unitUnderTest1);
        System.out.println(" Result: " + result);

        /*
######################################
(hz._hzInstance_1_unittest-cache-group.event-1) [2]: put:key_1:value_1
(hz._hzInstance_1_unittest-cache-group.event-2) [3]: put:key_0:value_0
(hz._hzInstance_1_unittest-cache-group.event-4) [4]: autonomousEvict:key_3:null
(hz._hzInstance_1_unittest-cache-group.event-4) [6]: put:key_3:value_3
(hz._hzInstance_1_unittest-cache-group.event-4) [7]: autonomousEvict:key_4:null
(hz._hzInstance_1_unittest-cache-group.event-4) [8]: put:key_4:value_4
(hz._hzInstance_1_unittest-cache-group.event-5) [1]: autonomousEvict:key_2:null
(hz._hzInstance_1_unittest-cache-group.event-5) [5]: put:key_2:value_2
######################################
         */

        // Assert
        Assert.assertEquals("Expected [" + expectedRemainingAfterEviction
                        + "] remaining elements after eviction. Got [" + result.size() + "].",
                expectedRemainingAfterEviction, result.size());

        for (int i = 0; i < expectedRemainingAfterEviction; i++) {
            final String currentKey = key + "_" + i;
            final String currentValue = value + "_" + i;

            Assert.assertTrue(result.keySet().contains(currentKey));
            Assert.assertEquals(currentValue, result.get(currentKey));
        }
    }

    @Ignore("Will fail WRT ordering of the events before Hazelcast release 3.4")
    @Test
    public void validateEvictionInLifecycleInHazelcastAPI() throws InterruptedException {

        // Assemble
        final String quickEvictionMapId = "quickEvictionMap";
        final String quickEvictConfigFile = "config/hazelcast/QuickEvictionConfig.xml";
        final AtomicInteger counter = new AtomicInteger();

        final Config config = HazelcastCacheMember.readConfigFile(quickEvictConfigFile);
        config.setInstanceName("quickEvictionInstance");
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);
        final double expectedRemainingAfterEviction = (mapConfig.getMaxSizeConfig().getSize()
                * (100.0d - (double) mapConfig.getEvictionPercentage()) / 100.0d) - 1;

        final HazelcastInstance cache = Hazelcast.getOrCreateHazelcastInstance(config);
        final SortedMap<Integer, String> indexedMessages = new TreeMap<>();
        final EntryListener<String, String> el = new EntryAdapter<String, String>() {

            final Object lock = new Object();

            @Override
            public void onEntryEvent(final EntryEvent<String, String> event) {

                synchronized (lock) {
                    final String message = "onEntryEvent: " + event.getEventType().toString() + " - [" + event.getKey()
                            + "]: " + event.getOldValue() + " --> " + event.getValue();
                    indexedMessages.put(counter.getAndIncrement(), message);
                }
            }

            @Override
            public void onMapEvent(final MapEvent event) {

                synchronized (lock) {
                    final String message = "onMapEvent: " + event.getEventType().toString() + " - [" + event.getName()
                            + "(" + event.getNumberOfEntriesAffected() + "]: " + event.getEventType();
                    indexedMessages.put(counter.getAndIncrement(), message);
                }
            }
        };

        // Act
        final IMap<String, String> map = cache.getMap(quickEvictionMapId);
        map.addEntryListener(el, true);
        Thread.sleep(200);

        for (int i = 0; i < mapConfig.getMaxSizeConfig().getSize() + 1; i++) {
            map.put(key + "_" + i, value + "_" + i);
        }
        Thread.sleep(200);

        /*
######################################
[0]: onEntryEvent: ADDED - [key_1]: null --> value_1
[1]: onEntryEvent: ADDED - [key_0]: null --> value_0
[2]: onEntryEvent: EVICTED - [key_3]: value_3 --> null
[3]: onEntryEvent: EVICTED - [key_2]: value_2 --> null
[4]: onEntryEvent: ADDED - [key_3]: null --> value_3
[5]: onEntryEvent: ADDED - [key_2]: null --> value_2
[6]: onEntryEvent: EVICTED - [key_4]: value_4 --> null
[7]: onEntryEvent: ADDED - [key_4]: null --> value_4
######################################
         */
        System.out.println("\n\n\n######################################\n");
        for (Map.Entry<Integer, String> current : indexedMessages.entrySet()) {
            System.out.println("[" + current.getKey() + "]: " + current.getValue());
        }
        System.out.println("\n######################################\n\n\n");

        // Assert
    }

    //
    // Private helpers
    //

    private void putValues(final int numValues, final Map<String, String> map) {
        for (int i = 0; i < numValues; i++) {
            map.put(key + "_" + i, value + "_" + i);
        }
    }

    private TreeMap<String, String> getValueMap(final Map<String, String> source) {
        TreeMap<String, String> toReturn = new TreeMap<String, String>();
        for (String current : source.keySet()) {
            toReturn.put(current, source.get(current));
        }

        return toReturn;
    }
}
