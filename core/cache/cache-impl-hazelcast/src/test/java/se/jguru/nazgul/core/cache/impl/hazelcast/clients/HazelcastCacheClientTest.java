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
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.client.config.ClientConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastCacheTest;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.DebugCacheListener;
import se.jguru.nazgul.core.cache.impl.hazelcast.helpers.EventInfo;

import java.util.Arrays;
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
        clientConfig.getNetworkConfig().setAddresses(Arrays.asList("localhost:5701"));
        // .addInetSocketAddress(new InetSocketAddress("localhost", 5701));

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
        final Object before = hzCache1.put(key, value);
        final Object valueInClient = cacheClient.get(key);
        final Object removedValueInClient = cacheClient.remove(key);
        final Object afterInCache = hzCache1.get(key);

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
        final DebugCacheListener<Object> cacheListener = new DebugCacheListener<Object>("cacheClientListener");

        // Act
        final boolean successAddition = cacheClient.addListener(cacheListener);
        final Object before = hzCache1.put(key, value);
        final Object valueInClient = cacheClient.get(key);
        final Object removedValueInClient = cacheClient.remove(key);
        final Object afterInCache = hzCache1.get(key);

        // Assert
        Assert.assertTrue(successAddition);
        Assert.assertNull(before);
        Assert.assertEquals(value, valueInClient);
        Assert.assertEquals(value, removedValueInClient);
        Assert.assertNull(afterInCache);

        final TreeMap<Integer, EventInfo> events = cacheListener.eventId2EventInfoMap;
        System.out.println("Got: " + events);
    }
}
