/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;

/**
 * Lightweight Topic definition, implying a distributed cluster-wide Topic
 * which exists within the context of a distributed cache.
 *
 * @param <MessageType> T
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LightweightTopic<MessageType extends Serializable> extends Clusterable {

    /**
     * Sends/Publishes the provided message through this LightweightTopic.
     *
     * @param message The message to publish in this Topic.
     */
    void publish(MessageType message);

    /**
     * Adds a new LightweightTopicListener to this Topic.
     * The LightweightTopicListener is invoked whenever a message
     * is sent through this LightweightTopic.
     *
     * @param listener The LightweightTopicListener to register to this Topic.
     */
    void addListener(LightweightTopicListener<MessageType> listener);

    /**
     * Removes the provided LightweightTopicListener from this LightweightTopic.
     *
     * @param listener The listener to remove.
     */
    void removeListener(LightweightTopicListener<MessageType> listener);
}
