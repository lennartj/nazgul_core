/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import java.util.Map;

/**
 * Abstract implementation of the FrameworkLauncher interface, which
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AbstractFrameworkLauncher implements FrameworkLauncher {

    // Internal state
    /**
     * The OSGi Framework reference.
     */
    protected Framework framework;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Map<String, Object> configuration) throws IllegalStateException {
    }

    /**
     * {@inheritDoc} Delegates processing to the {@code onStart} method.
     */
    @Override
    public final void start() throws BundleException, IllegalStateException {

        if (framework == null) {
            throw new IllegalStateException("OSGi Framework not created. Did you initialize it?");
        }

        // Start the osgiFramework.
        framework.start();

        // Synchronize listeners
        synchronizeListenerRegistration();

        // Cater for custom event implementation.
        onStart(framework);
    }

    /**
     * Custom handler for the start event - override this method
     * if you need to implement some custom logic when the Framework
     * is started.
     *
     * @param framework The just started osgiFramework instance.
     */
    protected void onStart(final Framework framework) {
        // Default implementation does nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Framework getFramework() {
        return framework;
    }

    /**
     * {@inheritDoc} Delegates processing to the {@code onStop} method.
     */
    @Override
    public final void stop() throws BundleException, IllegalStateException {

        if (framework == null) {
            throw new IllegalStateException("OSGi Framework not created. Did you initialize it?");
        }

        // Stop the framework
        framework.stop();

        // Synchronize listeners
        synchronizeListenerRegistration();

        // Cater for custom event implementation.
        onStop(framework);
    }

    /**
     * Custom handler for the stop event - override this method
     * if you need to implement some custom logic after the Framework
     * is stopped.
     *
     * @param framework The just stopped OSGi Framework.
     */
    protected void onStop(final Framework framework) {
        // Default implementation does nothing.
    }

    /**
     * {@inheritDoc}
     */
    public void synchronizeListenerRegistration() {
    }
}
