/*
 * #%L
 * Nazgul Project: nazgul-core-cache-example
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
package se.jguru.nazgul.core.cache.example.event;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.async.DestinationProvider;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.example.AbstractCacheExample;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DistributedEventInCacheExampleTest extends AbstractCacheExample {

    private static final Logger log = LoggerFactory.getLogger(DistributedEventInCacheExampleTest.class);

    @Test
    public void useCase3_createAndUseTopicForAsyncCallback() throws InterruptedException {

        // Acquire the cache.
        final DestinationProvider<String> cache = getCache();

        // 1: Get a LightweightTopic, which works like a JMS topic
        //    connected to the DistributedCache. The generic on the
        //    LightweightTopic declaration shows the type of message
        //    that the LightweightTopic relays.
        final String distTopicID = "aClusterUniqueIdForTheDistributedTopic";
        final LightweightTopic<ExampleConfigurationChangeEvent> configurationChangeTopic = cache.getTopic(distTopicID);

        // 2: Create and add two LightweightTopicListener instances
        //    to the distributed LightweightTopic.
        //
        //    Note that these listeners can be registered in different
        //    JVMs - as long as they are within the same distributed
        //    cache cluster. We will use IDs to indicate this fact.
        final ExampleConfigurationChangeListener l1 =
                new ExampleConfigurationChangeListener("listenerWithinAnApplicationServer");
        final ExampleConfigurationChangeListener l2 =
                new ExampleConfigurationChangeListener("standaloneSwingApplicationListener");
        configurationChangeTopic.addListener(l1);
        configurationChangeTopic.addListener(l2);

        // 3: Let's simulate a change in configuration. The configuration
        //    change data is wrapped within an event object and sent to
        //    the configuration topic.
        configurationChangeTopic.publish(new ExampleConfigurationChangeEvent<String>(
                "fooProperty", "newBar", "oldBar"));

        // 4: Validate in the that the two listeners are invoked once each by the topic.
        Thread.sleep(500l);
        validateThatOneMessageWasReceived(l1, "fooProperty", "oldBar", "newBar");
        validateThatOneMessageWasReceived(l2, "fooProperty", "oldBar", "newBar");
    }

    private void validateThatOneMessageWasReceived(final ExampleConfigurationChangeListener listener,
                                                  final String expectedPropertyName,
                                                  final String expectedOldValue,
                                                  final String expectedNewValue) {

        final List<ExampleConfigurationChangeEvent<String>> receivedEvents = listener.getReceivedEvents();
        Assert.assertEquals(1, receivedEvents.size());

        final ExampleConfigurationChangeEvent<String> event = receivedEvents.get(0);
        Assert.assertEquals(expectedPropertyName, event.getPropertyName());
        Assert.assertEquals(expectedOldValue, event.getOldValue());
        Assert.assertEquals(expectedNewValue, event.getNewValue());
    }
}
