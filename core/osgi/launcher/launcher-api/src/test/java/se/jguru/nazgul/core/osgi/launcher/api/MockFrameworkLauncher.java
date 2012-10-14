/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.osgi.framework.launch.Framework;

import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockFrameworkLauncher extends AbstractFrameworkLauncher {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Map<String, Object> configuration) throws IllegalStateException {
        super.initialize(configuration);
    }

    /**
     * Custom handler for the start event - override this method
     * if you need to implement some custom logic when the Framework
     * is started.
     *
     * @param framework The just started osgiFramework instance.
     */
    @Override
    protected void onStart(Framework framework) {
        super.onStart(framework);
    }

    /**
     * Custom handler for the stop event - override this method
     * if you need to implement some custom logic after the Framework
     * is stopped.
     *
     * @param framework The just stopped OSGi Framework.
     */
    @Override
    protected void onStop(Framework framework) {
        super.onStop(framework);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void synchronizeListenerRegistration() {
        super.synchronizeListenerRegistration();
    }
}
