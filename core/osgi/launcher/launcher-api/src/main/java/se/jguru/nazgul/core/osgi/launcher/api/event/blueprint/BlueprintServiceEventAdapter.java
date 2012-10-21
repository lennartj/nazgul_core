/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.event.api.consumer.AbstractEventConsumer;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;

/**
 * Adapter class, converting OSGI BlueprintContainer Service events to Nazgul BlueprintServiceListener events.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BlueprintServiceEventAdapter extends AbstractEventConsumer<BlueprintServiceEventAdapter>
        implements BundleContextHolder, ServiceListener, EventConsumer<BlueprintServiceEventAdapter> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(BlueprintServiceEventAdapter.class);

    // Internal state
    private BlueprintServiceListener delegate;
    private Filter<ServiceReference> serviceReferenceFilter;

    /**
     * Creates a new BlueprintServiceEventAdapter delegating all BlueprintContainer events to the delegate provided.
     *
     * @param delegate The BlueprintServiceListener delegate to which all relevant events are forwarded.
     */
    public BlueprintServiceEventAdapter(final BlueprintServiceListener delegate) {
        this(delegate, null);
    }

    /**
     * Creates a new BlueprintServiceEventAdapter delegating relevant events to the delegate provided.
     *
     * @param delegate               The BlueprintServiceListener delegate to which all relevant events are forwarded.
     * @param serviceReferenceFilter A filter defining which BlueprintContainerService events should be
     *                               relayed to the given delegate. If {@code null}, all events will be
     *                               relayed.
     */
    public BlueprintServiceEventAdapter(final BlueprintServiceListener delegate,
                                        final Filter<ServiceReference> serviceReferenceFilter) {
        super(delegate.getId());

        // Check sanity
        Validate.notNull(delegate, "Cannot handle null delegate argument.");

        // Assign internal state
        this.delegate = delegate;
        this.serviceReferenceFilter = serviceReferenceFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getId() {
        return delegate.getId();
    }

    /**
     * @return The internal delegate of this BlueprintServiceEventAdapter.
     */
    public final BlueprintServiceListener getDelegate() {
        return delegate;
    }

    /**
     * Registers this BlueprintServiceEventAdapter instance as an OSGI ServiceListener
     * to the supplied BundleContext, for BlueprintContainer ServiceEvents only.
     *
     * @param context The BundleContext which should be registered to this BundleContextHolder.
     */
    @Override
    public final void register(final BundleContext context) {
        try {
            context.addServiceListener(this, BlueprintServiceFilter.getBlueprintContainerServiceLDAPFilter());
        } catch (final InvalidSyntaxException e) {
            throw new IllegalStateException("Incorrect filter syntax", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void unregister(final BundleContext context) {
        context.removeServiceListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final BlueprintServiceEventAdapter o) {
        return o == null ? -1 : getId().compareTo(o.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serviceChanged(final ServiceEvent serviceEvent) {

        // Filter out relevant BlueprintContainer events
        final ServiceReference<?> reference = serviceEvent.getServiceReference();
        if (serviceReferenceFilter != null && !serviceReferenceFilter.accept(reference)) {

            log.debug("ServiceReferenceFilter [" + serviceReferenceFilter.getClass().getName()
                    + "] rejected serviceEvent [" + reference.getProperty(Constants.SERVICE_DESCRIPTION)
                    + "]. Aborting handling.");
            return;
        }

        // Get the BlueprintContainer which gave rise to the event.
        final Bundle localBundle = reference.getBundle();
        final BlueprintContainer blueprintContainer =
                (BlueprintContainer) localBundle.getBundleContext().getService(reference);

        switch (serviceEvent.getType()) {

            case ServiceEvent.REGISTERED:
                delegate.afterServiceAdded(blueprintContainer);
                break;

            case ServiceEvent.UNREGISTERING:
                delegate.beforeServiceRemoved(blueprintContainer);
                break;

            default:
                delegate.onServiceModified(blueprintContainer);
                break;
        }
    }
}
