/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi.event;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ServiceListenerAdapter<T extends EventConsumer<T>> extends AbstractEventProducer<T>
        implements ServiceListener, EventConsumer<T> {

    // Internal state
    private ServiceListener delegate;

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID    A cluster-unique Identifier.
     * @param eventConsumerClass The type of EventConsumer handled by this AbstractEventProducer.
     * @param delegate           The ServiceListener to which all events will be delegated.
     */
    public ServiceListenerAdapter(final String clusterUniqueID,
                                  final Class<T> eventConsumerClass,
                                  final ServiceListener delegate) {
        super(clusterUniqueID, eventConsumerClass);
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final T that) {
        return Integer.valueOf(hashCode()).compareTo(that.hashCode());
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public void serviceChanged(final ServiceEvent event) {
        delegate.serviceChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
