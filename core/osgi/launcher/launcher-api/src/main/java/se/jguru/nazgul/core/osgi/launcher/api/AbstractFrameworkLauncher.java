/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;
import se.jguru.nazgul.core.clustering.api.ConstantIdGenerator;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.OsgiFrameworkListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of the FrameworkLauncher interface, which caters
 * for registering and deregistering EventConsumers (in the form of
 * OsgiFrameworkListener instances) to OSGi ServiceEvents.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractFrameworkLauncher<T extends OsgiFrameworkListener> extends AbstractEventProducer<T>
        implements FrameworkLauncher {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractFrameworkLauncher.class);

    // Internal state
    /**
     * The OSGi Framework reference.
     */
    protected Framework framework;
    private final Object[] lock = new Object[0];
    private Map<String, BundleContextHolder> toRegister = new HashMap<String, BundleContextHolder>();
    private Map<String, BundleContextHolder> toDeregister = new HashMap<String, BundleContextHolder>();

    /**
     * Creates a new AbstractFrameworkLauncher with the provided IdGenerator.
     *
     * @param idGenerator The ID generator used to acquire a cluster-unique identifier for
     *                    this AbstractFrameworkLauncher instance.
     */
    protected AbstractFrameworkLauncher(final IdGenerator idGenerator, final Class<T> tClass) {
        super(idGenerator, tClass);
    }

    /**
     * Creates a new AbstractFrameworkLauncher and assigns the provided
     * cluster-unique ID to this instance.
     *
     * @param clusterUniqueID A cluster-unique Identifier.
     */
    protected AbstractFrameworkLauncher(final String clusterUniqueID, final Class<T> tClass) {
        this(new ConstantIdGenerator(clusterUniqueID), tClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Map<String, String> configuration) throws IllegalStateException {

        // Check sanity
        Validate.notNull(configuration, "Cannot handle null configuration Map.");

        // Create the Framework, and register ourselves as a FrameworkListener
        framework = createFramework(configuration);
    }

    /**
     * Creates the OSGi Framework instance from the data within the provided configuration.
     *
     * @param configuration The configuration properties given at initialization time to this ContainerEmbedder.
     * @return The initialized - but not started - OSGi Framework reference.
     * @throws IllegalArgumentException if the configuration was deemed inappropriate for
     *                                  initializing the underlying container.
     */
    protected abstract Framework createFramework(final Map<String, String> configuration)
            throws IllegalArgumentException;

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

        deregisterQueuedListeners();
        registerQueuedListeners();
    }

    /**
     * Internal factory method creating a BundleContextHolder instance from
     * the supplied validEventConsumer.
     *
     * @param validEventConsumer A valid EventConsumer instance.
     * @return A BundleContextHolder wrapping the provided validEventConsumer.
     */
    protected abstract BundleContextHolder makeBundleContextHolder(final T validEventConsumer);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean registerConsumerToEventProducers(final T validEventConsumer) {

        final BundleContextHolder bundleContextHolder = makeBundleContextHolder(validEventConsumer);

        if (getFramework() == null || getFramework().getBundleContext() == null) {

            // Defer registration until startup
            toRegister.put(bundleContextHolder.getId(), bundleContextHolder);

        } else {

            final BundleContext bundleContext = getFramework().getBundleContext();

            try {
                bundleContextHolder.register(bundleContext);
            } catch (Exception e) {

                log.error("Could not add [" + bundleContextHolder.getId() + "] as an EventConsumer", e);
                bundleContextHolder.unregister(bundleContext);
                return false;
            }
        }

        // All done.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean removeConsumerFromEventProducers(final T validEventConsumer) {

        final BundleContextHolder bundleContextHolder = makeBundleContextHolder(validEventConsumer);

        if (getFramework() == null || getFramework().getBundleContext() == null) {

            // Defer deregistration until shutdown
            toDeregister.put(bundleContextHolder.getId(), bundleContextHolder);

        } else {

            final BundleContext bundleContext = getFramework().getBundleContext();

            try {
                bundleContextHolder.unregister(bundleContext);

            } catch (Exception e) {

                log.error("Could not remove [" + bundleContextHolder.getId() + "] as an EventConsumer", e);
                return false;
            }

        }

        // All done.
        return true;
    }

    //
    // Private helpers
    //

    private void registerQueuedListeners() {

        synchronized (lock) {

            // Alter the registration list
            final Map<String, BundleContextHolder> containerEventListeners = toRegister;
            if (getFramework() == null) {

                log.warn("Abandoning [registerQueuedListeners]: OSGi Framework is null. (Check your lifecycle).");
                return;
            }

            final BundleContext ctx = getFramework().getBundleContext();
            if (ctx != null) {

                // All seems OK. Proceed with registering.
                this.toRegister = new HashMap<String, BundleContextHolder>();

                for (String current : containerEventListeners.keySet()) {

                    // Register the ContainerEventListener
                    BundleContextHolder bundleContextHolder = containerEventListeners.get(current);

                    try {
                        bundleContextHolder.register(ctx);
                    } catch (Exception e) {
                        log.error("Could not add [" + bundleContextHolder.getId() + "] as a ServiceListener. Abandoning.", e);

                        // Rollback this particular listener.
                        bundleContextHolder.unregister(ctx);
                    }
                }

                // Remove the old elements from the list, to invoke GC.
                containerEventListeners.clear();
            } else {
                throw new IllegalStateException(
                        "Could not acquire BundleContext from framework. This can happen if you forget to start "
                                + "the framework before using it. You might also need to check the installation.");
            }
        }
    }

    private void deregisterQueuedListeners() {

        synchronized (lock) {

            // Alter the registration list
            final Map<String, BundleContextHolder> containerEventListeners = toDeregister;
            if (getFramework() == null) {

                log.warn("Abandoning [deregisterQueuedListeners]: OSGi Framework is null. (Check your lifecycle).");
                return;
            }

            final BundleContext ctx = getFramework().getBundleContext();
            if (ctx != null) {

                // All seems OK. Proceed with deregistering.
                this.toDeregister = new HashMap<String, BundleContextHolder>();

                for (String current : containerEventListeners.keySet()) {

                    final BundleContextHolder wrapper = containerEventListeners.get(current);

                    try {
                        wrapper.unregister(ctx);
                    } catch (Exception e) {

                        log.error("Could not deregister [" + wrapper.getId() + "]. Listener state may be corrupt.", e);
                        this.toDeregister.put(wrapper.getId(), wrapper);
                    }
                }

                // Remove all listeners from the old queue list.
                containerEventListeners.clear();
            } else {
                throw new IllegalStateException(
                        "Could not acquire BundleContext from framework. This can happen if you forget to start "
                                + "the framework before using it. You might also need to check the installation.");
            }
        }
    }
}