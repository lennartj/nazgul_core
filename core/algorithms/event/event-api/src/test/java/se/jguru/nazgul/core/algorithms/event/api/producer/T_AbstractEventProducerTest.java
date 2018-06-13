/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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


package se.jguru.nazgul.core.algorithms.event.api.producer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEvent;
import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEventConsumer;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractEventProducerTest {

    // Shared state
    private String producerIdentifier = "uniqueProducerId";
    private String consumerIdentifier = "uniqueConsumerId";
    private MockEventProducer unitUnderTest;
    private MockEventConsumer consumer;

    @Before
    public void setupSharedState() {
        unitUnderTest = new MockEventProducer(producerIdentifier);
        consumer = new MockEventConsumer(consumerIdentifier);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullIdentifier() {

        // Assemble
        final String incorrectNull = null;

        // Act & Assert
        new MockEventProducer(incorrectNull);
    }

    @Test
    public void validateNoExceptionOnAddingNullEventConsumer() {

        // Assemble
        final MockEventConsumer incorrectNull = null;

        // Act
        unitUnderTest.addConsumer(incorrectNull);

        // Assert
        Assert.assertEquals(0, unitUnderTest.getConsumerIDs().size());
        Assert.assertEquals(MockEventConsumer.class, unitUnderTest.getConsumerType());
    }

    @Test
    public void validateEventConsumerLifecycle() {

        // Assemble

        // Act & Assert #1
        List<String> consumerIDs = unitUnderTest.getConsumerIDs();
        Assert.assertNotNull(consumerIDs);
        Assert.assertEquals(0, consumerIDs.size());

        // Act & Assert #2
        unitUnderTest.addConsumer(consumer);
        consumerIDs = unitUnderTest.getConsumerIDs();
        Assert.assertEquals(1, consumerIDs.size());
        Assert.assertEquals(consumerIdentifier, unitUnderTest.getConsumerIDs().get(0));
        Assert.assertSame(consumer, unitUnderTest.getConsumer(consumerIdentifier));

        // Act & Assert #3
        unitUnderTest.removeConsumer(consumerIdentifier);
        consumerIDs = unitUnderTest.getConsumerIDs();
        Assert.assertEquals(0, consumerIDs.size());
    }

    @Test
    public void validateNotAddingSameConsumerTwice() {

        // Assemble

        // Act
        unitUnderTest.addConsumer(consumer);
        unitUnderTest.addConsumer(consumer);
        List<String> consumerIDs = unitUnderTest.getConsumerIDs();

        // Assert
        Assert.assertEquals(1, consumerIDs.size());
        Assert.assertEquals(consumerIdentifier, unitUnderTest.getConsumerIDs().get(0));
        Assert.assertSame(consumer, unitUnderTest.getConsumer(consumerIdentifier));
    }

    @Test
    public void validateNoExceptionOnRemovingNullOrEmptyIdConsumer() {

        // Assemble
        unitUnderTest.addConsumer(consumer);

        // Act & Assert
        unitUnderTest.removeConsumer(null);
        unitUnderTest.removeConsumer("");
    }

    @Test
    public void validateRemovingNonExistentConsumer() {

        // Assemble
        final String anotherID = "anotherTestId";
        unitUnderTest.addConsumer(consumer);

        // Act
        boolean result1 = unitUnderTest.removeConsumer(anotherID);
        boolean result2 = unitUnderTest.removeConsumer(consumerIdentifier);

        // Assert
        Assert.assertFalse(result1);
        Assert.assertTrue(result2);
    }

    @Test
    public void validateEventCallback() {

        // Assemble
        unitUnderTest.addConsumer(consumer);

        final MockEvent event1 = new MockEvent("event1");
        final MockEvent event2 = new MockEvent("event2");

        // Act
        unitUnderTest.notifyConsumers(new MockEventConsumerCallback(event1));
        unitUnderTest.notifyConsumers(new MockEventConsumerCallback(event2));

        // Assert
        final List<String> callTrace = consumer.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals("consume [event1]", callTrace.get(0));
        Assert.assertEquals("consume [event2]", callTrace.get(1));
    }
}
