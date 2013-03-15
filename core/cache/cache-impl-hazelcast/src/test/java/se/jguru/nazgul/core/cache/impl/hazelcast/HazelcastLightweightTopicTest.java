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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastLightweightTopicTest extends AbstractHazelcastCacheTest {

    // Shared state
    private final String value = "foo";
    private final static String standaloneConfigFile = "config/hazelcast/StandaloneConfig.xml";

    private static HazelcastCacheMember hzCache1;
    private static HazelcastCacheMember hzCache2;

    @BeforeClass
    public static void initialize() {
        configureLogging();

        hzCache1 = getCache(standaloneConfigFile);
        hzCache2 = getCache(standaloneConfigFile);
    }

    @After
    public void after() {
        purgeCache(hzCache1);
        purgeCache(hzCache2);
    }

    @Test
    public void validateHazelcast() {
        hzCache1.getClusterUniqueID();
        hzCache1.getClusterUniqueID();
    }

    @Test
    public void validateLightweightTopicListenerOperationInSingleCacheInstance() throws InterruptedException {

        // Assemble
        final String topicID = hzCache1.getClusterUniqueID();
        final DebugHazelcastLightweightTopicListener listener1
                = new DebugHazelcastLightweightTopicListener("listener1");
        final DebugHazelcastLightweightTopicListener listener2
                = new DebugHazelcastLightweightTopicListener("listener2");

        // Act
        final LightweightTopic<String> distributedTopic = hzCache1.getTopic(topicID);
        distributedTopic.addListener(listener1);
        distributedTopic.addListener(listener2);

        distributedTopic.publish(value);
        Thread.sleep(200);

        // Assert
        final List<String> callTrace1 = listener1.callTrace;
        final List<String> callTrace2 = listener2.callTrace;

        Assert.assertEquals(1, callTrace1.size());
        Assert.assertEquals(1, callTrace2.size());

        Assert.assertEquals(value, callTrace1.get(0));
        Assert.assertEquals(value, callTrace2.get(0));

        Assert.assertEquals(topicID, distributedTopic.getClusterId());
    }

    // TODO A null pointer deep down in hazelcast prevents this test to go through - must look in to!
    //    @Ignore("A null pointer deep down in hazelcast prevents this test to go through - must look in to!")
    @Test
    public void validateLightweightTopicListenerOperationInDistributedCache() throws InterruptedException {

        // Assemble
        final String topicID = hzCache1.getClusterUniqueID();
        final DebugHazelcastLightweightTopicListener unitUnderTest1
                = new DebugHazelcastLightweightTopicListener("unitUnderTest1");
        final DebugHazelcastLightweightTopicListener unitUnderTest2
                = new DebugHazelcastLightweightTopicListener("unitUnderTest2");
        final DebugHazelcastLightweightTopicListener incorrectNotFound
                = new DebugHazelcastLightweightTopicListener("nonexistent");

        final LightweightTopic<String> distributedTopic1 = hzCache1.getTopic(topicID);
        final LightweightTopic<String> distributedTopic2 = hzCache2.getTopic(topicID);
        distributedTopic1.addListener(unitUnderTest1);
        distributedTopic2.addListener(unitUnderTest2);

        // Act
        distributedTopic1.publish(value);
        Thread.sleep(200);

        distributedTopic1.removeListener(unitUnderTest1);
        distributedTopic2.removeListener(unitUnderTest2);
        distributedTopic1.removeListener(incorrectNotFound);
        distributedTopic2.removeListener(incorrectNotFound);

        // Assert
        final List<String> callTrace1 = unitUnderTest1.callTrace;
        final List<String> callTrace2 = unitUnderTest2.callTrace;

        Assert.assertEquals(1, callTrace1.size());
        Assert.assertEquals(1, callTrace2.size());

        Assert.assertEquals(value, callTrace1.get(0));
        Assert.assertEquals(value, callTrace2.get(0));
    }
}
