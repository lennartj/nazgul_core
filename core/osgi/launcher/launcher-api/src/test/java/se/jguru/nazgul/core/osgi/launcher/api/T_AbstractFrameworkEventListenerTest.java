/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.osgi.launcher.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.MockBlueprintServiceEventAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractFrameworkEventListenerTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(T_AbstractFrameworkEventListenerTest.class);

    // Shared state
    private MockBlueprintServiceEventAdapter listener;
    private FelixFrameworkLauncher unitUnderTest;
    private Map<String, String> configuration;
    private File targetDirectory;
    private File downloadedBundlesDirectory;
    private boolean stopFramework;
    private static int iteration = 0;

    @Before
    public void launchFelix() {

        // Find the relevant directories
        final URL origin = getClass().getClassLoader().getResource("test_origin.txt");
        Assert.assertNotNull(origin);
        targetDirectory = new File(origin.getPath()).getParentFile().getParentFile();
        Assert.assertTrue(targetDirectory.isDirectory());
        Assert.assertEquals("target", targetDirectory.getName());

        downloadedBundlesDirectory = new File(targetDirectory, "downloadedBundles");
        Assert.assertTrue(downloadedBundlesDirectory.isDirectory());

        stopFramework = true;

        // Create the embedder
        unitUnderTest = new FelixFrameworkLauncher("testID_" + iteration++);

        final File userDir = new File(targetDirectory, "userDir_" + iteration);
        userDir.delete();

        // Create base configuration properties
        configuration = new TreeMap<String, String>();
        configuration.put("felix.log.level", "3");
        configuration.put("felix.cache.rootdir", new File(userDir, "felix-cache").getAbsolutePath());

        // Create the ServiceEventAdapter
        listener = new MockBlueprintServiceEventAdapter("testListener");
    }

    @After
    public void shutdownFelix() throws Exception {

        if (stopFramework) {
            try {
                unitUnderTest.stop();
                unitUnderTest.getFramework().waitForStop(0);
            } catch (Exception e) {
                log.error("Could not properly stop Felix", e);
            }
        }
    }

    @Test
    public void validateLifecycleOnAddingConsumersBeforeFrameworkIsLaunched()
            throws BundleException, InvalidSyntaxException {

        // Act
        unitUnderTest.addConsumer(listener);
        unitUnderTest.addConsumer(listener);
        final int sizeBeforeInit = getMapOfPendingBundleContextHolders(true).size();

        unitUnderTest.initialize(configuration);
        unitUnderTest.start();

        final int sizeAfterInit = getMapOfPendingBundleContextHolders(true).size();

        final Framework felix = unitUnderTest.getFramework();
        final BundleContext mainCtx = felix.getBundleContext();

        installAllDownloadedBundles(downloadedBundlesDirectory, mainCtx, true);

        /*
        final DeploymentAdmin deploymentAdmin = getServiceInTheStupidWay(DeploymentAdmin.class, mainCtx);
        for(DeploymentPackage current : deploymentAdmin.listDeploymentPackages()) {
            log.info(" ==> " + current.getName() + " (" + current.getDisplayName() + ") -- ");
        }

        final ServiceReference packageAdminServiceReference = mainCtx.getServiceReference(PackageAdmin.class.getName());
        final PackageAdmin packageAdmin = (PackageAdmin) felix.getBundleContext()
                .getService(packageAdminServiceReference);
        */


        for (ServiceReference current : mainCtx.getAllServiceReferences(null, null)) {
            log.info("==> " + current + " from bundle [" + current.getBundle().getSymbolicName() + "]");
        }

        for (Bundle current : mainCtx.getBundles()) {
            log.info("Bundle [" + current.getBundleId() + "]: " + current.getSymbolicName() + " (" + current
                    .getVersion() + ") -- " + getState(current.getState()));
        }

        // Assert
        Assert.assertEquals(1, sizeBeforeInit);
        Assert.assertEquals(0, sizeAfterInit);
    }

    @Test
    public void validateLifecycleOnAddingListenersAfterFrameworkStart() throws Exception {

        // Act
        unitUnderTest.initialize(configuration);
        unitUnderTest.start();

        unitUnderTest.addConsumer(listener);
        unitUnderTest.addConsumer(listener);
        final int pendingListenersSize = getMapOfPendingBundleContextHolders(true).size();

        final Framework felix = unitUnderTest.getFramework();
        final BundleContext mainCtx = felix.getBundleContext();

        installAllDownloadedBundles(downloadedBundlesDirectory, mainCtx, true);

        // Assert
        Assert.assertEquals(0, pendingListenersSize);
    }

    @Test
    public void validateRemovingListenersBeforeFrameworkStart() throws Exception {

        // Assemble
        final MockBlueprintServiceEventAdapter listener1 = new MockBlueprintServiceEventAdapter("testListener1");
        final MockBlueprintServiceEventAdapter listener2 = new MockBlueprintServiceEventAdapter("testListener2");

        // Act
        unitUnderTest.addConsumer(listener);
        unitUnderTest.addConsumer(listener);
        unitUnderTest.addConsumer(listener2);
        unitUnderTest.removeConsumer("nonexistent");
        unitUnderTest.removeConsumer(listener2.getClusterId());
        final int toRegisterSizeBeforeInit = getMapOfPendingBundleContextHolders(true).size();
        final int toDeregisterSizeBeforeInit = getMapOfPendingBundleContextHolders(false).size();

        unitUnderTest.initialize(configuration);
        unitUnderTest.start();

        final int toRegisterSizeAfterInit = getMapOfPendingBundleContextHolders(true).size();
        final int toDeregisterSizeAfterInit = getMapOfPendingBundleContextHolders(false).size();

        final Framework felix = unitUnderTest.getFramework();
        final BundleContext mainCtx = felix.getBundleContext();

        installAllDownloadedBundles(downloadedBundlesDirectory, mainCtx, true);

        /*
        final DeploymentAdmin deploymentAdmin = getServiceInTheStupidWay(DeploymentAdmin.class, mainCtx);
        for(DeploymentPackage current : deploymentAdmin.listDeploymentPackages()) {
            log.info(" ==> " + current.getName() + " (" + current.getDisplayName() + ") -- ");
        }

        final ServiceReference packageAdminServiceReference = mainCtx.getServiceReference(PackageAdmin.class.getName());
        final PackageAdmin packageAdmin = (PackageAdmin) felix.getBundleContext()
                .getService(packageAdminServiceReference);


        for (ServiceReference current : mainCtx.getAllServiceReferences(null, null)) {
            System.out.println("==> " + current + " from bundle [" + current.getBundle().getSymbolicName() + "]");
        }

        for (Bundle current : mainCtx.getBundles()) {
            log.info("Bundle [" + current.getBundleId() + "]: " + current.getSymbolicName() + " (" + current
                    .getVersion() + ") -- " + getState(current.getState()));
        }
        */

        // Assert
        Assert.assertEquals(2, toRegisterSizeBeforeInit);
        Assert.assertEquals(1, toDeregisterSizeBeforeInit);
        Assert.assertEquals(0, toRegisterSizeAfterInit);
        Assert.assertEquals(0, toDeregisterSizeAfterInit);
    }

    @Test
    public void validateLifecycleOnAddingListenersWithoutFrameworkStart() throws Exception {

        // Assemble
        stopFramework = false;

        // Act
        unitUnderTest.initialize(configuration);

        unitUnderTest.addConsumer(listener);
        unitUnderTest.addConsumer(listener);

        final Framework felix = unitUnderTest.getFramework();

        // Assert
        final BundleContext bundleContext = felix.getBundleContext();
        Assert.assertNotNull(bundleContext);
        Assert.assertEquals(Bundle.STARTING, felix.getState());
    }

    //
    // Private helpers
    //

    private Map<String, BundleContextHolder> getMapOfPendingBundleContextHolders(boolean toRegister) {

        final String fieldName = toRegister ? "toRegister" : "toDeregister";
        try {
            final Field theField = AbstractFrameworkLauncher.class.getDeclaredField(fieldName);
            theField.setAccessible(true);

            return (Map<String, BundleContextHolder>) theField.get(unitUnderTest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getState(int bundleState) {

        // UNINSTALLED,INSTALLED, RESOLVED,STARTING, STOPPING,ACTIVE.
        String toReturn;

        switch (bundleState) {
            case Bundle.UNINSTALLED:
                toReturn = "UNINSTALLED";
                break;

            case Bundle.INSTALLED:
                toReturn = "INSTALLED";
                break;

            case Bundle.RESOLVED:
                toReturn = "RESOLVED";
                break;

            case Bundle.STARTING:
                toReturn = "STARTING";
                break;

            case Bundle.STOPPING:
                toReturn = "STOPPING";
                break;

            case Bundle.ACTIVE:
                toReturn = "ACTIVE";
                break;

            default:
                toReturn = "<Unknown> (" + bundleState + ")";
                break;
        }

        return toReturn;
    }

    public static <T> T getServiceInTheStupidWay(Class<T> apiType, BundleContext ctx) {

        ServiceReference ref = ctx.getServiceReference(apiType.getName());
        return (T) ctx.getService(ref);
    }

    public static void installAllDownloadedBundles(File downloadDirectory, BundleContext ctx, boolean start) {

        List<Bundle> installed = new ArrayList<Bundle>();

        for (File current : downloadDirectory.listFiles()) {
            final String absolutePath = current.getAbsolutePath();
            Bundle currentBundle = null;
            try {
                currentBundle = ctx.installBundle("file:/" + absolutePath, new FileInputStream(current));
                installed.add(currentBundle);
            } catch (Exception e) {
                String bundleSymbolicName = currentBundle == null ? "<unknown>" : currentBundle.getSymbolicName();
                log.error("Could not install bundle [" + bundleSymbolicName + "]", e);
            }
        }

        if (start) {
            for (Bundle current : installed) {
                try {
                    current.start();
                } catch (BundleException e) {
                    log.error("Could not start bundle [" + current.getSymbolicName() + "]", e);
                }
            }
        }
    }
}
