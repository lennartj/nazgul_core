/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.impl.felix.event;

import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBlueprintServiceEventAdapter implements BlueprintServiceListener {

    public List<BlueprintContainer> callTrace = new ArrayList<BlueprintContainer>();

    // Internal state
    private String id;

    public MockBlueprintServiceEventAdapter(final String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterServiceAdded(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeServiceRemoved(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceModified(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * @return an Identifier, unique within the cluster.
     */
    @Override
    public String getClusterId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(BlueprintServiceListener that) {
        return that == null ? -1 : getClusterId().compareTo(that.getClusterId());
    }
}
