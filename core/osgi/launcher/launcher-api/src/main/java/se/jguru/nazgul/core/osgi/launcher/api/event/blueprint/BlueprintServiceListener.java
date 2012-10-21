/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.osgi.launcher.api.event.OsgiFrameworkListener;

/**
 * OsgiFrameworkListener specification for listening to BlueprintContainer events.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BlueprintServiceListener<T extends EventConsumer<T>>
        extends OsgiFrameworkListener<T, BlueprintContainer> {

    /**
     * Event callback method invoked after a BlueprintContainer service
     * was added to the underlying OSGI Framework.
     *
     * @param serviceObject The BlueprintContainer instance just added.
     */
    @Override
    void afterServiceAdded(BlueprintContainer serviceObject);

    /**
     * Event callback method invoked before a BlueprintContainer service
     * is removed from the underlying OSGI Framework.
     *
     * @param container A reference to the BlueprintContainer instance to be removed.
     */
    @Override
    void beforeServiceRemoved(BlueprintContainer container);

    /**
     * Event callback method invoked when a BlueprintContainer service
     * is updated within the underlying OSGI Framework.
     *
     * @param container A reference to the modified service.
     */
    @Override
    void onServiceModified(BlueprintContainer container);
}
