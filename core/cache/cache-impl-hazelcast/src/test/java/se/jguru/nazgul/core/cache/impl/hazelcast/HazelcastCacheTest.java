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

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.GridOperations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheTest extends AbstractHazelcastCacheTest {

    private static final String TEST_DIST_MAP = "testDistMap";
    // Shared state
    private final String key = "foo";
    private final String value = "bar";
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
    public void validateExceptionOnNonexistentConfiguration() {
        // Act & Assert
        HazelcastCacheMember.readConfigFile("some/nonexistent/config");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNullConfiguration() {
        // Act & Assert
        HazelcastCacheMember.readConfigFile(null);
    }

    @Test
    public void validateDirectCachingInSingleCacheInstance() {

        // Assemble
        final String cacheIDStart = "_hzInstance_";
        final String cacheIDEnd = "_unittest-cache-group";

        // Act
        final Serializable previous = hzCache1.put(key, value);
        final Serializable result = hzCache1.get(key);

        // Assert
        Assert.assertNull(previous);
        Assert.assertTrue(hzCache1.containsKey(key));
        Assert.assertEquals(value, result);
        Assert.assertTrue(hzCache1.getClusterId().startsWith(cacheIDStart));
        Assert.assertTrue(hzCache1.getClusterId().endsWith(cacheIDEnd));
    }


    @Test
    public void validateDirectCachingInDistributedCache() {

        // Act
        final Serializable previous = hzCache1.put(key, value);
        final Serializable result = hzCache2.get(key);

        // Assert
        Assert.assertNotSame(hzCache1, hzCache2);
        Assert.assertTrue(hzCache1.containsKey(key));
        Assert.assertTrue(hzCache2.containsKey(key));
        Assert.assertNull(previous);
        Assert.assertEquals(value, result);
    }

    @Test
    public void validateDistributedMapInDistributedCache() {

        // Act
        final Map<String, String> distMap1 = hzCache1.getDistributedMap(TEST_DIST_MAP);
        final Serializable previous = distMap1.put(key, value);
        final Serializable result = hzCache2.getDistributedMap(TEST_DIST_MAP).get(key);

        // Assert
        Assert.assertNotNull(distMap1);
        Assert.assertNull(previous);
        Assert.assertEquals(value, result);
    }

    @Test
    public void validateFaultToleranceInDistributedCache() throws InterruptedException {

        // Act
        final Map<String, String> distMap1 = hzCache1.getDistributedMap(TEST_DIST_MAP);
        final Serializable previous = distMap1.put(key, value);

        hzCache1.stopCache();
        Thread.sleep(200);      // Wait for the cache instance to make a full stop.

        final Serializable result = hzCache2.getDistributedMap(TEST_DIST_MAP).get(key);

        // Restore cache instance
        hzCache1 = getCache(configFile);

        // Assert
        Assert.assertNotNull(distMap1);
        Assert.assertNull(previous);
        Assert.assertEquals(value, result);
    }

    @Test
    public void validateFaultToleranceDuringAdminShutdown() throws InterruptedException {

        // Act
        final Map<String, String> distMap1 = hzCache1.getDistributedMap(TEST_DIST_MAP);
        final Serializable previous = distMap1.put(key, value);

        LightweightTopic<AdminMessage> adminTopic = hzCache1.getTopic(GridOperations.CLUSTER_ADMIN_TOPIC);

        adminTopic.publish(AdminMessage.createShutdownInstanceMessage(hzCache1.getClusterId()));
        Thread.sleep(200);      // Wait for the cache instance to make a full stop.

        final Serializable result = hzCache2.getDistributedMap(TEST_DIST_MAP).get(key);

        // Restore cache instance
        hzCache1 = getCache(configFile);

        // Assert
        Assert.assertNotNull(distMap1);
        Assert.assertNull(previous);
        Assert.assertEquals(value, result);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnIllegalListenerInstanceType() {

        // Assemble
        final HazelcastInstance internalInstance = getInternalInstance(hzCache1);
        final DebugCacheListener listener1 = new DebugCacheListener("listener_1");

        // Act & Assert
        final AtomicNumber number = internalInstance.getAtomicNumber("foo");
        hzCache1.addListenerFor(number, listener1);
    }

    @Test
    public void validateHazelcastTransactionRollback() {

        // Assemble
        final HazelcastInstance internalInstance = getInternalInstance(hzCache1);
        final DebugCacheListener listener1 = new DebugCacheListener("listener_validateHazelcastTransactionRollback");
        final String expectedErrorMessage = "Will not add listener to an instance of type [ATOMIC_NUMBER]. "
                + "Supported types are [LIST, SET, QUEUE, MAP].";

        // Act
        final AtomicNumber number = internalInstance.getAtomicNumber("foo");
        String errorMessage = null;

        try {
            hzCache1.addListenerFor(number, listener1);
        } catch (final IllegalStateException e) {

            // Expected
            errorMessage = e.getCause().getMessage();

        } catch (final Exception e) {
            Assert.fail("Expected IllegalStateException, but received " + e.getClass().getName());
        }

        // Assert
        Assert.assertEquals(expectedErrorMessage, errorMessage);
    }

    @Test
    public void validateIterable() {

        // Assemble
        final Map<String, String> data = new HashMap<String, String>();
        data.put("fooo", "bar");
        data.put("gnat", "baz");

        for(String current : data.keySet()) {
            hzCache1.put(current, data.get(current));
        }

        // Act & Assert
        for(String current : hzCache1) {
            Assert.assertEquals(hzCache1.get(current), data.get(current));
        }
    }
}
