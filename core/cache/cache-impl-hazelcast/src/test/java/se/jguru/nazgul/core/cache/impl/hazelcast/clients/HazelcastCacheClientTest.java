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

import com.hazelcast.client.ClientConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastCacheTest;
import se.jguru.nazgul.core.cache.impl.hazelcast.DebugCacheListener;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheClientTest extends AbstractHazelcastCacheTest {

    // Shared config
    final String key = "foo";
    final String value = "bar";
    private static final String configFile = "config/hazelcast/StandaloneConfig.xml";

    private static HazelcastCacheMember hzCache1;
    private static HazelcastCacheClient cacheClient;

    @BeforeClass
    public static void initialize() {
        configureLogging();

        hzCache1 = getCache(configFile);

        final ClientConfig clientConfig = HazelcastCacheClient.getClientConfig(
                "unittest-cache-group", "unittest-pass");
        clientConfig.addInetSocketAddress(new InetSocketAddress("localhost", 5701));

        cacheClient = new HazelcastCacheClient(clientConfig);
    }

    @After
    public void after() {
        purgeCache(cacheClient);
        purgeCache(hzCache1);
    }

    @Test
    public void validateCacheClientOperations() {
        // Act
        final Serializable before = hzCache1.put(key, value);
        final Serializable valueInClient = cacheClient.get(key);
        final Serializable removedValueInClient = cacheClient.remove(key);
        final Serializable afterInCache = hzCache1.get(key);

        // Assert
        // Assert.assertTrue(successAddition);
        Assert.assertNull(before);
        Assert.assertEquals(value, valueInClient);
        Assert.assertEquals(value, removedValueInClient);
        Assert.assertNull(afterInCache);
    }

    @Test
    public void validateExceptionOnCacheClientListenerAddition() {

        // Assemble
        final DebugCacheListener cacheListener = new DebugCacheListener("cacheClientListener");

        // Act
        final boolean successAddition = cacheClient.addListener(cacheListener);
        final Serializable before = hzCache1.put(key, value);
        final Serializable valueInClient = cacheClient.get(key);
        final Serializable removedValueInClient = cacheClient.remove(key);
        final Serializable afterInCache = hzCache1.get(key);

        // Assert
        Assert.assertTrue(successAddition);
        Assert.assertNull(before);
        Assert.assertEquals(value, valueInClient);
        Assert.assertEquals(value, removedValueInClient);
        Assert.assertNull(afterInCache);

        final TreeMap<Integer, DebugCacheListener.EventInfo> events = cacheListener.eventId2KeyValueMap;
        System.out.println("Got: " + events);
    }
}
