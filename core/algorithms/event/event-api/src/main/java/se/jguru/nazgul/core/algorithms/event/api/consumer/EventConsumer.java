/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.EventListener;

/**
 * EventConsumer/EventListener specification with callback methods invoked when events occur.
 * EventConsumer instances should function correctly in a clustered environment, and must also
 * be Comparable to enforce a natural ordering and comparison between instances.
 *
 * @param <E> The exact subtype of EventConsumer in effect
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see EventListener
 */
public interface EventConsumer<E extends EventConsumer<E>> extends EventListener, Comparable<E>, Clusterable {
}
