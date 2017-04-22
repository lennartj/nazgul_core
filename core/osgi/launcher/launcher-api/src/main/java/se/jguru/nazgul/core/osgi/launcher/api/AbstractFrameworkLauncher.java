/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;
import se.jguru.nazgul.core.clustering.api.ConstantIdGenerator;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.OsgiFrameworkListener;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract implementation of the FrameworkLauncher interface, which caters
 * for registering and deregistering EventConsumers (in the form of
 * OsgiFrameworkListener instances) to OSGi ServiceEvents.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractFrameworkLauncher<T extends OsgiFrameworkListener>
        extends AbstractEventProducer<T> implements FrameworkLauncher {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractFrameworkLauncher.class);

    /**
     * The OSGi Framework reference.
     */
    protected Framework framework;

    // Internal state
    private final Object[] lock = new Object[0];
    private ConcurrentMap<String, BundleContextHolder> toRegister;
    private ConcurrentMap<String, BundleContextHolder> toDeregister;

    /**
     * Creates a new AbstractFrameworkLauncher with the provided IdGenerator.
     *
     * @param idGenerator        The ID generator used to acquire a cluster-unique identifier for
     *                           this AbstractFrameworkLauncher instance.
     * @param eventConsumerClass The type of eventConsumer which should be used by this AbstractFrameworkLauncher.
     * @see AbstractEventProducer#AbstractEventProducer(IdGenerator, Class)
     */
    protected AbstractFrameworkLauncher(final IdGenerator idGenerator, final Class<T> eventConsumerClass) {

        // Delegate
        super(idGenerator, eventConsumerClass);

        // Assign internal state
        this.toRegister = new ConcurrentHashMap<>();
        this.toDeregister = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new AbstractFrameworkLauncher and assigns the provided
     * cluster-unique ID to this instance.
     *
     * @param clusterUniqueID    A cluster-unique Identifier.
     * @param eventConsumerClass The type of eventConsumer which should be used by this AbstractFrameworkLauncher.
     */
    protected AbstractFrameworkLauncher(final String clusterUniqueID, final Class<T> eventConsumerClass) {

        // Delegate
        this(new ConstantIdGenerator(clusterUniqueID), eventConsumerClass);

        // Assign internal state
        this.toRegister = new ConcurrentHashMap<>();
        this.toDeregister = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(@NotNull final Map<String, String> configuration) throws IllegalStateException {

        // Check sanity
        Validate.notNull(configuration, "configuration");

        // Create the Framework, and register ourselves as a FrameworkListener
        framework = createFramework(configuration);

        try {

            // Initialize the created OSGi Framework.
            framework.init();

            // Perform any custom processing.
            onInitialize(configuration, framework);

        } catch (final BundleException e) {
            throw new IllegalStateException("Could not initialize the OSGi Framework", e);
        }
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
     * Custom handler for the init event - override this method
     * if you need to implement some custom logic to execute after
     * the Framework is initialized.
     *
     * @param framework     The just initialized osgiFramework instance.
     * @param configuration A Map holding configuration data for the underlying OSGi container.
     */
    protected void onInitialize(final Map<String, String> configuration, final Framework framework) {
        // Default implementation does nothing.
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
            toRegister.put(bundleContextHolder.getClusterId(), bundleContextHolder);

        } else {

            final BundleContext bundleContext = getFramework().getBundleContext();

            try {
                bundleContextHolder.register(bundleContext);
            } catch (Exception e) {

                log.error("Could not add [" + bundleContextHolder.getClusterId() + "] as an EventConsumer", e);
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
            toDeregister.put(bundleContextHolder.getClusterId(), bundleContextHolder);

        } else {

            final BundleContext bundleContext = getFramework().getBundleContext();

            try {
                bundleContextHolder.unregister(bundleContext);

            } catch (Exception e) {

                log.error("Could not remove [" + bundleContextHolder.getClusterId() + "] as an EventConsumer", e);
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
                this.toRegister = new ConcurrentHashMap<>();

                for (Map.Entry<String, BundleContextHolder> current : containerEventListeners.entrySet()) {

                    // Register the ContainerEventListener
                    BundleContextHolder bundleContextHolder = current.getValue();

                    try {
                        bundleContextHolder.register(ctx);
                    } catch (Exception e) {
                        log.error("Could not add [" + bundleContextHolder.getClusterId()
                                + "] as a ServiceListener. Abandoning.", e);

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

                // Proceed with deregistering.
                // First, create a new
                this.toDeregister = new ConcurrentHashMap<>();

                for (Map.Entry<String, BundleContextHolder> current : containerEventListeners.entrySet()) {

                    final BundleContextHolder wrapper = current.getValue();

                    try {
                        wrapper.unregister(ctx);
                    } catch (Exception e) {

                        log.error("Could not deregister [" + wrapper.getClusterId() + "]. Listener state may be corrupt.", e);
                        this.toDeregister.put(wrapper.getClusterId(), wrapper);
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
