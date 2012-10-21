/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi.event;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;

/**
 * Simple delegating BundleListener, delegating all received events to a
 * wrapped EventConsumer.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundleListenerAdapter<T extends EventConsumer<T>>
        extends AbstractEventProducer<T> implements BundleListener, EventConsumer<T> {

    // Internal state
    private BundleListener delegate;

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID   A cluster-unique Identifier.
     * @param eventConsumerType The type of EventConsumer handled by this AbstractEventProducer.
     */
    public BundleListenerAdapter(final String clusterUniqueID,
                                 final Class<T> eventConsumerType,
                                 final BundleListener delegate) {
        super(clusterUniqueID, eventConsumerType);

        Validate.notNull(delegate, "Cannot handle null delegate argument.");
        this.delegate = delegate;
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The {@code BundleEvent}.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        delegate.bundleChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(T o) {
        return new Integer(hashCode()).compareTo(o.hashCode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
