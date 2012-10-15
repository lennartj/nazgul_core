/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.List;

/**
 * EventProducer/EventGenerator specification, which produces a single
 * type of event intended for a single type of EventConsumer.
 *
 * @param <T> The type of EventConsumer which can be registered to this EventProducer.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventProducer<T extends EventConsumer> extends Clusterable {

    /**
     * @return the type of [Event]Consumer handled by this EventProducer.
     */
    Class<T> getConsumerType();

    /**
     * Adds the provided listener to this EventProducer provided that the listener was not already added.
     * Silently ignores adding an already added EventConsumer instance.
     *
     * @param consumer The EventConsumer instance to add.
     */
    void addConsumer(T consumer);

    /**
     * @return a non-null List containing unique identifiers for all known/added EventConsumers.
     * @throws UnsupportedOperationException if this EventProducer cannot not provide the
     *                                       IDs of all known EventConsumers.
     */
    List<String> getConsumerIDs() throws UnsupportedOperationException;

    /**
     * Removes the EventConsumer with the given consumerID from this EventProducer.
     *
     * @param consumerID The unique identifier of the EventConsumer to remove.
     * @return {@code true} if the EventConsumer with the given ID was properly removed,
     *         and {@code false} otherwise.
     */
    boolean removeConsumer(String consumerID);

    /**
     * Acquires the registered EventConsumer with the provided consumerID.
     *
     * @param consumerID The unique identifier for the EventConsumer to retrieve
     * @return the EventConsumer with the given consumerID or {@code null} if none exists.
     */
    T getConsumer(String consumerID);

    /**
     * Perform a callback/notification to all registered EventConsumers using
     * the provided ConsumerEventCallback instance.
     *
     * @param callback an EventConsumer callback method, which cannot be  {@code null}.
     */
    void notifyConsumers(final ConsumerEventCallback<T> callback);
}
