/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.easymock.EasyMock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceEventAdapter;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockFrameworkLauncher extends AbstractFrameworkLauncher<BlueprintServiceListener> {

    // Shared state
    public List<String> callTrace = new ArrayList<String>();
    public Framework fw = EasyMock.createMock(Framework.class);
    public BundleContext ctx = EasyMock.createMock(BundleContext.class);

    public MockFrameworkLauncher(String clusterUniqueID, Class<BlueprintServiceListener> typeClass) {
        super(clusterUniqueID, typeClass);
    }

    public MockFrameworkLauncher(final String clusterUniqueID) {
        super(clusterUniqueID, BlueprintServiceListener.class);
    }

    /**
     * Creates the OSGi Framework instance from the data within the provided configuration.
     *
     * @param configuration The configuration properties given at initialization time to this ContainerEmbedder.
     * @return The initialized - but not started - OSGi Framework reference.
     * @throws IllegalArgumentException if the configuration was deemed inappropriate for
     *                                  initializing the underlying container.
     */
    @Override
    protected Framework createFramework(final Map<String, String> configuration) throws IllegalArgumentException {
        return fw;
    }

    @Override
    protected BundleContextHolder makeBundleContextHolder(final BlueprintServiceListener validEventConsumer) {
        callTrace.add("makeBundleContextHolder [" + validEventConsumer.getId() + "]");
        return new BlueprintServiceEventAdapter(validEventConsumer);
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
        callTrace.add("onStart");
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
        callTrace.add("onStop");
        super.onStop(framework);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void synchronizeListenerRegistration() {
        callTrace.add("synchronizeListenerRegistration");
        super.synchronizeListenerRegistration();
    }
}