/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.easymock.EasyMock;
import org.junit.Test;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceEventAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractFrameworkLauncherTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullTClass() {

        // Assemble
        new MockFrameworkLauncher("testId", null);
    }

    @Test
    public void validateFrameworkLauncherLifecycle() throws Exception {

        // Assemble
        final MockFrameworkLauncher unitUnderTest = new MockFrameworkLauncher("testId");
        final Map<String, Object> configuration = new HashMap<String, Object>();

        unitUnderTest.fw.start();
        EasyMock.expect(unitUnderTest.fw.getBundleContext()).andReturn(unitUnderTest.ctx).times(4);
        unitUnderTest.fw.stop();
        EasyMock.replay(unitUnderTest.fw);

        // Act
        unitUnderTest.initialize(configuration);
        unitUnderTest.start();
        unitUnderTest.stop();

        // Assert
        EasyMock.verify(unitUnderTest.fw);
        final List<String> callTrace = unitUnderTest.callTrace;

        // synchronizeListenerRegistration,
        // onStart,
        // synchronizeListenerRegistration,
        // onStop
        System.out.println("Got: " + callTrace);
    }
}
