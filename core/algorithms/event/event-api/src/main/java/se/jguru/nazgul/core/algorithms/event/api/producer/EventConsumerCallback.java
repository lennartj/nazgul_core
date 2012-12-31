/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;

import java.io.Serializable;

/**
 * Specification for how to perform an event callback on an EventConsumer.
 * Pattern-wise, this is a Visitor.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventConsumerCallback<T extends EventConsumer> extends Serializable {

    /**
     * Performs a callback on the provided EventConsumer [subclass] instance.
     *
     * @param eventConsumer The EventConsumer subclass instance.
     */
    void onEvent(T eventConsumer);
}
