/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.impl.felix;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.osgi.launcher.api.AbstractFrameworkLauncher;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceEventAdapter;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.Map;

/**
 * FrameworkLauncher implementation for the Apache Felix OSGi container.
 * Uses BlueprintServiceListener instances, implying that the DependencyInjection
 * and event mechanism is the OSGi [Aries] Blueprint framework.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FelixLauncher extends AbstractFrameworkLauncher<BlueprintServiceListener> {

    /**
     * {@inheritDoc}
     */
    public FelixLauncher(final IdGenerator idGenerator) {
        super(idGenerator, BlueprintServiceListener.class);
    }

    /**
     * {@inheritDoc}
     */
    public FelixLauncher(final String clusterUniqueID) {
        super(clusterUniqueID, BlueprintServiceListener.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Framework createFramework(final Map<String, String> configuration) throws IllegalArgumentException {
        final FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
        return factory.newFramework(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BundleContextHolder makeBundleContextHolder(final BlueprintServiceListener validEventConsumer) {
        return new BlueprintServiceEventAdapter(validEventConsumer);
    }
}
