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
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.DebugCacheListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheListenerTest extends AbstractHazelcastCacheTest {

    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheListenerTest.class);

    // Shared state
    final String key = "foo";
    final String distributedMapKey = "distributedMap";
    final String value = "bar";
    private static final String configFile = "config/hazelcast/StandaloneConfig.xml";

    private static HazelcastCacheMember hzCache1;
    private static HazelcastCacheMember hzCache2;

    @BeforeClass
    public static void initialize() {
        configureLogging();

        hzCache1 = getCache(configFile);
        hzCache2 = getCache(configFile);
    }

    @After
    public void after() {
        purgeCache(hzCache1);
        purgeCache(hzCache2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddIncorrectInstanceType() {

        // Assemble
        final DebugCacheListener<Object> listener1 = new DebugCacheListener<Object>(hzCache1.getClusterUniqueID());

        // Act & Assert
        hzCache1.addListenerFor("notAnInstance", listener1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnRemoveIncorrectInstanceType() {

        // Act & Assert
        hzCache1.removeListenerFor("notAnInstance", "nonExistentID");
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnRemoveNonexistentListenerId() {

        // Assemble
        final Map<String, Serializable> distMap = hzCache1.getDistributedMap(distributedMapKey);

        // Act & Assert
        hzCache1.removeListenerFor(distMap, "notARegisteredListenerID");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnGettingListenerIDsForNonInstance() {

        // Act & Assert
        hzCache1.getListenersIDsFor("notAnInstance");
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnRemovingNonRegisteredListener() {

        // Assemble
        final String existentListenerID = "testListener";
        final Map<String, Serializable> distMap = hzCache1.getDistributedMap(distributedMapKey);
        final boolean addedOK = hzCache1.addListenerFor(distMap, new DebugCacheListener<Object>(existentListenerID));

        // Act & Assert
        Assert.assertTrue(addedOK);
        hzCache1.removeListenerFor(distMap, "notARegisteredListenerID");
    }

    @Test
    public void validateCacheListenerStateInCache() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> listener1 = new DebugCacheListener<Object>("listener_1");
        final DebugCacheListener<Object> listener2 = new DebugCacheListener<Object>("listener_2");
        final DebugCacheListener<Object> listener3WithSameIdAsListener2 = new DebugCacheListener<Object>(
                listener2.getClusterId());

        // Act
        final boolean listener1Added = hzCache1.addListener(listener1);
        final List<String> listenerKeysAfterListener1Added = hzCache1.getListenerIds();
        final boolean listener2Added = hzCache1.addListener(listener2);
        final List<String> listenerKeysAfterListener2Added = hzCache1.getListenerIds();
        final boolean listener3Added = hzCache1.addListener(listener3WithSameIdAsListener2);
        final List<String> listenerKeysAfterListener3Added = hzCache1.getListenerIds();

        hzCache1.put(key, value);
        hzCache1.remove(key);

        hzCache1.removeListener(listener1.getClusterId());
        // Wait a little for the async Topic to spray
        // out its message and remove the Listener.
        Thread.sleep(50);
        final List<String> listenerKeysAfterListener1Removed = hzCache1.getListenerIds();

        hzCache1.removeListener(listener2.getClusterId());
        // Wait a little for the async Topic to spray
        // out its message and remove the Listener.
        Thread.sleep(50);
        final List<String> listenerKeysAfterListener2Removed = hzCache1.getListenerIds();

        // Assert
        Assert.assertTrue(listener1Added);
        Assert.assertTrue(listener2Added);
        Assert.assertFalse(listener3Added);

        Assert.assertEquals(1, listenerKeysAfterListener1Added.size());
        Assert.assertEquals(listener1.getClusterId(), listenerKeysAfterListener1Added.get(0));
        Assert.assertEquals(2, listenerKeysAfterListener2Added.size());
        Assert.assertEquals(listener1.getClusterId(), listenerKeysAfterListener2Added.get(0));
        Assert.assertEquals(listener2.getClusterId(), listenerKeysAfterListener2Added.get(1));
        Assert.assertEquals(2, listenerKeysAfterListener3Added.size());
        Assert.assertEquals(listener1.getClusterId(), listenerKeysAfterListener3Added.get(0));
        Assert.assertEquals(listener2.getClusterId(), listenerKeysAfterListener3Added.get(1));
        Assert.assertEquals(1, listenerKeysAfterListener1Removed.size());
        Assert.assertEquals(listener2.getClusterId(), listenerKeysAfterListener1Removed.get(0));
        Assert.assertEquals(0, listenerKeysAfterListener2Removed.size());
    }

    @Ignore("Not quite working yet.")
    @Test
    public void validateCacheListenerLifecycleInSingleCacheInstance() throws InterruptedException {

        // Assemble
        final HazelcastInstance internalInstance1 = getInternalInstance(hzCache1);
        final HazelcastInstance internalInstance2 = getInternalInstance(hzCache2);
        final DebugCacheListener<Object> listener = new DebugCacheListener<Object>("listener_1");
        final String listenerID = listener.getClusterId();

        // Act
        final boolean addedSuccessfully = hzCache1.addListener(listener);

        final Object previous = hzCache1.put(key, value);
        // internalInstance1.getCountDownLatch("FooBar").
        final Object updated = hzCache1.put(key, value + "2");
        final Object updatedAgain = hzCache1.put(key, value);
        final Object result = hzCache1.get(key);
        final Object after = hzCache1.remove(key);

        // Assert
        Assert.assertTrue(addedSuccessfully);
        Assert.assertNull(previous);
        Assert.assertEquals(value, updated);
        Assert.assertEquals(value + "2", updatedAgain);
        Assert.assertEquals(value, result);
        Assert.assertEquals(value, after);
        Thread.sleep(100); // Wait until all events have been distributed fully.

        final List<String> listeners = hzCache1.getListenerIds();
        Assert.assertEquals(1, listeners.size());
        Assert.assertEquals(listenerID, listeners.get(0));

        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap = listener.eventId2EventInfoMap;
        Assert.assertEquals(4, traceMap.size());

        validateEventInfo(traceMap.get(1), "put");
        validateEventInfo(traceMap.get(2), "update", "foo", "bar2");
        validateEventInfo(traceMap.get(3), "update", "foo", "bar");
        validateEventInfo(traceMap.get(4), "remove");
    }

    @Test
    public void validateCacheListenerLifecycleInDistributedCache() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> unitUnderTest1 = new DebugCacheListener<Object>("listener_1");
        final DebugCacheListener<Object> unitUnderTest2 = new DebugCacheListener<Object>("listener_2");

        // Act
        hzCache1.addListener(unitUnderTest1);
        Thread.sleep(50);
        hzCache2.addListener(unitUnderTest2);
        Thread.sleep(200);

        final Object previous_1 = hzCache1.put(key, value);
        final Object result_2 = hzCache2.get(key);
        final Object after_2 = hzCache2.remove(key);

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, hzCache2);
        Assert.assertNull(previous_1);
        Assert.assertEquals(value, result_2);
        Assert.assertEquals(value, after_2);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listenerIDs1 = hzCache1.getListenerIds();
        final List<String> listenerIDs2 = hzCache2.getListenerIds();

        Assert.assertEquals(2, listenerIDs1.size());
        Assert.assertEquals(unitUnderTest1.getClusterId(), listenerIDs1.get(0));
        Assert.assertEquals(unitUnderTest2.getClusterId(), listenerIDs1.get(1));
        Assert.assertEquals(2, listenerIDs2.size());
        Assert.assertEquals(unitUnderTest1.getClusterId(), listenerIDs2.get(0));
        Assert.assertEquals(unitUnderTest2.getClusterId(), listenerIDs2.get(1));

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateEventInfo(traceMap1.get(1), "put");
        validateEventInfo(traceMap1.get(2), "remove");

        validateEventInfo(traceMap2.get(1), "put");
        validateEventInfo(traceMap2.get(2), "remove");
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedMapInDistributedCache() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> unitUnderTest1 = new DebugCacheListener<Object>("cacheListener1");
        final DebugCacheListener<Object> unitUnderTest2 = new DebugCacheListener<Object>("cacheListener2");

        final Map<String, String> distributedMap1 = hzCache1.getDistributedMap(distributedMapKey);
        final Map<String, String> distributedMap2 = hzCache2.getDistributedMap(distributedMapKey);

        // Act
        hzCache1.addListenerFor(distributedMap1, unitUnderTest1);
        hzCache2.addListenerFor(distributedMap2, unitUnderTest2);
        Thread.sleep(100);

        final Serializable previous = distributedMap1.put(key, value);
        final Serializable result_2 = distributedMap2.get(key);
        final Serializable result_1 = distributedMap1.get(key);
        final Serializable after_2 = distributedMap2.remove(key);
        final Serializable after_1 = distributedMap1.remove(key);
        Thread.sleep(100);

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, hzCache2);
        Assert.assertNull(previous);
        Assert.assertEquals(value, result_2);
        Assert.assertEquals(value, after_2);
        Assert.assertEquals(value, result_1);
        Assert.assertNull(after_1);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedMap1);
        final List<String> listeners2 = hzCache2.getListenersIDsFor(distributedMap2);

        Assert.assertEquals(2, listeners1.size());
        Assert.assertEquals(unitUnderTest1.getClusterId(), listeners1.get(0));
        Assert.assertEquals(unitUnderTest2.getClusterId(), listeners1.get(1));

        Assert.assertEquals(2, listeners2.size());
        Assert.assertEquals(unitUnderTest1.getClusterId(), listeners2.get(0));
        Assert.assertEquals(unitUnderTest2.getClusterId(), listeners2.get(1));

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateEventInfo(traceMap1.get(1), "put");
        validateEventInfo(traceMap1.get(2), "remove");

        validateEventInfo(traceMap2.get(1), "put");
        validateEventInfo(traceMap2.get(2), "remove");
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedCollectionInDistributedCache() throws InterruptedException {

        // Assemble
        final String distributedListKey = "distList";
        final DebugCacheListener<Object> unitUnderTest1 = new DebugCacheListener<Object>("cacheListener1");
        final DebugCacheListener<Object> unitUnderTest2 = new DebugCacheListener<Object>("cacheListener2");

        final Collection<String> distributedCollection =
                (Collection<String>) hzCache1.getDistributedCollection(
                        DistributedCache.DistributedCollectionType.COLLECTION, distributedListKey);

        // Act
        hzCache1.addListenerFor(distributedCollection, unitUnderTest1);
        hzCache2.addListenerFor(distributedCollection, unitUnderTest2);

        distributedCollection.add(value);
        final int collSizeAfterAddingValue = distributedCollection.size();

        final Object collIteratorGetResult = distributedCollection.iterator().next();
        final boolean collRemoveResult = distributedCollection.remove(value);
        final int collSizeAfterRemovingValue = distributedCollection.size();

        hzCache1.removeListenerFor(distributedCollection, unitUnderTest1.getClusterId());
        hzCache1.removeListenerFor(distributedCollection, unitUnderTest2.getClusterId());
        // Thread.sleep(200);
        // Wait for the async listener removal to complete - implemented within the

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, hzCache2);
        Assert.assertEquals(1, collSizeAfterAddingValue);
        Assert.assertEquals(value, collIteratorGetResult);
        Assert.assertTrue(collRemoveResult);
        Assert.assertEquals(0, collSizeAfterRemovingValue);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedCollection);
        final List<String> listeners2 = hzCache2.getListenersIDsFor(distributedCollection);

        Assert.assertEquals(0, listeners1.size());
        Assert.assertEquals(0, listeners2.size());

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

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

        final Set<String> distributedSet =
                (Set<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.SET,
                        distributedSetKey);

        // Act
        final DebugCacheListener<Object> unitUnderTest1 = new DebugCacheListener<Object>(listenerID1);
        final DebugCacheListener<Object> unitUnderTest2 = new DebugCacheListener<Object>(listenerID2);
        hzCache1.addListenerFor(distributedSet, unitUnderTest1);
        hzCache2.addListenerFor(distributedSet, unitUnderTest2);

        distributedSet.add(value);
        final int sizeAfterAddingValue = distributedSet.size();
        final String getResult = distributedSet.iterator().next();
        final boolean removeResult = distributedSet.remove(value);
        final int sizeAfterRemovingValue = distributedSet.size();

        hzCache1.removeListenerFor(distributedSet, listenerID1);
        // Thread.sleep(200); // Wait for the async remove operation to complete

        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedSet);
        final List<String> listeners2 = hzCache2.getListenersIDsFor(distributedSet);

        hzCache1.removeListenerFor(distributedSet, listenerID2);
        // Thread.sleep(200); // Wait for the async remove operation to complete

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, hzCache2);
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
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateCollectionEventInfo(traceMap1.get(1), "put", value);
        validateCollectionEventInfo(traceMap1.get(2), "remove", value);

        validateCollectionEventInfo(traceMap2.get(1), "put", value);
        validateCollectionEventInfo(traceMap2.get(2), "remove", value);
    }

    @Test
    public void validateCacheListenerLifecycleOnDistributedQueueInDistributedCache() throws InterruptedException {

        // Assemble
        final String distributedQueueKey = "distQueue";

        final DebugCacheListener<Object> unitUnderTest1 = new DebugCacheListener<Object>("testID_1");
        final DebugCacheListener<Object> unitUnderTest2 = new DebugCacheListener<Object>("testID_2");

        final Queue<String> distributedQueue =
                (Queue<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.QUEUE,
                        distributedQueueKey);

        // Act
        hzCache1.addListenerFor(distributedQueue, unitUnderTest1);
        hzCache2.addListenerFor(distributedQueue, unitUnderTest2);

        distributedQueue.add(value);
        final int sizeAfterAddingValue = distributedQueue.size();
        final String getResult = distributedQueue.iterator().next();
        final String getAndRemoveResult = distributedQueue.poll(); // This is the normal use of a queue.
        final int sizeAfterRemovingValue = distributedQueue.size();

        hzCache1.removeListenerFor(distributedQueue, unitUnderTest1.getClusterId());
        hzCache2.removeListenerFor(distributedQueue, unitUnderTest2.getClusterId());
        Thread.sleep(200); // Wait for the async remove operation to complete

        // Assert #1: Check cache state.
        Assert.assertNotSame(hzCache1, hzCache2);
        Assert.assertEquals(1, sizeAfterAddingValue);
        Assert.assertEquals(value, getResult);
        Assert.assertEquals(value, getAndRemoveResult);
        Assert.assertEquals(0, sizeAfterRemovingValue);

        // Assert #2: Validate the CacheListener IDs.
        final List<String> listeners1 = hzCache1.getListenersIDsFor(distributedQueue);
        final List<String> listeners2 = hzCache2.getListenersIDsFor(distributedQueue);

        Assert.assertEquals(0, listeners1.size());
        Assert.assertEquals(0, listeners2.size());

        // Assert #2: Validate the Lifecycle of the CacheListeners.
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap1 = unitUnderTest1.eventId2EventInfoMap;
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> traceMap2 = unitUnderTest2.eventId2EventInfoMap;

        Thread.sleep(200); // Wait for the async remove operation to complete

        Assert.assertEquals(2, traceMap1.size());
        Assert.assertEquals(2, traceMap2.size());

        validateCollectionEventInfo(traceMap1.get(1), "put", value);
        validateCollectionEventInfo(traceMap1.get(2), "remove", value);

        validateCollectionEventInfo(traceMap2.get(1), "put", value);
        validateCollectionEventInfo(traceMap2.get(2), "remove", value);
    }

    @Test
    public void validateInstanceListeners() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> listener1 = new DebugCacheListener<Object>("listener_1");
        hzCache1.addInstanceListener(listener1);

        // Act
        final Queue<String> queue =
                (Queue<String>) hzCache1.getDistributedCollection(
                        DistributedCache.DistributedCollectionType.QUEUE, "queue_1");
        Assert.assertNotNull(queue);
        Thread.sleep(500);

        final boolean successfullyRemoved = hzCache1.removeInstanceListener(listener1.getClusterId());

        // Assert
        Assert.assertTrue(successfullyRemoved);
        final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> eventMap1 = listener1.eventId2EventInfoMap;

        Assert.assertTrue(eventMap1.size() > 0);
        final DebugCacheListener.EventInfo info = eventMap1.get(1);

        Assert.assertEquals("put", info.eventType);
        Assert.assertTrue(info.key.startsWith("q:queue_"));
        Assert.assertSame(info.value, queue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingNullInstanceListener() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> incorrectNull = null;

        // Act & Assert
        hzCache1.addInstanceListener(incorrectNull);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingAlreadyAddedInstanceListener() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> listener1 = new DebugCacheListener<Object>("listener_1");
        final DebugCacheListener<Object> listener2WithSameIDasListener1 = new DebugCacheListener<Object>("listener_1");

        // Act & Assert
        try {
            hzCache1.addInstanceListener(listener1);
        } catch (final Exception e) {
            Assert.fail("Should be able to add an InstanceListener with unique listenerID.");
        }

        hzCache1.addInstanceListener(listener2WithSameIDasListener1);
    }

    @Test
    public void validateGracefulReturnOnRemovingNullOrNonexistentInstanceListener() throws InterruptedException {

        // Assemble
        final DebugCacheListener<Object> listener1 = new DebugCacheListener<Object>("listener_1");
        hzCache1.addInstanceListener(listener1);

        // Act
        final boolean resultOfNullRemoval = hzCache1.removeInstanceListener(null);
        final boolean resultOfNonexistentIdRemoval =
                hzCache1.removeInstanceListener("nonextistenInstanceListenerID");

        // Assert
        Assert.assertFalse(resultOfNullRemoval);
        Assert.assertFalse(resultOfNonexistentIdRemoval);
    }

    //
    // private helpers
    //

    private void validateEventInfo(final DebugCacheListener<Object>.EventInfo info,
                                   final String expectedType,
                                   final String expectedKey,
                                   final String expectedValue) {

        Assert.assertEquals(expectedType, info.eventType);
        Assert.assertEquals(expectedKey, info.key);
        Assert.assertEquals(expectedValue, info.value);
    }

    private void validateCollectionEventInfo(final DebugCacheListener<Object>.EventInfo info, final String expectedType,
                                             final String expectedValue) {

        validateEventInfo(info, expectedType, AbstractHazelcastCacheListenerAdapter.ILLUSORY_KEY_FOR_COLLECTIONS, expectedValue);
    }

    private void validateEventInfo(final DebugCacheListener<Object>.EventInfo info, final String expectedType) {

        validateEventInfo(info, expectedType, key, value);
    }

    private void printEventInfoMap(final TreeMap<Integer, DebugCacheListener<Object>.EventInfo> map) {

        log.info(" logging eventInfoMap ");

        for (final Map.Entry<Integer, DebugCacheListener<Object>.EventInfo> current : map.entrySet()) {
            log.info("[" + current.getKey() + "]: " + current.getValue());
        }
    }
}
