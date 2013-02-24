/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopicListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugHazelcastLightweightTopicListener implements LightweightTopicListener<String> {

    // Internal sate
    public String id;
    public List<String> callTrace = new ArrayList<String>();

    public DebugHazelcastLightweightTopicListener(String id) {
        this.id = id;
    }

    /**
     * Callback method invoked by the underlying LightweightTopic whenever
     * a message is passed through the Topic(s) where this LightweightTopicListener
     * is registered.
     *
     * @param message The message received.
     */
    @Override
    public void onMessage(String message) {
        callTrace.add(message);
    }

    /**
     * @return a human-readable, cluster-unique Identifier for this cache instance.
     */
    @Override
    public String getClusterId() {
        return id;
    }
}
