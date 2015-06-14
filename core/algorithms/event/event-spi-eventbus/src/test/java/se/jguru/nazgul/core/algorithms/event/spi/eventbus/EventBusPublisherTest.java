/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-spi-eventbus
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
package se.jguru.nazgul.core.algorithms.event.spi.eventbus;

import com.google.common.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.event.spi.eventbus.helpers.BarEvent;
import se.jguru.nazgul.core.algorithms.event.spi.eventbus.helpers.BarEventSubscriber;
import se.jguru.nazgul.core.algorithms.event.spi.eventbus.helpers.FooEvent;
import se.jguru.nazgul.core.algorithms.event.spi.eventbus.helpers.FooEventSubscriber;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.clustering.api.UUIDGenerator;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventBusPublisherTest {

    // Shared state
    private IdGenerator idGenerator;

    @Before
    public void setupSharedState() {
        this.idGenerator = new UUIDGenerator();
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullEventBus() {

        // Act & Assert
        new EventBusPublisher(null);
    }

    @Test
    public void validateAddingAndRemovingConsumerLifecycle() {

        // Assemble
        final String uuid = idGenerator.getIdentifier();
        final EventBus bus = new EventBus(uuid);
        final String message = "duh!";

        final FooEventSubscriber subscriber = new FooEventSubscriber();
        final EventBusPublisher unitUnderTest = new EventBusPublisher(bus);

        // Act
        final String subscriberId = unitUnderTest.addConsumer(subscriber);
        unitUnderTest.publish(new FooEvent(message));
        boolean successfullyRemoved = unitUnderTest.removeConsumer(subscriberId);

        // Assert
        Assert.assertNotNull(subscriberId);

        final List<String> callTrace = subscriber.callTrace;
        Assert.assertEquals(1, callTrace.size());
        Assert.assertEquals("Received [" + message + "]", callTrace.get(0));
        Assert.assertTrue(successfullyRemoved);
        Assert.assertEquals(0, unitUnderTest.getConsumerIDs().size());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullConsumer() {

        // Assemble
        final String uuid = idGenerator.getIdentifier();
        final EventBus bus = new EventBus(uuid);
        final EventBusPublisher unitUnderTest = new EventBusPublisher(bus);

        // Act & Assert
        unitUnderTest.addConsumer(null);
    }

    @Test
    public void validateListenerIdManipulations() {

        // Assemble
        final String uuid = idGenerator.getIdentifier();
        final EventBus bus = new EventBus(uuid);

        final FooEventSubscriber subscriber = new FooEventSubscriber();
        final EventBusPublisher unitUnderTest = new EventBusPublisher(bus);

        // Act
        final String subscriberId = unitUnderTest.addConsumer(subscriber);

        // Assert
        Assert.assertNotNull(subscriberId);
        Assert.assertSame(unitUnderTest.getConsumer(subscriberId), subscriber);
        Assert.assertNull(unitUnderTest.getConsumer("someNonexistentConsumerId"));
        Assert.assertFalse(unitUnderTest.removeConsumer("someNonexistentConsumerId"));
    }

    @Test
    public void validateNotAddingConsumerTwice() {

        // Assemble
        final String uuid = idGenerator.getIdentifier();
        final EventBus bus = new EventBus(uuid);

        final FooEventSubscriber subscriber = new FooEventSubscriber();
        final EventBusPublisher unitUnderTest = new EventBusPublisher(bus);

        // Act
        final String subscriberId1 = unitUnderTest.addConsumer(subscriber);
        final String subscriberId2 = unitUnderTest.addConsumer(subscriber);

        // Assert
        Assert.assertEquals(subscriberId1, subscriberId2);
        Assert.assertEquals(1, unitUnderTest.getConsumerIDs().size());
        Assert.assertEquals(subscriberId1, unitUnderTest.getConsumerIDs().get(0));
    }

    @Test
    public void validateMultipleEventTypesAreProperlySeparated() {

        // Assemble
        final String uuid = idGenerator.getIdentifier();
        final EventBus bus = new EventBus(uuid);

        final FooEventSubscriber fooEventSubscriber = new FooEventSubscriber();
        final BarEventSubscriber barEventSubscriber = new BarEventSubscriber("bar!");
        final EventBusPublisher unitUnderTest = new EventBusPublisher(bus);

        // Act
        final String fooSubscriberId = unitUnderTest.addConsumer(fooEventSubscriber);
        final String barSubscriberId = unitUnderTest.addConsumer(barEventSubscriber);

        unitUnderTest.publish(new FooEvent("msg1"));
        unitUnderTest.publish(new BarEvent("msg2"));
        unitUnderTest.publish(new FooEvent("msg3"));
        unitUnderTest.publish(new BarEvent("msg4"));
        unitUnderTest.publish("aStringWhichShouldNotBeAcceptedByAnySubscriber");

        // Assert
        Assert.assertEquals("bar!", barSubscriberId);
        Assert.assertNotNull(fooSubscriberId);
        Assert.assertNotEquals(fooSubscriberId, barSubscriberId);

        final List<String> fooEventCallTrace = fooEventSubscriber.callTrace;
        final List<String> barEventCallTrace = barEventSubscriber.callTrace;

        Assert.assertEquals(2, fooEventCallTrace.size());
        Assert.assertEquals(2, barEventCallTrace.size());

        Assert.assertEquals("Received [msg1]", fooEventCallTrace.get(0));
        Assert.assertEquals("Received [msg3]", fooEventCallTrace.get(1));
        Assert.assertEquals("[msg2]", barEventCallTrace.get(0));
        Assert.assertEquals("[msg4]", barEventCallTrace.get(1));
    }
}
