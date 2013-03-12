/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.test.osgi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBundleTest {

    // Shared state
    private String osgiStringVersion = "1.2.3.SNAPSHOT";
    private MockBundle unitUnderTest;

    @Before
    public void setupSharedState() {
        unitUnderTest = new MockBundle(osgiStringVersion);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullOsgiStringVersion() {

        // Act & Assert
        new MockBundle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyOsgiStringVersion() {

        // Act & Assert
        new MockBundle("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateFindEntriesNotImplemented() {

        // Act & Assert
        unitUnderTest.findEntries("foo", "bar", true);
    }

    @Test
    public void validateStandardHeaders() {

        // Assemble
        final List<String> expectedProperties = Arrays.asList(
                Constants.BUNDLE_MANIFESTVERSION,
                Constants.BUNDLE_NAME,
                Constants.BUNDLE_VENDOR,
                Constants.BUNDLE_VERSION,
                Constants.BUNDLE_DESCRIPTION,
                Constants.BUNDLE_SYMBOLICNAME);

        // Act
        final Dictionary<String, String> standardHeaders = MockBundle.getStandardHeaders(osgiStringVersion);
        final Dictionary<String, String> headers = unitUnderTest.getHeaders("ignored");

        // Assert
        Assert.assertNotNull(standardHeaders);
        Assert.assertEquals(expectedProperties.size(), standardHeaders.size());
        for (String current : expectedProperties) {
            Assert.assertNotNull(standardHeaders.get(current));
        }
        Assert.assertEquals(standardHeaders, headers);
    }

    @Test
    public void validateRemovingBundleHeader() {

        // Assemble
        final Dictionary<String, String> standardHeaders = MockBundle.getStandardHeaders(osgiStringVersion);

        // Act
        unitUnderTest.removeHeader(Constants.BUNDLE_DESCRIPTION);

        // Assert
        Assert.assertEquals(standardHeaders.size() - 1, unitUnderTest.getHeaders().size());
    }

    @Test
    public void validateBundleIdIsInSequence() {

        // Assemble
        final Bundle bundleOne = new MockBundle("2.3.4");
        final Bundle bundleTwo = new MockBundle("2.3.4");

        // Act & Assert
        Assert.assertEquals(bundleOne.getBundleId() + 1, bundleTwo.getBundleId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetLocationNotImplemented() {

        // Act & Assert
        unitUnderTest.getLocation();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRegisteredServicesNotImplemented() {

        // Act & Assert
        unitUnderTest.getRegisteredServices();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetServicesInUseNotImplemented() {

        // Act & Assert
        unitUnderTest.getServicesInUse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateHasPermissionNotImplemented() {

        // Act & Assert
        unitUnderTest.hasPermission(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetResourceNotImplemented() {

        // Act & Assert
        unitUnderTest.getResource("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateLoadClassNotImplemented() throws ClassNotFoundException {

        // Act & Assert
        unitUnderTest.loadClass("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetResourcesNotImplemented() throws IOException {

        // Act & Assert
        unitUnderTest.getResources("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetEntryPathsNotImplemented() {

        // Act & Asset
        unitUnderTest.getEntryPaths("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetEntryNotImplemented() {

        // Act & Assert
        unitUnderTest.getEntry("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetLastModifiedNotImplemented() {

        // Act & Assert
        unitUnderTest.getLastModified();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetSignerCertificatesNotImplemented() {

        // Act & Assert
        unitUnderTest.getSignerCertificates(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetStateNotImplemented() {

        // Act & Assert
        unitUnderTest.getState();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateStartWithOptionsNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.start(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateStopWithOptionsNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.stop(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateUninstallNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.uninstall();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateUpdateNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.update();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateGetDatafileNotImplemented() throws BundleException {

        // Act & Assert
        unitUnderTest.getDataFile("irrelevant");
    }

    @Test
    public void validateSymbolicNameManagement() {

        // Assemble
        final String symbolicName = "SomeBundleName";

        // Act
        final String defaultSymbolicName = unitUnderTest.getSymbolicName();
        unitUnderTest.setHeader(Constants.BUNDLE_SYMBOLICNAME, symbolicName);

        // Assert
        Assert.assertEquals("SymbolicNameForMockBundle", defaultSymbolicName);
        Assert.assertEquals(symbolicName, unitUnderTest.getSymbolicName());
    }

    @Test
    public void validateVersionHandling() {

        // Act
        final Version version = unitUnderTest.getVersion();

        // Assert
        assertEquals(osgiStringVersion, version.toString());
    }

    @Test
    public void validateEqualityAndComparison() {

        // Assemble
        final Bundle bundleOne = new MockBundle("1.2.3.foobar");
        final Bundle bundleTwo = new MockBundle("2.3.4.foobar");

        // Act
        final Long bundleOneId = bundleOne.getBundleId();
        final Long bundleTwoId = bundleTwo.getBundleId();

        // Assert
        Assert.assertTrue(bundleOne.equals(bundleOne));
        Assert.assertFalse(bundleOne.equals(null));
        Assert.assertFalse(bundleOne.equals(bundleTwo));
        Assert.assertFalse(bundleOne.equals("foobar"));

        Assert.assertEquals(-1, bundleOne.compareTo(null));
        Assert.assertEquals(bundleOneId.compareTo(bundleTwoId), bundleOne.compareTo(bundleTwo));
    }

    @Test
    public void validateListenerCallbacksOnStartStopLifecycle() throws BundleException {

        // Assemble
        final TracingBundleListener bundleListener = new TracingBundleListener("testId");
        unitUnderTest.getBundleContext().addBundleListener(bundleListener);

        // Act
        unitUnderTest.start();
        unitUnderTest.stop();

        // Assert
        final List<BundleEvent> callTrace = bundleListener.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals(BundleEvent.STARTED, callTrace.get(0).getType());
        Assert.assertEquals(BundleEvent.STOPPED, callTrace.get(1).getType());
    }

    @Test
    public void validateAdaption() {

        // Act
        final Serializable serializable = unitUnderTest.adapt(Serializable.class);
        final BundleContext shouldBeNull = unitUnderTest.adapt(BundleContext.class);

        // Assert
        Assert.assertNotNull(serializable);
        Assert.assertNull(shouldBeNull);
        Assert.assertSame(unitUnderTest, serializable);
    }
}
