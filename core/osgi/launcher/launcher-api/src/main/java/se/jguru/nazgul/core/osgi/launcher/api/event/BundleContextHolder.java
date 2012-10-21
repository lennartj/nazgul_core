/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event;

import org.osgi.framework.BundleContext;
import se.jguru.nazgul.core.clustering.api.Clusterable;

/**
 * Event callback specification for managing BundleContext instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BundleContextHolder extends Clusterable {

    /**
     * Registers the provided BundleContext to this BundleContextHolder instance.
     *
     * @param context The BundleContext which should be registered to this BundleContextHolder.
     */
    void register(final BundleContext context);

    /**
     * Un-registers the provided BundleContext from this BundleContextHolder instance.
     *
     * @param context The BundleContext which should be un-registered from this BundleContextHolder.
     */
    void unregister(final BundleContext context);
}
