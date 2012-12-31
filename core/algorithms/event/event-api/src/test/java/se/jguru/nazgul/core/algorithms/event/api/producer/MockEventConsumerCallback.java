/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEvent;
import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEventConsumer;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEventConsumerCallback implements EventConsumerCallback<MockEventConsumer> {

    // Internal state
    private MockEvent event;

    public MockEventConsumerCallback(MockEvent event) {
        this.event = event;
    }

    /**
     * Performs a callback on the provided EventConsumer [subclass] instance.
     *
     * @param eventConsumer The EventConsumer subclass instance.
     */
    @Override
    public void onEvent(final MockEventConsumer eventConsumer) {
        eventConsumer.consume(event);
    }
}
