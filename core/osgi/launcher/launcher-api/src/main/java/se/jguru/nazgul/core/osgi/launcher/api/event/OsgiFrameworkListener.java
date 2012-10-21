/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;

/**
 * Listener specification for receiving callbacks when the OSGi framework
 * emits events for BlueprintContainer services.
 *
 * @param <T> The exact subtype of OsgiFrameworkListener in effect
 * @param <S> The OSGi service object type to subscibe to. This might be
 *            a standard {@code ServiceReference} or a {@code BlueprintContainer}
 *            depending on the implementation.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface OsgiFrameworkListener<T extends EventConsumer<T>, S> extends EventConsumer<T> {

    /**
     * Event callback method invoked after an OSGi service was added to the underlying OSGI Framework.
     *
     * @param serviceObject A reference to the service just added.
     */
    void afterServiceAdded(S serviceObject);

    /**
     * Event callback method invoked before an OSGi service was removed from the underlying OSGI framework.
     *
     * @param container A reference to the service to be removed.
     */
    void beforeServiceRemoved(S container);

    /**
     * Event callback method invoked when an OSGi service was modified within the underlying OSGI framework.
     *
     * @param container A reference to the modified service.
     */
    void onServiceModified(S container);
}
