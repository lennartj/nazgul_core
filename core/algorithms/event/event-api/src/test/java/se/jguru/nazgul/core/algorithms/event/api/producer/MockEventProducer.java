/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEventConsumer;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEventProducer extends AbstractEventProducer<MockEventConsumer> {

    /**
     * Creates a new AbstractEventProducer with the provided IdGenerator and EventConsumer type.
     *
     * @param idGenerator The ID generator used to acquire a cluster-unique identifier for
     *                    this AbstractEventProducer instance.
     */
    public MockEventProducer(final IdGenerator idGenerator) {
        super(idGenerator, MockEventConsumer.class);
    }

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID A cluster-unique Identifier.
     */
    public MockEventProducer(final String clusterUniqueID) {
        super(clusterUniqueID, MockEventConsumer.class);
    }
}
