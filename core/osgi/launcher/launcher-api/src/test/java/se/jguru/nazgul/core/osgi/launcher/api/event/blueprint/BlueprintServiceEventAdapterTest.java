/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import org.junit.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.test.osgi.MockBundle;
import se.jguru.nazgul.test.osgi.MockBundleContext;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BlueprintServiceEventAdapterTest {

    // Shared state
    private MockBundleContext bundleContext;
    private MockBundle bundle;
    private MockBlueprintServiceEventAdapter mockServiceListener;
    private Hashtable<String, String> blueprintContainerProperties;

    @Before
    public void setupSharedState() {

        mockServiceListener = new MockBlueprintServiceEventAdapter("testAdapter");
        bundle = new MockBundle("1.2.3.Lennart");
        bundleContext = (MockBundleContext) bundle.getBundleContext();
        blueprintContainerProperties = new Hashtable<String, String>();
        blueprintContainerProperties.put(BlueprintServiceFilter.BLUEPRINT_NAME_KEY, "foobar");
    }

    @Test
    public void validateServiceRegistrationLifecycle() {

        // Assemble
        final BlueprintServiceEventAdapter unitUnderTest = new BlueprintServiceEventAdapter(mockServiceListener);
        final BlueprintContainer blueprintContainer = EasyMock.createMock(BlueprintContainer.class);
        EasyMock.replay(blueprintContainer);

        // Act
        unitUnderTest.register(bundleContext);
        final ServiceRegistration reg = bundleContext.registerService(
                BlueprintContainer.class, blueprintContainer, blueprintContainerProperties);

        unitUnderTest.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED, reg.getReference()));
        unitUnderTest.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, reg.getReference()));
        unitUnderTest.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, reg.getReference()));
        unitUnderTest.unregister(bundleContext);

        // Assert
        EasyMock.verify(blueprintContainer);
        Assert.assertSame(mockServiceListener, unitUnderTest.getDelegate());

        final List<BlueprintContainer> callTrace = mockServiceListener.callTrace;

        Assert.assertEquals(4, callTrace.size());
        Assert.assertSame(blueprintContainer, callTrace.get(0));
        Assert.assertEquals(mockServiceListener.getClusterId(), unitUnderTest.getClusterId());
    }

    @Test
    public void validateComparingBlueprintServiceEventAdapters() {

        // Assemble
        final BlueprintServiceEventAdapter unitUnderTest = new BlueprintServiceEventAdapter(mockServiceListener);
        final BlueprintServiceEventAdapter unitUnderTest2 = new BlueprintServiceEventAdapter(mockServiceListener);

        // Act & Assert
        Assert.assertEquals(-1, unitUnderTest.compareTo(null));
        Assert.assertEquals(0, unitUnderTest.compareTo(unitUnderTest2));
    }

    @Test
    public void validateNoServiceEventHandlingForNonBlueprintServices() {

        // Assemble
        final BlueprintServiceEventAdapter unitUnderTest = new BlueprintServiceEventAdapter(mockServiceListener);
        final BlueprintContainer blueprintContainer = EasyMock.createMock(BlueprintContainer.class);
        EasyMock.replay(blueprintContainer);

        // Act
        unitUnderTest.register(bundleContext);
        final ServiceRegistration reg = bundleContext.registerService(
                Date.class, new Date(), new Hashtable<String, Object>());

        unitUnderTest.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED, reg.getReference()));

        // Assert
        final List<BlueprintContainer> callTrace = mockServiceListener.callTrace;
        Assert.assertEquals(0, callTrace.size());
    }
}
