/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import se.jguru.nazgul.core.algorithms.event.api.producer.EventConsumerCallback;
import se.jguru.nazgul.test.osgi.event.BundleListenerAdapter;
import se.jguru.nazgul.test.osgi.event.ServiceListenerAdapter;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBundleContextTest {

    // Shared state
    private final String osgiVersion = "2.3.4.FooBar";
    private MockBundle bundle;
    private MockBundleContext unitUnderTest;

    @Before
    public void setupSharedState() {

        bundle = new MockBundle(osgiVersion);
        unitUnderTest = (MockBundleContext) bundle.getBundleContext();
    }

    @Test
    public void validateEventPumpOnBundleStartAndStopped() throws Exception {

        // Assemble
        final TracingBundleListener eventListener = new TracingBundleListener(2, "testId");
        unitUnderTest.addBundleListener(eventListener);

        // Act
        bundle.start();
        bundle.stop();
        eventListener.onEventLatch.await();

        // Assert
        final List<BundleEvent> callTrace = eventListener.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals(BundleEvent.STARTED, callTrace.get(0).getType());
        Assert.assertEquals(BundleEvent.STOPPED, callTrace.get(1).getType());
    }

    @Test
    public void validateBundleListenerLifecycle() {

        // Assemble
        final TracingBundleListener listener1 = new TracingBundleListener("id1");
        final TracingBundleListener listener2 = new TracingBundleListener("id2");

        // Act
        unitUnderTest.addBundleListener(listener1);
        fireBundleEvent(BundleEvent.RESOLVED);

        unitUnderTest.addBundleListener(listener2);
        fireBundleEvent(BundleEvent.STARTED);

        unitUnderTest.removeBundleListener(listener1);
        fireBundleEvent(BundleEvent.STOPPING);

        unitUnderTest.removeBundleListener(listener2);
        fireBundleEvent(BundleEvent.STOPPED);

        // Assert
        final List<BundleEvent> callTrace1 = listener1.callTrace;
        final List<BundleEvent> callTrace2 = listener2.callTrace;

        Assert.assertEquals(2, callTrace1.size());
        Assert.assertEquals(2, callTrace2.size());

        Assert.assertEquals(BundleEvent.RESOLVED, callTrace1.get(0).getType());
        Assert.assertEquals(BundleEvent.STARTED, callTrace1.get(1).getType());

        Assert.assertEquals(BundleEvent.STARTED, callTrace2.get(0).getType());
        Assert.assertEquals(BundleEvent.STOPPING, callTrace2.get(1).getType());
    }

    @Test
    public void validateAcquiringBundles() {

        // Assemble
        final long bundleId = bundle.getBundleId();

        // Act
        final Bundle result = unitUnderTest.getBundle();
        final Bundle result2 = unitUnderTest.getBundle(bundleId);
        final Bundle result3 = unitUnderTest.getBundle("fooBar");
        final Bundle[] result4 = unitUnderTest.getBundles();

        // Assert
        Assert.assertSame(bundle, result);
        Assert.assertSame(bundle, result2);
        Assert.assertSame(bundle, result3);
        Assert.assertEquals(1, result4.length);
        Assert.assertSame(bundle, result4[0]);
        Assert.assertNull(unitUnderTest.getBundle(-24));
    }

    @Test
    public void validateInstallingBundle() {

        // Assemble
        final MockBundle bundle = new MockBundle("2.75");

        // Act
        final Bundle result = unitUnderTest.installBundle(bundle);
        final Bundle result2 = unitUnderTest.getBundle(bundle.getBundleId());
        final Bundle[] result3 = unitUnderTest.getBundles();

        // Assert
        Assert.assertSame(bundle, result);
        Assert.assertEquals(bundle, result2);
        Assert.assertEquals(2, result3.length);
    }

    @Test
    public void validateFilterCreationWithCorrectSyntax() throws InvalidSyntaxException {

        // Assemble
        final String filterDefinition = "(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)";

        // Act
        final Filter result = unitUnderTest.createFilter(filterDefinition);

        // Assert
        Assert.assertNotNull(result);
    }

    @Test(expected = InvalidSyntaxException.class)
    public void validateExceptionOnIncorrectLDAPFilterSyntax() throws InvalidSyntaxException {

        // Act & Assert
        unitUnderTest.createFilter("()))InvalidSyntax");
    }

    @Test
    public void validateServiceListenerLifecycle() {

        // Assemble
        final TracingServiceListener listener1 = new TracingServiceListener("id1");
        final TracingServiceListener listener2 = new TracingServiceListener("id2");

        final MockBundle mockBundle1 = new MockBundle("11.22.33");
        final MockBundle mockBundle2 = new MockBundle("4.52");

        final MockServiceReference serviceReference1 = new MockServiceReference(mockBundle1, String.class.getName());
        final MockServiceReference serviceReference2 = new MockServiceReference(mockBundle2, Date.class.getName());

        // Act
        unitUnderTest.addServiceListener(listener1);
        fireServiceEvent(ServiceEvent.REGISTERED, serviceReference1);

        unitUnderTest.addServiceListener(listener2);
        fireServiceEvent(ServiceEvent.MODIFIED, serviceReference1);
        fireServiceEvent(ServiceEvent.MODIFIED, serviceReference2);

        unitUnderTest.removeServiceListener(listener2);
        fireServiceEvent(ServiceEvent.UNREGISTERING, serviceReference2);

        unitUnderTest.removeServiceListener(listener1);
        fireServiceEvent(ServiceEvent.UNREGISTERING, serviceReference1);

        // Assert
        final List<ServiceEvent> callTrace1 = listener1.callTrace;
        final List<ServiceEvent> callTrace2 = listener2.callTrace;

        Assert.assertEquals(4, callTrace1.size());
        Assert.assertEquals(2, callTrace2.size());

        final List<Integer> serviceEventTrace1 = Arrays.asList(ServiceEvent.REGISTERED,
                ServiceEvent.MODIFIED,
                ServiceEvent.MODIFIED,
                ServiceEvent.UNREGISTERING);

        for (int i = 0; i < serviceEventTrace1.size(); i++) {
            Assert.assertEquals((int) serviceEventTrace1.get(i), callTrace1.get(i).getType());
        }

        Assert.assertSame(serviceReference1, callTrace1.get(1).getServiceReference());
        Assert.assertSame(serviceReference2, callTrace1.get(2).getServiceReference());

        Assert.assertEquals(ServiceEvent.MODIFIED, callTrace2.get(0).getType());
        Assert.assertEquals(ServiceEvent.MODIFIED, callTrace2.get(1).getType());

        Assert.assertSame(serviceReference1, callTrace2.get(0).getServiceReference());
        Assert.assertSame(serviceReference2, callTrace2.get(1).getServiceReference());
    }

    @Test
    public void validateTypedServiceRegistration() {

        // Assemble
        final Date service = new Date();

        // Act
        final ServiceRegistration<Date> result = unitUnderTest.registerService(
                Date.class, service, new Hashtable<String, Object>());
        final ServiceReference<Date> reference = result.getReference();

        // Assert
        Assert.assertNotNull(reference);
    }

    @Test
    public void validateServiceRegistrationLifecycle() throws InvalidSyntaxException {

        // Assemble
        final TracingServiceListener listener = new TracingServiceListener("testListener");
        final Date service = new Date();
        final Hashtable<String, ?> registrationProperties = new Hashtable<String, Object>();

        // Act #1
        unitUnderTest.addServiceListener(listener);
        final ServiceRegistration<?> result = unitUnderTest.registerService(
                service.getClass().getName(), service, registrationProperties);
        final ServiceReference<?> serviceReference = result.getReference();
        final ServiceReference<?>[] dateRefs = unitUnderTest.getServiceReferences(Date.class.getName(), null);
        final ServiceReference<?>[] serviceRefs = unitUnderTest.getServiceReferences((String) null, null);
        final ServiceReference<?>[] allRefs = unitUnderTest.getAllServiceReferences(null, null);

        // Assert #1
        Assert.assertNotNull(result);
        Assert.assertNotNull(serviceReference);
        Assert.assertEquals(1, dateRefs.length);
        Assert.assertSame(serviceReference, dateRefs[0]);
        Assert.assertEquals(1, serviceRefs.length);
        Assert.assertSame(serviceReference, serviceRefs[0]);
        Assert.assertEquals(1, allRefs.length);
        Assert.assertSame(serviceReference, allRefs[0]);
        Assert.assertSame(serviceReference, unitUnderTest.getServiceReference(Date.class));

        final List<ServiceEvent> callTrace = listener.callTrace;
        Assert.assertEquals(1, callTrace.size());

        ServiceEvent serviceEvent = callTrace.get(0);
        Assert.assertSame(serviceReference, serviceEvent.getServiceReference());
        Assert.assertSame(serviceReference, serviceEvent.getSource());
        Assert.assertEquals(ServiceEvent.REGISTERED, serviceEvent.getType());

        // Act #2
        unitUnderTest.unregister(serviceReference);

        // Assert #2
        Assert.assertEquals(2, callTrace.size());
        serviceEvent = callTrace.get(1);
        Assert.assertSame(serviceReference, serviceEvent.getServiceReference());
        Assert.assertEquals(ServiceEvent.UNREGISTERING, serviceEvent.getType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateInstallBundleFromStreamNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.installBundle("irrelevant", null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateInstallBundleFromURLNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.installBundle("irrelevant");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGenericGetServiceReferenceNotImplemented() throws InvalidSyntaxException {

        // Act & Assert
        unitUnderTest.getServiceReferences(Date.class, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateAddingFrameworkListenerNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.addFrameworkListener(new FrameworkListener() {
            @Override
            public void frameworkEvent(FrameworkEvent event) {
                // Do nothing
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateRemovingFrameworkListenerNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.removeFrameworkListener(new FrameworkListener() {
            @Override
            public void frameworkEvent(FrameworkEvent event) {
                // Do nothing
            }
        });
    }

    //
    // Private helpers
    //

    private void fireBundleEvent(int bundleEventType) {

        final BundleEvent toFire = new BundleEvent(bundleEventType, bundle, bundle);

        unitUnderTest.fireBundleEvent(new EventConsumerCallback<BundleListenerAdapter>() {
            @Override
            public void onEvent(BundleListenerAdapter eventConsumer) {
                eventConsumer.bundleChanged(toFire);
            }
        });
    }

    private void fireServiceEvent(int serviceEventType, final ServiceReference reference) {

        final ServiceEvent toFire = new ServiceEvent(serviceEventType, reference);

        unitUnderTest.fireServiceEvent(new EventConsumerCallback<ServiceListenerAdapter>() {
            @Override
            public void onEvent(ServiceListenerAdapter eventConsumer) {
                eventConsumer.serviceChanged(toFire);
            }
        });
    }
}
