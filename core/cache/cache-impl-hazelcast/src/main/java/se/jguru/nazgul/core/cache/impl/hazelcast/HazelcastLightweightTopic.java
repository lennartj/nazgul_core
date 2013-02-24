/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.ITopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopicListener;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * LightweightTopic implementation for Hazelcast.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastLightweightTopic<MessageType extends Serializable> implements LightweightTopic<MessageType> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HazelcastLightweightTopic.class);

    // Internal state
    private final Object lock = new Object();
    private ITopic<MessageType> hazelcastTopic;
    private Map<String, HazelcastLightweightTopicListenerAdapter<MessageType>> knownListeners;

    /**
     * Creates a new HazelcastLightweightTopic (LightweightTopic wrapper), delegating its calls to the provided ITopic
     * instance.
     *
     * @param hazelcastTopic The ITopic received from Hazelcast.
     */
    public HazelcastLightweightTopic(final ITopic<MessageType> hazelcastTopic) {

        this.hazelcastTopic = hazelcastTopic;
        knownListeners = new TreeMap<String, HazelcastLightweightTopicListenerAdapter<MessageType>>();
    }

    /**
     * Sends/Publishes the provided message through this LightweightTopic.
     *
     * @param message The message to publish in this Topic.
     */
    @Override
    public void publish(final MessageType message) {
        hazelcastTopic.publish(message);
    }

    /**
     * Adds a new LightweightTopicListener to this Topic. The LightweightTopicListener is invoked whenever a message is
     * sent through this LightweightTopic.
     *
     * @param listener The LightweightTopicListener to register to this Topic.
     */
    @Override
    public void addListener(final LightweightTopicListener<MessageType> listener) {

        synchronized (lock) {
            final HazelcastLightweightTopicListenerAdapter<MessageType> lst =
                    new HazelcastLightweightTopicListenerAdapter<MessageType>(listener);

            // Add to map and topic
            knownListeners.put(lst.getClusterId(), lst);
            hazelcastTopic.addMessageListener(lst);
        }
    }

    /**
     * Removes the provided LightweightTopicListener from this LightweightTopic.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeListener(final LightweightTopicListener<MessageType> listener) {

        // Check sanity.
        if (listener == null) {
            throw new IllegalArgumentException("Cannot handle null listener argument.");
        }

        // Get the Wrapper
        final HazelcastLightweightTopicListenerAdapter<MessageType> toRemove = knownListeners.get(listener.getClusterId());

        if (toRemove == null) {
            log.warn("HazelcastLightweightTopicListenerAdapter [" + listener.getClusterId() + "] not found. Aborting.");
            return;
        }

        synchronized (lock) {
            hazelcastTopic.removeMessageListener(knownListeners.remove(listener.getClusterId()));
        }
    }

    /**
     * @return a human-readable, cluster-unique Identifier for this instance.
     */
    @Override
    public String getClusterId() {
        return hazelcastTopic.getName();
    }
}
