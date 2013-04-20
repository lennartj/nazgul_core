/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.test.osgi;

import org.apache.commons.lang3.Validate;
import org.apache.felix.framework.FilterImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;
import se.jguru.nazgul.core.algorithms.event.api.producer.EventConsumerCallback;
import se.jguru.nazgul.core.algorithms.event.api.producer.EventProducer;
import se.jguru.nazgul.test.osgi.event.BundleListenerAdapter;
import se.jguru.nazgul.test.osgi.event.ServiceListenerAdapter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A mock implementation of an OSGi BundleContext, to be used in unit tests only.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBundleContext implements BundleContext {

    // Internal state
    private Map<String, ServiceReference> serviceReferences = new HashMap<String, ServiceReference>();
    private Map<ServiceReference, Object> services = new HashMap<ServiceReference, Object>();
    private Map<Long, Bundle> bundles = new HashMap<Long, Bundle>();

    private EventProducer<BundleListenerAdapter> bundleListeners =
            new AbstractEventProducer<BundleListenerAdapter>("testBundleListeners", BundleListenerAdapter.class) {
            };

    private EventProducer<ServiceListenerAdapter> serviceListeners =
            new AbstractEventProducer<ServiceListenerAdapter>("testServiceListeners", ServiceListenerAdapter.class) {
            };

    private final Bundle bundle;
    private Properties properties;

    public MockBundleContext(final Bundle bundle) {

        // Assign internal state
        this.bundle = bundle;
        this.bundles.put(bundle.getBundleId(), bundle);
        this.properties = new Properties();
        properties.put(Constants.FRAMEWORK_VERSION, bundle.getVersion().toString());
    }

    /**
     * Fires a BundleEvent to all registered bundleListeners, with the given
     * [Event]ConsumerEventCallback implementation to define what happens
     * within the event.
     *
     * @param consumerCallback The callback definition for each registered BundleListenerAdapter.
     */
    public void fireBundleEvent(final EventConsumerCallback<BundleListenerAdapter> consumerCallback) {
        bundleListeners.notifyConsumers(consumerCallback);
    }

    /**
     * Fires an OSGi ServiceEvent to all registered bundleListeners, with the
     * given visitor pattern ConsumerEventCallback implementatino to define what
     * happens within the event.
     *
     * @param consumerCallback The callback definition for each registered ServiceListenerAdapter.
     */
    public void fireServiceEvent(final EventConsumerCallback<ServiceListenerAdapter> consumerCallback) {
        serviceListeners.notifyConsumers(consumerCallback);
    }

    /**
     * Searches only properties defined within the MockBundleContext.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public String getProperty(final String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves the internal Bundle.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Installs a bundle from the specified {@code InputStream} object.
     * <p/>
     * <p/>
     * If the specified {@code InputStream} is {@code null}, the Framework must
     * create the {@code InputStream} from which to read the bundle by
     * interpreting, in an implementation dependent manner, the specified
     * {@code location}.
     * <p/>
     * <p/>
     * The specified {@code location} identifier will be used as the identity of
     * the bundle. Every installed bundle is uniquely identified by its location
     * identifier which is typically in the form of a URL.
     * <p/>
     * <p/>
     * The following steps are required to install a bundle:
     * <ol>
     * <li>If a bundle containing the same location identifier is already
     * installed, the {@code Bundle} object for that bundle is returned.
     * <p/>
     * <li>The bundle's content is read from the input stream. If this fails, a
     * {@link org.osgi.framework.BundleException} is thrown.
     * <p/>
     * <li>The bundle's associated resources are allocated. The associated
     * resources minimally consist of a unique identifier and a persistent
     * storage area if the platform has file system support. If this step fails,
     * a {@code BundleException} is thrown.
     * <p/>
     * <li>The bundle's state is set to {@code INSTALLED}.
     * <p/>
     * <li>A bundle event of type {@link org.osgi.framework.BundleEvent#INSTALLED} is fired.
     * <p/>
     * <li>The {@code Bundle} object for the newly or previously installed
     * bundle is returned.
     * </ol>
     * <p/>
     * <b>Postconditions, no exceptions thrown </b>
     * <ul>
     * <li>{@code getState()} in &#x007B; {@code INSTALLED}, {@code RESOLVED}
     * &#x007D;.
     * <li>Bundle has a unique ID.
     * </ul>
     * <b>Postconditions, when an exception is thrown </b>
     * <ul>
     * <li>Bundle is not installed. If there was an existing bundle for the
     * specified location, then that bundle must still be in the state it was
     * prior to calling this method.</li>
     * </ul>
     *
     * @param location The location identifier of the bundle to install.
     * @param input    The {@code InputStream} object from which this bundle will
     *                 be read or {@code null} to indicate the Framework must create the
     *                 input stream from the specified location identifier. The input
     *                 stream must always be closed when this method completes, even if
     *                 an exception is thrown.
     * @return The {@code Bundle} object of the installed bundle.
     * @throws org.osgi.framework.BundleException
     *                               If the installation failed. BundleException types
     *                               thrown by this method include: {@link org.osgi.framework.BundleException#READ_ERROR}
     *                               , {@link org.osgi.framework.BundleException#DUPLICATE_BUNDLE_ERROR},
     *                               {@link org.osgi.framework.BundleException#MANIFEST_ERROR}, and
     *                               {@link org.osgi.framework.BundleException#REJECTED_BY_HOOK}.
     * @throws SecurityException     If the caller does not have the appropriate
     *                               {@code AdminPermission[installed bundle,LIFECYCLE]}, and the Java
     *                               Runtime Environment supports permissions.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     */
    @Override
    public Bundle installBundle(final String location, final InputStream input) throws BundleException {
        throw new UnsupportedOperationException("Use installBundle(Bundle) instead. This is a MockBundleContext.");
    }

    /**
     * Installs a bundle from the specified {@code location} identifier.
     * <p/>
     * <p/>
     * This method performs the same function as calling
     * {@link #installBundle(String, java.io.InputStream)} with the specified
     * {@code location} identifier and a {@code null} InputStream.
     *
     * @param location The location identifier of the bundle to install.
     * @return The {@code Bundle} object of the installed bundle.
     * @throws org.osgi.framework.BundleException
     *                               If the installation failed. BundleException types
     *                               thrown by this method include: {@link org.osgi.framework.BundleException#READ_ERROR}
     *                               , {@link org.osgi.framework.BundleException#DUPLICATE_BUNDLE_ERROR},
     *                               {@link org.osgi.framework.BundleException#MANIFEST_ERROR}, and
     *                               {@link org.osgi.framework.BundleException#REJECTED_BY_HOOK}.
     * @throws SecurityException     If the caller does not have the appropriate
     *                               {@code AdminPermission[installed bundle,LIFECYCLE]}, and the Java
     *                               Runtime Environment supports permissions.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #installBundle(String, java.io.InputStream)
     */
    @Override
    public Bundle installBundle(final String location) throws BundleException {
        throw new UnsupportedOperationException("Use installBundle(Bundle) instead. This is a MockBundleContext.");
    }

    /**
     * Mock implementation of installing a provided bundle
     * <p/>
     * A bundle event of type {@link org.osgi.framework.BundleEvent#INSTALLED} is fired.
     *
     * @param bundle the bundle to install
     * @return the given bundle
     */
    public Bundle installBundle(final Bundle bundle) {

        // Register the bundle
        bundles.put(bundle.getBundleId(), bundle);

        // Notify all listeners
        final EventConsumerCallback<BundleListenerAdapter> consumerCallback
                = new EventConsumerCallback<BundleListenerAdapter>() {
            @Override
            public void onEvent(final BundleListenerAdapter eventConsumer) {
                eventConsumer.bundleChanged(new BundleEvent(BundleEvent.INSTALLED, bundle));
            }
        };
        fireBundleEvent(consumerCallback);

        // All done.
        return bundle;
    }

    /**
     * Returns the bundle with the specified identifier.
     *
     * @param id The identifier of the bundle to retrieve.
     * @return A {@code Bundle} object or {@code null} if the identifier does
     *         not match any installed bundle.
     */
    @Override
    public Bundle getBundle(final long id) {
        return bundles.get(id);
    }

    /**
     * Returns a list of all installed bundles.
     * <p/>
     * This method returns a list of all bundles installed in the OSGi
     * environment at the time of the call to this method. However, since the
     * Framework is a very dynamic environment, bundles can be installed or
     * uninstalled at anytime.
     *
     * @return An array of {@code Bundle} objects, one object per installed
     *         bundle.
     */
    @Override
    public Bundle[] getBundles() {
        return bundles.values().toArray(new Bundle[bundles.size()]);
    }

    /**
     * Adds the specified {@code ServiceListener} object with the specified
     * {@code filter} to the context bundle's list of listeners. See
     * {@link org.osgi.framework.Filter} for a description of the filter syntax.
     * {@code ServiceListener} objects are notified when a service has a
     * lifecycle state change.
     * <p/>
     * <p/>
     * If the context bundle's list of listeners already contains a listener
     * {@code l} such that {@code (l==listener)}, then this method replaces that
     * listener's filter (which may be {@code null}) with the specified one
     * (which may be {@code null}).
     * <p/>
     * <p/>
     * The listener is called if the filter criteria is met. To filter based
     * upon the class of the service, the filter should reference the
     * {@link org.osgi.framework.Constants#OBJECTCLASS} property. If {@code filter} is {@code null}
     * , all services are considered to match the filter.
     * <p/>
     * <p/>
     * When using a {@code filter}, it is possible that the {@code ServiceEvent}
     * s for the complete lifecycle of a service will not be delivered to the
     * listener. For example, if the {@code filter} only matches when the
     * property {@code x} has the value {@code 1}, the listener will not be
     * called if the service is registered with the property {@code x} not set
     * to the value {@code 1}. Subsequently, when the service is modified
     * setting property {@code x} to the value {@code 1}, the filter will match
     * and the listener will be called with a {@code ServiceEvent} of type
     * {@code MODIFIED}. Thus, the listener will not be called with a
     * {@code ServiceEvent} of type {@code REGISTERED}.
     * <p/>
     * <p/>
     * If the Java Runtime Environment supports permissions, the
     * {@code ServiceListener} object will be notified of a service event only
     * if the bundle that is registering it has the {@code ServicePermission} to
     * get the service using at least one of the named classes the service was
     * registered under.
     *
     * @param listener The {@code ServiceListener} object to be added.
     * @param filter   The filter criteria.
     * @throws org.osgi.framework.InvalidSyntaxException
     *                               If {@code filter} contains an invalid
     *                               filter string that cannot be parsed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see org.osgi.framework.ServiceEvent
     * @see org.osgi.framework.ServiceListener
     * @see org.osgi.framework.ServicePermission
     */
    @Override
    public void addServiceListener(final ServiceListener listener, final String filter) throws InvalidSyntaxException {
        addServiceListener(listener);
    }

    /**
     * Adds the specified {@code ServiceListener} object to the context bundle's
     * list of listeners.
     * <p/>
     * <p/>
     * This method is the same as calling
     * {@code BundleContext.addServiceListener(ServiceListener listener,
     *String filter)} with {@code filter} set to {@code null}.
     *
     * @param listener The {@code ServiceListener} object to be added.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #addServiceListener(org.osgi.framework.ServiceListener, String)
     */
    @Override
    public void addServiceListener(final ServiceListener listener) {

        // Create a cluster-unique id
        final ServiceListenerAdapter adapter = new ServiceListenerAdapter(
                "ServiceListener_" + listener.hashCode(),
                ServiceListenerAdapter.class,
                listener);

        // Add the ServiceListener to the known collection of consumers.
        serviceListeners.addConsumer(adapter);
    }

    /**
     * Removes the specified {@code ServiceListener} object from the context
     * bundle's list of listeners.
     * <p/>
     * <p/>
     * If {@code listener} is not contained in this context bundle's list of
     * listeners, this method does nothing.
     *
     * @param listener The {@code ServiceListener} to be removed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     */
    @Override
    public void removeServiceListener(final ServiceListener listener) {

        // Re-create the ServiceListenerAdapter instance.
        final ServiceListenerAdapter adapter = new ServiceListenerAdapter(
                "ServiceListener_" + listener.hashCode(),
                ServiceListenerAdapter.class,
                listener);

        // Remove the ServiceListener from the known collection of consumers.
        serviceListeners.removeConsumer(adapter.getClusterId());
    }

    /**
     * Adds the specified {@code BundleListener} object to the context bundle's
     * list of listeners if not already present. BundleListener objects are
     * notified when a bundle has a lifecycle state change.
     * <p/>
     * <p/>
     * If the context bundle's list of listeners already contains a listener
     * {@code l} such that {@code (l==listener)}, this method does nothing.
     *
     * @param listener The {@code BundleListener} to be added.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @throws SecurityException     If listener is a
     *                               {@code SynchronousBundleListener} and the caller does not have
     *                               the appropriate {@code AdminPermission[context bundle,LISTENER]},
     *                               and the Java Runtime Environment supports permissions.
     * @see org.osgi.framework.BundleEvent
     * @see org.osgi.framework.BundleListener
     */
    @Override
    public void addBundleListener(final BundleListener listener) {

        final BundleListenerAdapter toAdd = new BundleListenerAdapter(
                "BundleListener_" + listener.hashCode(),
                BundleListenerAdapter.class,
                listener);

        bundleListeners.addConsumer(toAdd);
    }

    /**
     * Removes the specified {@code BundleListener} object from the context
     * bundle's list of listeners.
     * <p/>
     * <p/>
     * If {@code listener} is not contained in the context bundle's list of
     * listeners, this method does nothing.
     *
     * @param listener The {@code BundleListener} object to be removed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @throws SecurityException     If listener is a
     *                               {@code SynchronousBundleListener} and the caller does not have
     *                               the appropriate {@code AdminPermission[context bundle,LISTENER]},
     *                               and the Java Runtime Environment supports permissions.
     */
    @Override
    public void removeBundleListener(final BundleListener listener) {

        final BundleListenerAdapter toRemove = new BundleListenerAdapter(
                "BundleListener_" + listener.hashCode(),
                BundleListenerAdapter.class,
                listener);

        bundleListeners.removeConsumer(toRemove.getClusterId());
    }

    /**
     * Adds the specified {@code FrameworkListener} object to the context
     * bundle's list of listeners if not already present. FrameworkListeners are
     * notified of general Framework events.
     * <p/>
     * <p/>
     * If the context bundle's list of listeners already contains a listener
     * {@code l} such that {@code (l==listener)}, this method does nothing.
     *
     * @param listener The {@code FrameworkListener} object to be added.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see org.osgi.framework.FrameworkEvent
     * @see org.osgi.framework.FrameworkListener
     */
    @Override
    public void addFrameworkListener(final FrameworkListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Removes the specified {@code FrameworkListener} object from the context
     * bundle's list of listeners.
     * <p/>
     * <p/>
     * If {@code listener} is not contained in the context bundle's list of
     * listeners, this method does nothing.
     *
     * @param listener The {@code FrameworkListener} object to be removed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     */
    @Override
    public void removeFrameworkListener(final FrameworkListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Registers the specified service object with the specified properties
     * under the specified class names into the Framework. A
     * {@code ServiceRegistration} object is returned. The
     * {@code ServiceRegistration} object is for the private use of the bundle
     * registering the service and should not be shared with other bundles. The
     * registering bundle is defined to be the context bundle. Other bundles can
     * locate the service by using either the {@link #getServiceReferences} or
     * {@link #getServiceReference} method.
     * <p/>
     * <p/>
     * A bundle can register a service object that implements the
     * {@link org.osgi.framework.ServiceFactory} interface to have more flexibility in providing
     * service objects to other bundles.
     * <p/>
     * <p/>
     * The following steps are required to register a service:
     * <ol>
     * <li>If {@code service} is not a {@code ServiceFactory}, an
     * {@code IllegalArgumentException} is thrown if {@code service} is not an
     * {@code instanceof} all the specified class names.
     * <li>The Framework adds the following service properties to the service
     * properties from the specified {@code Dictionary} (which may be
     * {@code null}): <br/>
     * A property named {@link org.osgi.framework.Constants#SERVICE_ID} identifying the
     * registration number of the service <br/>
     * A property named {@link org.osgi.framework.Constants#OBJECTCLASS} containing all the
     * specified classes. <br/>
     * Properties with these names in the specified {@code Dictionary} will be
     * ignored.
     * <li>The service is added to the Framework service registry and may now be
     * used by other bundles.
     * <li>A service event of type {@link org.osgi.framework.ServiceEvent#REGISTERED} is fired.
     * <li>A {@code ServiceRegistration} object for this registration is
     * returned.
     * </ol>
     *
     * @param classes    The class names under which the service can be located.
     *                   The class names in this array will be stored in the service's
     *                   properties under the key {@link org.osgi.framework.Constants#OBJECTCLASS}.
     * @param service    The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service. The keys in the
     *                   properties object must all be {@code String} objects. See
     *                   {@link org.osgi.framework.Constants} for a list of standard service property keys.
     *                   Changes should not be made to this object after calling this
     *                   method. To update the service's properties the
     *                   {@link org.osgi.framework.ServiceRegistration#setProperties} method must be called.
     *                   The set of properties may be {@code null} if the service has no
     *                   properties.
     * @return A {@code ServiceRegistration} object for use by the bundle
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalArgumentException If one of the following is true:
     *                                  <ul>
     *                                  <li>{@code service} is {@code null}. <li>{@code service} is not a
     *                                  {@code ServiceFactory} object and is not an instance of all the
     *                                  named classes in {@code clazzes}. <li> {@code properties}
     *                                  contains case variants of the same key name.
     *                                  </ul>
     * @throws SecurityException        If the caller does not have the
     *                                  {@code ServicePermission} to register the service for all the
     *                                  named classes and the Java Runtime Environment supports
     *                                  permissions.
     * @throws IllegalStateException    If this BundleContext is no longer valid.
     * @see org.osgi.framework.ServiceRegistration
     * @see org.osgi.framework.ServiceFactory
     */
    @Override
    public ServiceRegistration registerService(final String[] classes,
                                               final Object service,
                                               final Dictionary properties) {

        // Check sanity
        Validate.notEmpty(classes, "Can not handle null or empty classes argument.");
        Validate.notNull(service, "Can not handle null service argument.");
        Validate.notNull(properties, "Can not handle null properties argument.");

        properties.put(Constants.OBJECTCLASS, classes);

        String serviceID = (String) properties.get(Constants.SERVICE_ID);
        if (serviceID == null) {
            serviceID = "Service_" + classes[0];
        }

        Integer serviceRanking = (Integer) properties.get(Constants.SERVICE_RANKING);
        if (serviceRanking == null) {
            serviceRanking = 0;
        }

        final MockServiceReference reference = new MockServiceReference(bundle,
                Arrays.asList(classes),
                properties,
                serviceID,
                serviceRanking);

        services.put(reference, service);

        for (final String clazz : classes) {
            serviceReferences.put(clazz, reference);
        }

        fireServiceEvent(new EventConsumerCallback<ServiceListenerAdapter>() {
            @Override
            public void onEvent(ServiceListenerAdapter eventConsumer) {
                eventConsumer.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, reference));
            }
        });

        // All done.
        return new MockServiceRegistration(reference, this, properties);
    }

    /**
     * Unregisters the supplied ServiceReference instance from this MockBundleContext.
     *
     * @param reference The ServiceReference to unregister.
     */
    public void unregister(final ServiceReference reference) {

        final String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        for (final String clazz : classes) {
            serviceReferences.remove(clazz);
        }

        services.remove(reference);

        fireServiceEvent(new EventConsumerCallback<ServiceListenerAdapter>() {
            @Override
            public void onEvent(ServiceListenerAdapter eventConsumer) {
                eventConsumer.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, reference));
            }
        });
    }

    /**
     * Registers the specified service object with the specified properties
     * under the specified class name with the Framework.
     * <p/>
     * <p/>
     * This method is otherwise identical to
     * {@link #registerService(String[], Object, java.util.Dictionary)} and is provided as
     * a convenience when {@code service} will only be registered under a single
     * class name. Note that even in this case the value of the service's
     * {@link org.osgi.framework.Constants#OBJECTCLASS} property will be an array of string, rather
     * than just a single string.
     *
     * @param clazz      The class name under which the service can be located.
     * @param service    The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service.
     * @return A {@code ServiceRegistration} object for use by the bundle
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #registerService(String[], Object, java.util.Dictionary)
     */
    @Override
    public ServiceRegistration<?> registerService(final String clazz,
                                                  final Object service,
                                                  final Dictionary<String, ?> properties) {
        return registerService(new String[]{clazz}, service, properties);
    }

    /**
     * Registers the specified service object with the specified properties
     * under the specified class name with the Framework.
     * <p/>
     * <p/>
     * This method is otherwise identical to
     * {@link #registerService(String[], Object, java.util.Dictionary)} and is provided as
     * a convenience when {@code service} will only be registered under a single
     * class name. Note that even in this case the value of the service's
     * {@link org.osgi.framework.Constants#OBJECTCLASS} property will be an array of string, rather
     * than just a single string.
     *
     * @param <S>        Type of Service.
     * @param clazz      The class name under which the service can be located.
     * @param service    The service object or a {@code ServiceFactory} object.
     * @param properties The properties for this service.
     * @return A {@code ServiceRegistration} object for use by the bundle
     *         registering the service to update the service's properties or to
     *         unregister the service.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #registerService(String[], Object, java.util.Dictionary)
     * @since 1.6
     */
    @Override
    public <S> ServiceRegistration<S> registerService(final Class<S> clazz,
                                                      final S service,
                                                      final Dictionary<String, ?> properties) {
        return registerService(new String[]{clazz.getName()}, service, properties);
    }

    /**
     * Returns an array of {@code ServiceReference} objects. The returned array
     * of {@code ServiceReference} objects contains services that were
     * registered under the specified class, match the specified filter
     * expression, and the packages for the class names under which the services
     * were registered match the context bundle's packages as defined in
     * {@link org.osgi.framework.ServiceReference#isAssignableTo(org.osgi.framework.Bundle, String)}.
     * <p/>
     * <p/>
     * The list is valid at the time of the call to this method. However since
     * the Framework is a very dynamic environment, services can be modified or
     * unregistered at any time.
     * <p/>
     * <p/>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link org.osgi.framework.Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link org.osgi.framework.InvalidSyntaxException} will be thrown with a human readable
     * message where the filter became unparsable.
     * <p/>
     * <p/>
     * The result is an array of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>If the specified class name, {@code clazz}, is not {@code null}, the
     * service must have been registered with the specified class name. The
     * complete list of class names with which a service was registered is
     * available from the service's {@link org.osgi.framework.Constants#OBJECTCLASS objectClass}
     * property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * <li>If the Java Runtime Environment supports permissions, the caller must
     * have {@code ServicePermission} with the {@code GET} action for at least
     * one of the class names under which the service was registered.
     * <li>For each class name with which the service was registered, calling
     * {@link org.osgi.framework.ServiceReference#isAssignableTo(org.osgi.framework.Bundle, String)} with the context
     * bundle and the class name on the service's {@code ServiceReference}
     * object must return {@code true}
     * </ul>
     *
     * @param clazz  The class name with which the service was registered or
     *               {@code null} for all services.
     * @param filter The filter expression or {@code null} for all services.
     * @return An array of {@code ServiceReference} objects or {@code null} if
     *         no services are registered which satisfy the search.
     * @throws org.osgi.framework.InvalidSyntaxException
     *                               If the specified {@code filter} contains
     *                               an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     */
    @Override
    public ServiceReference<?>[] getServiceReferences(final String clazz,
                                                      final String filter)
            throws InvalidSyntaxException {

        Validate.isTrue(filter == null, "Filtering not implemented in MockBundleContext::getServiceReferences.");


        final List<ServiceReference<?>> tmp = new ArrayList<ServiceReference<?>>();

        if (clazz == null) {
            for (ServiceReference<?> current : serviceReferences.values()) {
                tmp.add(current);
            }
        } else {
            final ServiceReference serviceReference = serviceReferences.get(clazz);
            if (serviceReference != null) {
                tmp.add(serviceReference);
            }
        }

        ServiceReference<?>[] toReturn = new ServiceReference<?>[tmp.size()];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = tmp.get(i);
        }

        return toReturn.length == 0 ? null : toReturn;
    }

    /**
     * Returns an array of {@code ServiceReference} objects. The returned array
     * of {@code ServiceReference} objects contains services that were
     * registered under the specified class and match the specified filter
     * expression.
     * <p/>
     * <p/>
     * The list is valid at the time of the call to this method. However since
     * the Framework is a very dynamic environment, services can be modified or
     * unregistered at any time.
     * <p/>
     * <p/>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link org.osgi.framework.Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link org.osgi.framework.InvalidSyntaxException} will be thrown with a human readable
     * message where the filter became unparsable.
     * <p/>
     * <p/>
     * The result is an array of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>If the specified class name, {@code clazz}, is not {@code null}, the
     * service must have been registered with the specified class name. The
     * complete list of class names with which a service was registered is
     * available from the service's {@link org.osgi.framework.Constants#OBJECTCLASS objectClass}
     * property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * <li>If the Java Runtime Environment supports permissions, the caller must
     * have {@code ServicePermission} with the {@code GET} action for at least
     * one of the class names under which the service was registered.
     * </ul>
     *
     * @param clazz  The class name with which the service was registered or
     *               {@code null} for all services.
     * @param filter The filter expression or {@code null} for all services.
     * @return An array of {@code ServiceReference} objects or {@code null} if
     *         no services are registered which satisfy the search.
     * @throws org.osgi.framework.InvalidSyntaxException
     *                               If the specified {@code filter} contains
     *                               an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @since 1.3
     */
    @Override
    public ServiceReference<?>[] getAllServiceReferences(final String clazz,
                                                         final String filter)
            throws InvalidSyntaxException {
        return getServiceReferences(clazz, filter);
    }

    /**
     * Returns a {@code ServiceReference} object for a service that implements
     * and was registered under the specified class.
     * <p/>
     * <p/>
     * The returned {@code ServiceReference} object is valid at the time of the
     * call to this method. However as the Framework is a very dynamic
     * environment, services can be modified or unregistered at any time.
     * <p/>
     * <p/>
     * This method is the same as calling
     * {@link #getServiceReferences(String, String)} with a {@code null} filter
     * expression and then finding the reference with the highest priority. It
     * is provided as a convenience for when the caller is interested in any
     * service that implements the specified class.
     * <p/>
     * If multiple such services exist, the service with the highest priority is
     * selected. This priority is defined as the service reference with the
     * highest ranking (as specified in its {@link org.osgi.framework.Constants#SERVICE_RANKING}
     * property) is returned.
     * <p/>
     * If there is a tie in ranking, the service with the lowest service ID (as
     * specified in its {@link org.osgi.framework.Constants#SERVICE_ID} property); that is, the
     * service that was registered first is returned.
     *
     * @param clazz The class name with which the service was registered.
     * @return A {@code ServiceReference} object, or {@code null} if no services
     *         are registered which implement the named class.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #getServiceReferences(String, String)
     */
    @Override
    public ServiceReference<?> getServiceReference(final String clazz) {
        return serviceReferences.get(clazz);
    }

    /**
     * Returns a {@code ServiceReference} object for a service that implements
     * and was registered under the specified class.
     * <p/>
     * <p/>
     * The returned {@code ServiceReference} object is valid at the time of the
     * call to this method. However as the Framework is a very dynamic
     * environment, services can be modified or unregistered at any time.
     * <p/>
     * <p/>
     * This method is the same as calling
     * {@link #getServiceReferences(Class, String)} with a {@code null} filter
     * expression. It is provided as a convenience for when the caller is
     * interested in any service that implements the specified class.
     * <p/>
     * If multiple such services exist, the service with the highest ranking (as
     * specified in its {@link org.osgi.framework.Constants#SERVICE_RANKING} property) is returned.
     * <p/>
     * If there is a tie in ranking, the service with the lowest service ID (as
     * specified in its {@link org.osgi.framework.Constants#SERVICE_ID} property); that is, the
     * service that was registered first is returned.
     *
     * @param <S>   Type of Service.
     * @param clazz The class name with which the service was registered.
     * @return A {@code ServiceReference} object, or {@code null} if no services
     *         are registered which implement the named class.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see #getServiceReferences(Class, String)
     * @since 1.6
     */
    @Override
    public <S> ServiceReference<S> getServiceReference(final Class<S> clazz) {
        return (ServiceReference<S>) getServiceReference(clazz.getName());
    }

    /**
     * Returns a collection of {@code ServiceReference} objects. The returned
     * collection of {@code ServiceReference} objects contains services that
     * were registered under the specified class, match the specified filter
     * expression, and the packages for the class names under which the services
     * were registered match the context bundle's packages as defined in
     * {@link org.osgi.framework.ServiceReference#isAssignableTo(org.osgi.framework.Bundle, String)}.
     * <p/>
     * <p/>
     * The collection is valid at the time of the call to this method. However
     * since the Framework is a very dynamic environment, services can be
     * modified or unregistered at any time.
     * <p/>
     * <p/>
     * The specified {@code filter} expression is used to select the registered
     * services whose service properties contain keys and values which satisfy
     * the filter expression. See {@link org.osgi.framework.Filter} for a description of the filter
     * syntax. If the specified {@code filter} is {@code null}, all registered
     * services are considered to match the filter. If the specified
     * {@code filter} expression cannot be parsed, an
     * {@link org.osgi.framework.InvalidSyntaxException} will be thrown with a human readable
     * message where the filter became unparsable.
     * <p/>
     * <p/>
     * The result is a collection of {@code ServiceReference} objects for all
     * services that meet all of the following conditions:
     * <ul>
     * <li>If the specified class name, {@code clazz}, is not {@code null}, the
     * service must have been registered with the specified class name. The
     * complete list of class names with which a service was registered is
     * available from the service's {@link org.osgi.framework.Constants#OBJECTCLASS objectClass}
     * property.
     * <li>If the specified {@code filter} is not {@code null}, the filter
     * expression must match the service.
     * <li>If the Java Runtime Environment supports permissions, the caller must
     * have {@code ServicePermission} with the {@code GET} action for at least
     * one of the class names under which the service was registered.
     * <li>For each class name with which the service was registered, calling
     * {@link org.osgi.framework.ServiceReference#isAssignableTo(org.osgi.framework.Bundle, String)} with the context
     * bundle and the class name on the service's {@code ServiceReference}
     * object must return {@code true}
     * </ul>
     *
     * @param <S>    Type of Service
     * @param clazz  The class name with which the service was registered. Must
     *               not be {@code null}.
     * @param filter The filter expression or {@code null} for all services.
     * @return A collection of {@code ServiceReference} objects. May be empty if
     *         no services are registered which satisfy the search.
     * @throws org.osgi.framework.InvalidSyntaxException
     *                               If the specified {@code filter} contains
     *                               an invalid filter expression that cannot be parsed.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @since 1.6
     */
    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(final Class<S> clazz, final String filter)
            throws InvalidSyntaxException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Returns the service object referenced by the specified
     * {@code ServiceReference} object.
     * <p/>
     * A bundle's use of a service is tracked by the bundle's use count of that
     * service. Each time a service's service object is returned by
     * {@link #getService(org.osgi.framework.ServiceReference)} the context bundle's use count for
     * that service is incremented by one. Each time the service is released by
     * {@link #ungetService(org.osgi.framework.ServiceReference)} the context bundle's use count
     * for that service is decremented by one.
     * <p/>
     * When a bundle's use count for a service drops to zero, the bundle should
     * no longer use that service.
     * <p/>
     * <p/>
     * This method will always return {@code null} when the service associated
     * with this {@code reference} has been unregistered.
     * <p/>
     * <p/>
     * The following steps are required to get the service object:
     * <ol>
     * <li>If the service has been unregistered, {@code null} is returned.
     * <li>If the context bundle's use count for the service is currently zero
     * and the service was registered with an object implementing the
     * {@code ServiceFactory} interface, the
     * {@link org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration)} method is
     * called to create a service object for the context bundle. If the service
     * object returned by the {@code ServiceFactory} object is {@code null}, not
     * an {@code instanceof} all the classes named when the service was
     * registered or the {@code ServiceFactory} object throws an exception or
     * will be recursively called for the context bundle, {@code null} is
     * returned and a Framework event of type {@link org.osgi.framework.FrameworkEvent#ERROR}
     * containing a {@link org.osgi.framework.ServiceException} describing the error is fired. <br>
     * This service object is cached by the Framework. While the context
     * bundle's use count for the service is greater than zero, subsequent calls
     * to get the services's service object for the context bundle will return
     * the cached service object.
     * <li>The context bundle's use count for this service is incremented by
     * one.
     * <li>The service object for the service is returned.
     * </ol>
     *
     * @param <S>       Type of Service.
     * @param reference A reference to the service.
     * @return A service object for the service associated with
     *         {@code reference} or {@code null} if the service is not
     *         registered, the service object returned by a
     *         {@code ServiceFactory} does not implement the classes under which
     *         it was registered or the {@code ServiceFactory} threw an
     *         exception.
     * @throws SecurityException        If the caller does not have the
     *                                  {@code ServicePermission} to get the service using at least one
     *                                  of the named classes the service was registered under and the
     *                                  Java Runtime Environment supports permissions.
     * @throws IllegalStateException    If this BundleContext is no longer valid.
     * @throws IllegalArgumentException If the specified
     *                                  {@code ServiceReference} was not created by the same framework
     *                                  instance as this {@code BundleContext}.
     * @see #ungetService(org.osgi.framework.ServiceReference)
     * @see org.osgi.framework.ServiceFactory
     */
    @Override
    public <S> S getService(final ServiceReference<S> reference) {
        return (S) services.get(reference);
    }

    /**
     * Releases the service object referenced by the specified
     * {@code ServiceReference} object. If the context bundle's use count for
     * the service is zero, this method returns {@code false}. Otherwise, the
     * context bundle's use count for the service is decremented by one.
     * <p/>
     * <p/>
     * The service's service object should no longer be used and all references
     * to it should be destroyed when a bundle's use count for the service drops
     * to zero.
     * <p/>
     * <p/>
     * The following steps are required to unget the service object:
     * <ol>
     * <li>If the context bundle's use count for the service is zero or the
     * service has been unregistered, {@code false} is returned.
     * <li>The context bundle's use count for this service is decremented by
     * one.
     * <li>If the context bundle's use count for the service is currently zero
     * and the service was registered with a {@code ServiceFactory} object, the
     * {@link org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle, org.osgi.framework.ServiceRegistration, Object)}
     * method is called to release the service object for the context bundle.
     * <li>{@code true} is returned.
     * </ol>
     *
     * @param reference A reference to the service to be released.
     * @return {@code false} if the context bundle's use count for the service
     *         is zero or if the service has been unregistered; {@code true}
     *         otherwise.
     * @throws IllegalStateException    If this BundleContext is no longer valid.
     * @throws IllegalArgumentException If the specified
     *                                  {@code ServiceReference} was not created by the same framework
     *                                  instance as this {@code BundleContext}.
     * @see #getService
     * @see org.osgi.framework.ServiceFactory
     */
    @Override
    public boolean ungetService(final ServiceReference<?> reference) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Creates a {@code File} object for a file in the persistent storage area
     * provided for the bundle by the Framework. This method will return
     * {@code null} if the platform does not have file system support.
     * <p/>
     * <p/>
     * A {@code File} object for the base directory of the persistent storage
     * area provided for the context bundle by the Framework can be obtained by
     * calling this method with an empty string as {@code filename}.
     * <p/>
     * <p/>
     * If the Java Runtime Environment supports permissions, the Framework will
     * ensure that the bundle has the {@code java.io.FilePermission} with
     * actions {@code read},{@code write},{@code delete} for all files
     * (recursively) in the persistent storage area provided for the context
     * bundle.
     *
     * @param filename A relative name to the file to be accessed.
     * @return A {@code File} object that represents the requested file or
     *         {@code null} if the platform does not have file system support.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     */
    @Override
    public File getDataFile(final String filename) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Creates a {@code Filter} object. This {@code Filter} object may be used
     * to match a {@code ServiceReference} object or a {@code Dictionary}
     * object.
     * <p/>
     * <p/>
     * If the filter cannot be parsed, an {@link org.osgi.framework.InvalidSyntaxException} will be
     * thrown with a human readable message where the filter became unparsable.
     *
     * @param filter The filter string.
     * @return A {@code Filter} object encapsulating the filter string.
     * @throws org.osgi.framework.InvalidSyntaxException
     *                               If {@code filter} contains an invalid
     *                               filter string that cannot be parsed.
     * @throws NullPointerException  If {@code filter} is null.
     * @throws IllegalStateException If this BundleContext is no longer valid.
     * @see "Framework specification for a description of the filter string syntax."
     * @see org.osgi.framework.FrameworkUtil#createFilter(String)
     * @since 1.1
     */
    @Override
    public Filter createFilter(final String filter) throws InvalidSyntaxException {
        return new FilterImpl(filter);
    }

    /**
     * Returns the bundle with the specified location.
     *
     * @param location The location of the bundle to retrieve.
     * @return A {@code Bundle} object or {@code null} if the location does not
     *         match any installed bundle.
     * @since 1.6
     */
    @Override
    public Bundle getBundle(final String location) {
        return bundle;
    }
}
