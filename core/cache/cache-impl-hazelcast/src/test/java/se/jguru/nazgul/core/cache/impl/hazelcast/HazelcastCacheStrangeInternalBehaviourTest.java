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
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;
import se.jguru.nazgul.core.cache.impl.hazelcast.trivialmodel.DebugSerializationEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This testcase is *very important* in that it tracks how the currently used release of
 * AbstractHazelcastInstanceWrapper behaves internally for certain actions.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheStrangeInternalBehaviourTest extends AbstractHazelcastCacheTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheStrangeInternalBehaviourTest.class);

    private static final String SLF4J_CONFIGURATION_PATH = "config/logging/logback-test-silent.xml";
    private static final String configFile = "config/hazelcast/StandaloneConfig.xml";

    // Shared state
    final String key = "foo";
    final String value = "bar";
    private static HazelcastCacheMember hzCache1;
    private static HazelcastCacheMember hzCache2;

    @BeforeClass
    public static void initialize() {
        configureLogging(SLF4J_CONFIGURATION_PATH);

        hzCache1 = getCache(configFile);
        hzCache2 = getCache(configFile);
    }

    @After
    public void after() {
        purgeCache(hzCache1);
        purgeCache(hzCache2);
    }

    @Test
    public void validateDistributedCollectionInSingleCacheInstance() {

        // Assemble

        final Set<String> distSet =
                (Set<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.SET,
                        "testSet");
        final List<String> distList =
                (List<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.COLLECTION,
                        "testList");
        final Queue<String> distQueue =
                (Queue<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.QUEUE,
                        "testQueue");

        // Act
        addElements(distList);
        addElements(distSet);
        addElements(distQueue);

        // Assert
        Assert.assertEquals(3, distSet.size());
        Assert.assertTrue(distSet.contains("1"));
        Assert.assertTrue(distSet.contains("2"));
        Assert.assertTrue(distSet.contains("3"));

        Assert.assertEquals(3, distQueue.size());
        Assert.assertEquals("1", distQueue.poll());
        Assert.assertEquals("2", distQueue.poll());
        Assert.assertEquals("3", distQueue.poll());

        Assert.assertEquals(3, distList.size());
        Assert.assertTrue(distList.contains("1"));
        Assert.assertTrue(distList.contains("2"));
        Assert.assertTrue(distList.contains("3"));
    }

    /**
     * The Hazelcast [version 1.9.1, 1.9.2] IList does not implement List.get(). Moreover, its implementation does not
     * honour the contract of java.util.List in terms of ordering.
     * <p/>
     * This test is devised to notify us if that fact changes - in which case we need to validate that our
     * implementation is still valid.
     * <p/>
     * Corrected in 1.9.3
     */
    @Test
    public void validateDistributedListIncompleteOperations_CorrectedSince_1_9_3() throws InterruptedException {

        // Assemble
        final List<String> distList =
                (List<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.COLLECTION,
                        "testIntegerList");

        // Act
        for (int i = 0; i < 50; i++) {
            distList.add("" + i);
        }

        // Assert
        for (int i = 0; i < distList.size(); i++) {
            Assert.assertEquals("" + i, distList.get(i));
        }
    }

    /**
     * The Hazelcast [version 1.9.1, 1.9.2] IList does not implement List.get(). Moreover, its implementation does not
     * honour the contract of java.util.List in terms of ordering.
     * <p/>
     * This test is devised to notify us if that fact changes - in which case we need to ensure that our implementation
     * is still valid.
     * <p/>
     * Corrected in Hazelcast 1.9.3.
     */
    @Test
    public void validateDistributedListCorrectOrdering_Since1_9_3() {

        // Assemble
        final List<String> distList =
                (List<String>) hzCache1.getDistributedCollection(DistributedCache.DistributedCollectionType.COLLECTION,
                        "testList");

        // Act
        distList.add("1");
        distList.add("2");
        distList.add("3");

        final List<String> orderedElements = new ArrayList<String>();
        for (final String current : distList) {
            orderedElements.add(current);
        }

        // Assert
        Assert.assertEquals(3, distList.size());
        boolean incorrectlyOrderedElementFound = false;
        for (int i = 0; i < orderedElements.size(); i++) {
            if (!orderedElements.get(i).equals("" + (i + 1))) {
                incorrectlyOrderedElementFound = true;
            }
        }

        Assert.assertFalse(incorrectlyOrderedElementFound);
    }

    /**
     * The Hazelcast [version 1.9.1+] distributed collections does not distribute changes to values, such as
     * collections used as keys, unless such collections are retrieved from Hazelcast.
     * <p/>
     * This test is devised to notify us if that fact changes - in which case we need to ensure that our implementation
     * is still valid.
     */
    @Test
    public void validateOkDistributionWhenUsingInternalCollectionsAsValuesInDistributedCollections()
            throws InterruptedException {

        // Assemble
        final HazelcastInstance member1 = getInternalInstance(hzCache1);
        final HazelcastInstance member2 = getInternalInstance(hzCache2);

        final String sharedMapName = "sharedMap";
        final IMap<String, Set<String>> referenceFromMember1 = member1.getMap(sharedMapName);
        final IMap<String, Set<String>> referenceFromMember2 = member2.getMap(sharedMapName);

        // Act
        // final Set<String> valueInDistributedMap = new TreeSet<String>();
        final Set<String> valueInDistributedMap = member1.getSet("someID");
        valueInDistributedMap.add(value);

        referenceFromMember1.put(key, valueInDistributedMap);
        referenceFromMember2.get(key).add("baz");
        Thread.sleep(100);

        final Set<String> valueFromMember1 = referenceFromMember1.get(key);
        final Set<String> valueFromMember2 = referenceFromMember2.get(key);

        // Assert
        final String[] expected = {value, "baz"};
        for (final String current : expected) {
            Assert.assertTrue(valueFromMember2.contains(current));
        }
        for (final String current : expected) {
            Assert.assertTrue(valueFromMember1.contains(current));
        }
    }

    /**
     * The Hazelcast [version 1.9.1+] distributed collections (ISet and IList) do not correctly implement
     * java.util.Collection.clear. This implies that severe memory leaks are generated within hazelcast whenever the
     * *internal structure of a cached object* contains a non-sorted Collection (i.e. HashSet or HashMap).
     * <p/>
     * This implies: a) Entity classes developed in-house cannot use Set relations, but must use SortedSet. b) 3rd party
     * products, whose implementations cannot be controlled cannot be cached within Hazelcast. c) Hazelcast cannot be
     * used for enterprise development caching until this bug is patched.
     * <p/>
     * <p/>
     * This test is devised to notify us if new versions of Hazelcast are bugfixed enough to use within our development.
     */
    @Ignore("Seems bugfixed in Hazelcast 2.3.1+")
    @Test
    public void validateIncorrectDistributionWhenUsingStandardCollectionsAsValuesInDistributedCollections()
            throws InterruptedException {

        // Assemble
        final HazelcastInstance member1 = getInternalInstance(hzCache1);
        final HazelcastInstance member2 = getInternalInstance(hzCache2);

        final String sharedMapName = "sharedMap";
        final IMap<String, Set<String>> referenceFromMember1 = member1.getMap(sharedMapName);
        Thread.sleep(100);
        final IMap<String, Set<String>> referenceFromMember2 = member2.getMap(sharedMapName);

        // Act
        final Set<String> valueInDistributedMap = new TreeSet<String>();
        valueInDistributedMap.add(value);

        referenceFromMember1.put(key, valueInDistributedMap);
        Thread.sleep(300);
        referenceFromMember2.get(key).add("baz"); // This call is ignored, or simply not updated as it should
        // in HazelCast versions [1.9.1, )
        Thread.sleep(300);

        final Set<String> valueFromMember1 = referenceFromMember1.get(key);
        final Set<String> valueFromMember2 = referenceFromMember2.get(key);

        // Assert
        Assert.assertEquals(1, valueFromMember1.size());
        Assert.assertEquals(1, valueFromMember2.size());
        Assert.assertEquals(value, valueFromMember1.iterator().next());
        Assert.assertEquals(value, valueFromMember2.iterator().next());
    }

    @Ignore("Bugfixed in Hazelcast 3.3.2+")
    @Test
    public void validateOperatingClearInDistributedCollectionWithSortedCollection() {

        // Assemble
        final int numEntities = 150;
        final HazelcastInstance hzInstance = getInternalInstance(hzCache1);

        final Set<DebugSerializationEntity> sortedDistSet = hzInstance.getSet("sortedDistSet");
        final Set<DebugSerializationEntity> unsortedDistSet = hzInstance.getSet("unsortedDistSet");

        final Set<String> sortedCollection = populate(40, new TreeSet<String>(), "entry_");
        final Set<String> unsortedCollection = populate(40, new HashSet<String>(), "entry_");

        // Act
        for (int i = 0; i < numEntities; i++) {
            sortedDistSet.add(new DebugSerializationEntity("Name_" + i, sortedCollection));
            unsortedDistSet.add(new DebugSerializationEntity("Name_" + i, unsortedCollection));
        }

        final int sortedSetSizeBefore = sortedDistSet.size();
        final int unsortedSetSizeBefore = unsortedDistSet.size();

        sortedDistSet.clear();
        unsortedDistSet.clear();

        final int sortedSetSizeAfter = sortedDistSet.size();
        final int unsortedSetSizeAfter = unsortedDistSet.size();

        // Assert
        Assert.assertEquals(numEntities, sortedSetSizeBefore);
        Assert.assertEquals(numEntities, unsortedSetSizeBefore);
        Assert.assertEquals(0, sortedSetSizeAfter);
        Assert.assertEquals(0, unsortedSetSizeAfter);

        System.out.println("unsortedDistSet.clear() missed [" + unsortedSetSizeAfter + "] elements.");
    }

    class SynchronizedEntryListener extends EntryAdapter<String, String> {

        // Internal state
        private final Object[] lock = new Object[0];
        private AtomicInteger counter = new AtomicInteger();
        private SortedMap<String, SortedMap<Integer, String>> threadName2OrderedMessagesMap
                = new TreeMap<String, SortedMap<Integer, String>>();

        @Override
        public void onEntryEvent(final EntryEvent<String, String> event) {
            synchronized (lock) {

                final SortedMap<Integer, String> orderedMessages = getOrderedMessageMap();
                final String message = "onEntryEvent: " + event.getEventType().toString()
                        + " - [" + event.getKey() + "]: " + event.getOldValue() + " --> " + event.getValue();
                orderedMessages.put(counter.getAndIncrement(), message);
            }
        }

        @Override
        public void onMapEvent(final MapEvent event) {
            synchronized (lock) {
                final SortedMap<Integer, String> orderedMessages = getOrderedMessageMap();
                final String message = "onMapEvent: " + event.getEventType().toString()
                        + " - [" + event.getName() + "(" + event.getNumberOfEntriesAffected() + "]: "
                        + event.getEventType();
                orderedMessages.put(counter.getAndIncrement(), message);
            }
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder("\n\n\n######################################\n");
            for (Map.Entry<String, SortedMap<Integer, String>> current : threadName2OrderedMessagesMap.entrySet()) {

                final String threadName = current.getKey();
                for (Map.Entry<Integer, String> currentMessageTuple : current.getValue().entrySet()) {
                    builder.append("(" + threadName + ") [" + currentMessageTuple.getKey() + "]: "
                            + currentMessageTuple.getValue() + "\n");
                }
            }
            return builder.toString() + "######################################\n\n\n";
        }

        public SortedMap<String, SortedMap<Integer, String>> getThreadName2OrderedMessagesMap() {
            return threadName2OrderedMessagesMap;
        }

        private SortedMap<Integer, String> getOrderedMessageMap() {

            final String threadName = Thread.currentThread().getName();

            SortedMap<Integer, String> orderedMessages = threadName2OrderedMessagesMap.get(threadName);
            if (orderedMessages == null) {
                orderedMessages = new TreeMap<Integer, String>();
                threadName2OrderedMessagesMap.put(threadName, orderedMessages);
            }

            return orderedMessages;
        }
    }

    @Test
    public void validateStrangeOrderingInListenerEvents() throws Exception {

        // Assemble
        final String key = "key";
        final String value = "value";
        final String quickEvictionMapId = "quickEvictionMap";
        final int maxCacheSize = 4;
        final int evictionPercentage = 25;

        final Config config = new Config("orderingTestInstance");
        final MapConfig mapConfig = config.getMapConfig(quickEvictionMapId);
        mapConfig.setEvictionPolicy(MapConfig.EvictionPolicy.LFU);
        mapConfig.setEvictionPercentage(evictionPercentage);
        mapConfig.setMaxSizeConfig(new MaxSizeConfig().setSize(maxCacheSize));
        final NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        final HazelcastInstance cache = Hazelcast.getOrCreateHazelcastInstance(config);

        final int expectedRemainingAfterEviction = (int) (mapConfig.getMaxSizeConfig().getSize()
                * (100.0d - (double) mapConfig.getEvictionPercentage()) / 100.0d) - 1;


        final SynchronizedEntryListener listener = new SynchronizedEntryListener();

        // Act
        final IMap<String, String> map = cache.getMap(quickEvictionMapId);
        map.addEntryListener(listener, true);
        Thread.sleep(200);

        for (int i = 0; i < maxCacheSize + 1; i++) {
            map.put(key + "_" + i, value + "_" + i);
        }
        Thread.sleep(200);

        // Assert
        System.out.println(listener.toString());
        Assert.assertEquals(expectedRemainingAfterEviction, map.size());

        final SortedMap<String, SortedMap<Integer, String>> threadName2OrderedMessagesMap
                = listener.getThreadName2OrderedMessagesMap();
        SortedMap<Integer, String> firstThreadWithTwoEvents = null;
        for (Map.Entry<String, SortedMap<Integer, String>> current : threadName2OrderedMessagesMap.entrySet()) {

            // Do we have exactly 2 events recorded in the current Map?
            final SortedMap<Integer, String> currentMap = current.getValue();
            if (currentMap.size() == 2) {
                firstThreadWithTwoEvents = currentMap;
                log.info("Found 2 events for thread [" + current.getKey() + "]");
            }
        }

        final List<String> orderedEvents = new ArrayList<>();
        for (Map.Entry<Integer, String> current : firstThreadWithTwoEvents.entrySet()) {
            orderedEvents.add(current.getValue());
        }

        // Verify the incorrect ordering.
        Assert.assertTrue(orderedEvents.get(0).contains("EVICTED"));
        Assert.assertTrue(orderedEvents.get(1).contains("ADDED"));
    }

    @Test
    public void showSerializationProblem() throws Exception {

        // Assemble
        final Set<String> before1 = populate(20, new HashSet<String>(5), "");
        final Set<String> before2 = populate(20, new TreeSet<String>(), "");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(outputStream);

        // Act
        out.writeObject(before1);
        out.writeObject(before2);
        out.flush();
        out.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        final Set<String> after1 = (Set<String>) in.readObject();
        final Set<String> after2 = (Set<String>) in.readObject();
        Thread.sleep(200);

        // Assert
        Assert.assertNotSame(before1, after1);
        Assert.assertNotSame(before2, after2);

        final List<String> before1List = new ArrayList<String>(before1);
        final List<String> after1List = new ArrayList<String>(after1);

        final List<String> before2List = new ArrayList<String>(before2);
        final List<String> after2List = new ArrayList<String>(after2);

        Assert.assertTrue(before2List.equals(after2List));
        Assert.assertFalse(before1List.equals(after1List));
    }

    //
    // Private helpers
    //


    private Set<String> populate(final int numElements, final Set<String> coll, final String prefix) {

        for (int i = 0; i < numElements; i++) {
            coll.add(prefix + i);
        }

        return coll;
    }

    private void addElements(final Collection<String> coll) {
        coll.add("1");
        coll.add("2");
        coll.add("3");
    }
}
