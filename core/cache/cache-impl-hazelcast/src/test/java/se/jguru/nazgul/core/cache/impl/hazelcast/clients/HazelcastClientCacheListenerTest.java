/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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

package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastCacheTest;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.DebugCacheListener;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastCacheListenerAdapter;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.GridOperations;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastClientCacheListenerTest extends AbstractHazelcastCacheTest {

    private static final Logger log = LoggerFactory.getLogger(HazelcastClientCacheListenerTest.class);

    // Shared state
    final String key = "foo";
    final String value = "bar";
    private static final String configFile = "config/hazelcast/StandaloneConfig.xml";

    private static HazelcastCacheMember hzCache1;
    private static HazelcastCacheClient cacheClient;

    private static final ClientConfig clientConfig = new ClientConfig()
            .setNetworkConfig(new ClientNetworkConfig().addAddress("localhost:5701"))
            .setGroupConfig(new GroupConfig("unittest-cache-group", "unittest-pass"));

    @BeforeClass
    public static void initialize() {
        configureLogging();

        hzCache1 = getCache(configFile);
        cacheClient = new HazelcastCacheClient(clientConfig);
    }

    @After
    public void after() {
        purgeCache(cacheClient);
        purgeCache(hzCache1);
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedCollectionInDistributedCache()
            throws InterruptedException {

        // Assemble
        final String distributedListKey = "distList";

        final DebugCacheListener unitUnderTest1 = new DebugCacheListener("cacheListener1");
        final DebugCacheListener unitUnderTest2 = new DebugCacheListener("cacheListener2");

        final Collection<String> distributedCollection = (Collection<String>) hzCache1.getDistributedCollection(
                DistributedCache.DistributedCollectionType.COLLECTION, distributedListKey);

        // Act
        hzCache1.addListenerFor(distributedCollection, unitUnderTest1);
        cacheClient.addListenerFor(distributedCollection, unitUnderTest2);
        Thread.sleep(200);

        distributedCollection.add(value);
        final int collSizeAfterAddingValue = distributedCollection.size();

        final Object collIteratorGetResult = distributedCollection.iterator().next();
        final boolean collRemoveResult = distributedCollection.remove(value);
        final int collSizeAfterRemovingValue = distributedCollection.size();

        hzCache1.removeListenerFor(distributedCollection, unitUnderTest1.getClusterId());
        hzCache1.removeListenerFor(distributedCollection, unitUnderTest2.getClusterId());
        // Wait for the async listener removal to complete - implemented within the
        Thread.sleep(200);

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, cacheClient);
        Assert.assertEquals(1, collSizeAfterAddingValue);
        Assert.assertEquals(value, collIteratorGetResult);
        Assert.assertTrue(collRemoveResult);
        Assert.assertEquals(0, collSizeAfterRemovingValue);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedCollection);
        final List<String> listeners2 = cacheClient.getListenersIDsFor(distributedCollection);

        Assert.assertEquals(0, listeners1.size());
        Assert.assertEquals(0, listeners2.size());

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateCollectionEventInfo(traceMap1.get(1), "put", value);
        validateCollectionEventInfo(traceMap1.get(2), "remove", value);

        validateCollectionEventInfo(traceMap2.get(1), "put", value);
        validateCollectionEventInfo(traceMap2.get(2), "remove", value);
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedSetInDistributedCache() throws InterruptedException {

        // Assemble
        final String distributedSetKey = "distSet";
        final String listenerID1 = "setListener_1";
        final String listenerID2 = "setListener_2";

        final Set<String> distributedSet = (Set<String>) hzCache1.getDistributedCollection(
                DistributedCache.DistributedCollectionType.SET, distributedSetKey);

        // Act
        final DebugCacheListener unitUnderTest1 = new DebugCacheListener(listenerID1);
        final DebugCacheListener unitUnderTest2 = new DebugCacheListener(listenerID2);
        hzCache1.addListenerFor(distributedSet, unitUnderTest1);
        cacheClient.addListenerFor(distributedSet, unitUnderTest2);


        distributedSet.add(value);
        final int sizeAfterAddingValue = distributedSet.size();
        final String getResult = distributedSet.iterator().next();
        final boolean removeResult = distributedSet.remove(value);
        final int sizeAfterRemovingValue = distributedSet.size();

        hzCache1.removeListenerFor(distributedSet, listenerID1);
        waitAwhile(50);

        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedSet);
        final List<String> listeners2 = cacheClient.getListenersIDsFor(distributedSet);

        hzCache1.removeListenerFor(distributedSet, listenerID2);
        waitAwhile(50);

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, cacheClient);
        Assert.assertEquals(1, sizeAfterAddingValue);
        Assert.assertEquals(value, getResult);
        Assert.assertEquals(true, removeResult);
        Assert.assertEquals(0, sizeAfterRemovingValue);

        // Assert #2: Validate the CacheListener IDs.
        Assert.assertEquals(1, listeners1.size());
        Assert.assertEquals(1, listeners2.size());
        Assert.assertEquals(listenerID2, listeners1.get(0));
        Assert.assertEquals(listenerID2, listeners2.get(0));

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateCollectionEventInfo(traceMap1.get(1), "put", value);
        validateCollectionEventInfo(traceMap1.get(2), "remove", value);

        validateCollectionEventInfo(traceMap2.get(1), "put", value);
        validateCollectionEventInfo(traceMap2.get(2), "remove", value);
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedQueueInDistributedCache()
            throws InterruptedException {

        // Assemble
        final String distributedQueueKey = "distQueue";

        final DebugCacheListener unitUnderTest1 = new DebugCacheListener("testID_1");
        final DebugCacheListener unitUnderTest2 = new DebugCacheListener("testID_2");

        final Queue<String> distributedQueue = (Queue<String>) hzCache1.getDistributedCollection(
                DistributedCache.DistributedCollectionType.QUEUE, distributedQueueKey);

        // Act
        hzCache1.addListenerFor(distributedQueue, unitUnderTest1);
        cacheClient.addListenerFor(distributedQueue, unitUnderTest2);

        distributedQueue.add(value);
        final int sizeAfterAddingValue = distributedQueue.size();
        final String getResult = distributedQueue.iterator().next();
        final String getAndRemoveResult = distributedQueue.poll();  // This is the normal use of a queue.
        final int sizeAfterRemovingValue = distributedQueue.size();

        hzCache1.removeListenerFor(distributedQueue, unitUnderTest1.getClusterId());
        hzCache1.removeListenerFor(distributedQueue, unitUnderTest2.getClusterId());
        Thread.sleep(200);  // Wait for the async remove operation to complete

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, cacheClient);
        Assert.assertEquals(1, sizeAfterAddingValue);
        Assert.assertEquals(value, getResult);
        Assert.assertEquals(value, getAndRemoveResult);
        Assert.assertEquals(0, sizeAfterRemovingValue);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedQueue);
        final List<String> listeners2 = cacheClient.getListenersIDsFor(distributedQueue);

        Assert.assertEquals(0, listeners1.size());
        Assert.assertEquals(0, listeners2.size());

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateCollectionEventInfo(traceMap1.get(1), "put", value);
        validateCollectionEventInfo(traceMap1.get(2), "remove", value);

        validateCollectionEventInfo(traceMap2.get(1), "put", value);
        validateCollectionEventInfo(traceMap2.get(2), "remove", value);
    }

    @Test
    public void validateAdminMessageOperation() throws Exception {

        // Assemble
        final LightweightTopic<AdminMessage> adminTopic = cacheClient.getTopic(GridOperations.CLUSTER_ADMIN_TOPIC);

        final Map<String, String> clientMap1 = cacheClient.getDistributedMap("clientMap1");
        final Map<String, String> clientMap2 = cacheClient.getDistributedMap("clientMap2");

        final DebugCacheListener listener1 = new DebugCacheListener("listener1");
        final DebugCacheListener listener2 = new DebugCacheListener("listener2");

        cacheClient.addListenerFor(clientMap1, listener1);
        cacheClient.addListenerFor(clientMap2, listener2);

        // Act
        adminTopic.publish(AdminMessage.createMakeInCacheInstanceMessage(AdminMessage.TypeDefinition.MAP, "foo"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createRemoveListenerMessage("foo", "nonexistentListener"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createRemoveListenerMessage("nonexistentInstance", "nonexistentListener"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createRemoveListenerMessage("clientMap1", "listener1"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createShutdownInstanceMessage("nonexistentInstance"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createShutdownInstanceMessage("noMatchingInstance"));
        waitAwhile(100);
        adminTopic.publish(AdminMessage.createShutdownInstanceMessage(cacheClient.getClusterId()));
        waitAwhile(100);

        final HazelcastInstance cacheInstance = getInternalInstance(cacheClient);

        // Assert

        // Restore
        cacheClient = new HazelcastCacheClient(clientConfig);
    }

    //
    // private helpers
    //

    private void waitAwhile(int numMillis) {

        try {
            Thread.sleep(numMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void validateEventInfo(final DebugCacheListener.EventInfo info,
                                   final String expectedType,
                                   final String expectedKey,
                                   final String expectedValue) {

        Assert.assertEquals(expectedType, info.eventType);
        Assert.assertEquals(expectedKey, info.key);
        Assert.assertEquals(expectedValue, info.value);
    }

    private void validateCollectionEventInfo(DebugCacheListener.EventInfo info,
                                             final String expectedType,
                                             final String expectedValue) {

        validateEventInfo(info,
                expectedType,
                AbstractHazelcastCacheListenerAdapter.ILLUSORY_KEY_FOR_COLLECTIONS,
                expectedValue);
    }

    private void validateEventInfo(DebugCacheListener.EventInfo info, final String expectedType) {

        validateEventInfo(info, expectedType, key, value);
    }

    private void printEventInfoMap(TreeMap<Integer, DebugCacheListener.EventInfo> map) {

        log.info(" logging eventInfoMap ");

        for (Map.Entry<Integer, DebugCacheListener.EventInfo> current : map.entrySet()) {
            log.info("[" + current.getKey() + "]: " + current.getValue());
        }
    }

    private HazelcastInstance getInternalInstance(final HazelcastCacheClient cache) {

        try {
            Field instanceField = cache.getClass().getSuperclass().getDeclaredField("cacheInstance");
            instanceField.setAccessible(true);

            return (HazelcastInstance) instanceField.get(cache);

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire the cacheInstance", e);
        }
    }
}
