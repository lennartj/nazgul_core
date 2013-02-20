/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopicListener;
import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;

/**
 * Adapter delegating Hazelcast events to the generic cache-api
 * specification type LightweightTopicListener.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastLightweightTopicListenerAdapter<MessageType extends Serializable>
        implements MessageListener<MessageType>, Clusterable {

    // Internal state
    private LightweightTopicListener<MessageType> delegate;

    /**
     * Creates a new HazelcastLightweightTopicListenerAdapter that adapts
     * Hazelcast MessageListener to the API of the LightweightTopicListener.
     *
     * @param listener The LightweightTopicListener to use for event delegation.
     */
    public HazelcastLightweightTopicListenerAdapter(final LightweightTopicListener<MessageType> listener) {
        this.delegate = listener;
    }

    /**
     * Invoked when a message is received for the added topic. Note that topic guarantees message ordering.
     * Therefore there is only one thread invoking onMessage. The user shouldn't keep the thread busy and preferably
     * dispatch it via an Executor. This will increase the performance of the topic.
     *
     * @param messageTypeMessage received message
     */
    @Override
    public void onMessage(final Message<MessageType> messageTypeMessage) {
        delegate.onMessage(messageTypeMessage.getMessageObject());
    }

    /**
     * @return a human-readable, cluster-unique Identifier for this cache instance.
     */
    @Override
    public String getId() {
        return delegate.getId();
    }
}
