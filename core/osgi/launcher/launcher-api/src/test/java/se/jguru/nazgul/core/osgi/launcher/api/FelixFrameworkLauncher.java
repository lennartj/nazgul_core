/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceEventAdapter;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.Map;

/**
 * Framework launcher for Felix.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FelixFrameworkLauncher extends AbstractFrameworkLauncher<BlueprintServiceListener> {

    /**
     * {@inheritDoc}
     */
    public FelixFrameworkLauncher(final String id) {
        super(id, BlueprintServiceListener.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Framework createFramework(Map<String, String> configuration) throws IllegalArgumentException {
        final FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
        return factory.newFramework(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BundleContextHolder makeBundleContextHolder(BlueprintServiceListener validEventConsumer) {
        return new BlueprintServiceEventAdapter(validEventConsumer);
    }
}
