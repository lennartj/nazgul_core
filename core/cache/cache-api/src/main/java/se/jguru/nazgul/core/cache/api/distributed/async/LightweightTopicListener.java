/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;

/**
 * Specification for a listener on a LightweightTopic. Similar to a JMS
 * MessageListener, this LightweightTopicListener will be invoked whenever
 * a Message is published on the underlying topic.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LightweightTopicListener<MessageType extends Serializable> extends Clusterable {

    /**
     * Callback method invoked by the underlying LightweightTopic whenever
     * a message is passed through the Topic(s) where this LightweightTopicListener
     * is registered.
     *
     * @param message The message received.
     */
    void onMessage(MessageType message);
}
