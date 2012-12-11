/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.junit.Assert;
import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.BundleException;

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
        final Map<String, String> configuration = new HashMap<String, String>();

        unitUnderTest.fw.init();
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

        Assert.assertEquals("synchronizeListenerRegistration", callTrace.get(0));
        Assert.assertEquals("onStart", callTrace.get(1));
        Assert.assertEquals("synchronizeListenerRegistration", callTrace.get(2));
        Assert.assertEquals("onStop", callTrace.get(3));
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionStartingNonInitializedFramework() throws BundleException {

        // Assemble
        final MockFrameworkLauncher unitUnderTest = new MockFrameworkLauncher("testId");
        unitUnderTest.fw = null;

        // Act
        unitUnderTest.start();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionStoppingNonInitializedFramework() throws BundleException {

        // Assemble
        final MockFrameworkLauncher unitUnderTest = new MockFrameworkLauncher("testId");
        unitUnderTest.fw = null;

        // Act
        unitUnderTest.stop();
    }
}
