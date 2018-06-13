/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-impl-felix
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


package se.jguru.nazgul.core.osgi.launcher.impl.felix;

import org.apache.felix.framework.util.FelixConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.clustering.api.ConstantIdGenerator;
import se.jguru.nazgul.core.osgi.launcher.impl.felix.event.MockBlueprintServiceEventAdapter;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FelixLauncherTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(FelixLauncherTest.class);

    // Shared state
    private MockBlueprintServiceEventAdapter listener;
    private FelixLauncher unitUnderTest;
    private Map<String, String> configuration;
    private File targetDirectory;
    private File downloadedBundlesDirectory;
    private boolean stopFramework;

    @Before
    public void launchFelix() {

        // Find the relevant directories
        final URL origin = getClass().getClassLoader().getResource("felix_test_origin.txt");
        Assert.assertNotNull(origin);
        targetDirectory = new File(origin.getPath()).getParentFile().getParentFile();
        Assert.assertTrue(targetDirectory.isDirectory());
        Assert.assertEquals("target", targetDirectory.getName());

        downloadedBundlesDirectory = new File(targetDirectory, "downloadedBundles");
        Assert.assertTrue(downloadedBundlesDirectory.isDirectory());

        stopFramework = true;

        // Create the embedder
        unitUnderTest = new FelixLauncher("testID");

        // Create base configuration properties
        configuration = new TreeMap<>();
        configuration.put(FelixConstants.LOG_LEVEL_PROP, "" + org.apache.felix.framework.Logger.LOG_DEBUG);
        configuration.put("felix.cache.rootdir", new File(targetDirectory, "felix-cache").getAbsolutePath());

        // Create the ServiceEventAdapter
        listener = new MockBlueprintServiceEventAdapter("testListener");
    }

    @After
    public void shutdownFelix() throws Exception {

        try {
            if (stopFramework) {
                unitUnderTest.stop();
                unitUnderTest.getFramework().waitForStop(0);
            }
        } catch (Exception e) {

            // Just swallow this; can happen for a pure Maven launcher during release.
            e.printStackTrace();
        }
    }

    @Test
    public void validateLaunchAndFrameworkAccess() throws Exception {

        // Assemble
        unitUnderTest.addConsumer(listener);

        // Act & Assert #1
        unitUnderTest.initialize(configuration);
        unitUnderTest.start();

        Framework framework = unitUnderTest.getFramework();
        Assert.assertNotNull(framework);
        Assert.assertEquals(Framework.ACTIVE, framework.getState());
    }

    @Test
    public void validateIdGeneratorConstruction() throws Exception {

        // Assemble
        unitUnderTest = new FelixLauncher(new ConstantIdGenerator("testID"));
        unitUnderTest.addConsumer(listener);

        // Act & Assert #1
        unitUnderTest.initialize(configuration);
        unitUnderTest.start();

        Framework framework = unitUnderTest.getFramework();
        Assert.assertNotNull(framework);
        Assert.assertEquals(Framework.ACTIVE, framework.getState());
    }
}
