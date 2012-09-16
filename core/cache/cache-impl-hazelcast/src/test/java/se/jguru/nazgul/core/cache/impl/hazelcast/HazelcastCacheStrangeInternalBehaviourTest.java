/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * This testcase is *very important* in that it tracks how the currently used release of
 * AbstractHazelcastInstanceWrapper behaves internally for certain actions.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheStrangeInternalBehaviourTest extends AbstractHazelcastCacheTest {

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
        final String[] expected = { value, "baz" };
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
        Assert.assertTrue(unsortedSetSizeAfter > 0);

        System.out.println("unsortedDistSet.clear() missed [" + unsortedSetSizeAfter + "] elements.");
    }

    /*
     * public void validateBrokenClearInDistributedCollection() { // Assemble final int numEntities = 150; final Config
     * conf = AbstractHazelcastInstanceWrapper.readConfigFile("config/hazelcast/StandaloneConfig.xml"); final
     * HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(conf); final Set<CollectionWrappingEntity>
     * distSetWithSortedCollection = hzInstance.getSet("distSetWithSortedCollection"); final
     * Set<CollectionWrappingEntity> distSetWithUnsortedCollection = hzInstance.getSet("distSetWithUnsortedCollection");
     * final Collection<CollectionWrappingEntity> distCollectionWithSortedCollection =
     * hzInstance.getList("distCollectionWithSortedCollection"); final Collection<CollectionWrappingEntity>
     * distCollectionWithUnsortedCollection = hzInstance.getList("distCollectionWithUnsortedCollection"); final
     * Queue<CollectionWrappingEntity> distQueueWithSortedCollection =
     * hzInstance.getQueue("distQueueWithSortedCollection"); final Queue<CollectionWrappingEntity>
     * distQueueWithUnsortedCollection = hzInstance.getQueue("distQueueWithUnsortedCollection"); final Set<String>
     * sortedCollection = new TreeSet<String>(); final Set<String> unsortedCollection = new HashSet<String>(); final
     * List<String> list1 = new ArrayList<String>(); final List<String> list2 = new LinkedList<String>(); for(int i = 0;
     * i < numEntities; i++) { sortedCollection.add("entry_" + i); unsortedCollection.add("entry_" + i);
     * list1.add("entry_" + i); list2.add("entry_" + i); } // Act for (int i = 0; i < numEntities; i++) {
     * distSetWithSortedCollection.add(new CollectionWrappingEntity("Name_" + i, sortedCollection));
     * distSetWithUnsortedCollection.add(new CollectionWrappingEntity("Name_" + i, unsortedCollection));
     * distCollectionWithSortedCollection.add(new DebugSerializationEntity("name_" + i, i, friends));
     * distQueueWithSortedCollection.add(new DebugSerializationEntity("name_" + i, i, friends)); } final int setSize =
     * distSetWithSortedCollection.size(); final int collSize = distCollectionWithSortedCollection.size(); final int
     * queueSize = distQueueWithSortedCollection.size(); distSetWithSortedCollection.clear();
     * distCollectionWithSortedCollection.clear(); distQueueWithSortedCollection.clear(); // Assert
     * Assert.assertEquals(numEntities, setSize); Assert.assertEquals(numEntities, collSize);
     * Assert.assertEquals(numEntities, queueSize); Assert.assertEquals(0, distSetWithSortedCollection.size());
     * Assert.assertEquals(0, distCollectionWithSortedCollection.size()); Assert.assertEquals(0,
     * distQueueWithSortedCollection.size()); }
     */

    //
    // Private helpers
    //

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
