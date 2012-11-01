/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.osgi;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import se.jguru.nazgul.core.algorithms.event.api.producer.EventConsumerCallback;
import se.jguru.nazgul.test.osgi.event.ServiceListenerAdapter;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Mock implementation of an OSGi ServiceRegistration, usable in unit tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockServiceRegistration<S> implements ServiceRegistration<S> {

    // Internal state
    private final MockServiceReference serviceReference;
    private final MockBundleContext bundleContext;
    private final Dictionary registrationProperties;

    /**
     * Creates a new MockServiceRegistration instance wrapping the provided
     * reference, context and registration properties.
     *
     * @param serviceReference       The ServiceReference to wrap.
     * @param bundleContext          The BundleContext to wrap.
     * @param registrationProperties The OSGi service registration properties.
     */
    public MockServiceRegistration(final MockServiceReference serviceReference,
                                   final MockBundleContext bundleContext,
                                   final Dictionary registrationProperties) {

        // Check sanity
        Validate.notNull(serviceReference, "Cannot handle null serviceReference argument.");
        Validate.notNull(bundleContext, "Cannot handle null bundleContext argument.");
        Validate.notNull(registrationProperties, "Cannot handle null registrationProperties argument.");

        // Assign internal state
        this.serviceReference = serviceReference;
        this.bundleContext = bundleContext;
        this.registrationProperties = registrationProperties;
    }

    /**
     * Creates a new MockServiceRegistration instance with an
     * empty registrationProperties dictionary.
     *
     * @param serviceReference The ServiceReference to wrap.
     * @param bundleContext    The BundleContext to wrap.
     */
    public MockServiceRegistration(final MockServiceReference serviceReference,
                                   final MockBundleContext bundleContext) {
        this(serviceReference, bundleContext, new Properties());
    }

    /**
     * Returns a {@code ServiceReference} object for a service being
     * registered.
     * <p/>
     * The {@code ServiceReference} object may be shared with other
     * bundles.
     *
     * @return {@code ServiceReference} object.
     * @throws IllegalStateException If this
     *                               {@code ServiceRegistration} object has already been
     *                               unregistered.
     */
    @Override
    public ServiceReference<S> getReference() {
        return serviceReference;
    }

    /**
     * Updates the properties associated with a service.
     * <p/>
     * <p/>
     * The {@link org.osgi.framework.Constants#OBJECTCLASS} and {@link org.osgi.framework.Constants#SERVICE_ID} keys
     * cannot be modified by this method. These values are set by the Framework
     * when the service is registered in the OSGi environment.
     * <p/>
     * <p/>
     * The following steps are required to modify service properties:
     * <ol>
     * <li>The service's properties are replaced with the provided properties.
     * <li>A service event of type {@link org.osgi.framework.ServiceEvent#MODIFIED} is fired.
     * </ol>
     *
     * @param properties The properties for this service. See {@link org.osgi.framework.Constants}
     *                   for a list of standard service property keys. Changes should not
     *                   be made to this object after calling this method. To update the
     *                   service's properties this method should be called again.
     * @throws IllegalStateException    If this {@code ServiceRegistration}
     *                                  object has already been unregistered.
     * @throws IllegalArgumentException If {@code properties} contains
     *                                  case variants of the same key name.
     */
    @Override
    public void setProperties(final Dictionary<String, ?> properties) {
        for (Enumeration en = properties.keys(); en.hasMoreElements(); ) {
            final Object currentKey = en.nextElement();
            registrationProperties.put(currentKey, properties.get(currentKey));
        }

        // Fire the modified event
        bundleContext.fireServiceEvent(new EventConsumerCallback<ServiceListenerAdapter>() {
            @Override
            public void onEvent(ServiceListenerAdapter eventConsumer) {
                eventConsumer.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED, serviceReference));
            }
        });
    }

    /**
     * Unregisters a service. Remove a {@code ServiceRegistration} object
     * from the Framework service registry. All {@code ServiceReference}
     * objects associated with this {@code ServiceRegistration} object
     * can no longer be used to interact with the service once unregistration is
     * complete.
     * <p/>
     * <p/>
     * The following steps are required to unregister a service:
     * <ol>
     * <li>The service is removed from the Framework service registry so that
     * it can no longer be obtained.
     * <li>A service event of type {@link org.osgi.framework.ServiceEvent#UNREGISTERING} is fired
     * so that bundles using this service can release their use of the service.
     * Once delivery of the service event is complete, the
     * {@code ServiceReference} objects for the service may no longer be
     * used to get a service object for the service.
     * <li>For each bundle whose use count for this service is greater than
     * zero: <br>
     * The bundle's use count for this service is set to zero. <br>
     * If the service was registered with a {@link org.osgi.framework.ServiceFactory} object, the
     * {@code ServiceFactory.ungetService} method is called to release
     * the service object for the bundle.
     * </ol>
     *
     * @throws IllegalStateException If this
     *                               {@code ServiceRegistration} object has already been
     *                               unregistered.
     * @see org.osgi.framework.BundleContext#ungetService
     * @see org.osgi.framework.ServiceFactory#ungetService
     */
    @Override
    public void unregister() {
        bundleContext.unregister(serviceReference);
    }
}
