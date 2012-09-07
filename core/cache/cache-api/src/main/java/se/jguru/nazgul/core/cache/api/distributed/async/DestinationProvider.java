/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;

import java.io.Serializable;

/**
 * Service interface definition for a distributed (clustered) cache which
 * provides Destinations leading into a distributed cache.
 *
 * @param <KeyType> The type of Keys used within the DistributedCache.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DestinationProvider<KeyType extends Serializable> extends DistributedCache<KeyType> {

    /**
     * Retrieves a LightweightTopic with the provided topicId from
     * the DestinationProvider. Depending on the capabilities of the underlying
     * implementation, the topic can be dynamically
     *
     * @param <MessageType> The type of message transmitted by this LightweightTopic.
     * @param topicId       The ID of the LightweightTopic to retrieve.
     * @return The LightweightTopic with the provided topicId.
     */
    <MessageType extends Serializable> LightweightTopic<MessageType> getTopic(String topicId);
}
