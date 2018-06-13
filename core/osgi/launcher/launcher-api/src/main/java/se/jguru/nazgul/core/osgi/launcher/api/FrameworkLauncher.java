/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-api
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


package se.jguru.nazgul.core.osgi.launcher.api;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import java.util.Map;

/**
 * <p>Specification for launching an OSGi Framework from within the currently running
 * application. The lifecycle of a FrameworkLauncher is as follows:</p>
 * <h2>Startup sequence</h2>
 * <pre>
 *      // Inject or acquire the FrameworkLauncher instance,
 *      // and any supplied configuration data.
 *      final FrameworkLauncher launcher = ....
 *      final Map&lt;String, Object&gt; configuration = ...
 *
 *      // Initialize the FrameworkLauncher
 *      launcher.initialize(configuration);
 *
 *      // Start the FrameworkLauncher; acquire the started Framework.
 *      launcher.start();
 *      final Framework framework = launcher.getFramework();
 * </pre>
 * <h2>Shutdown sequence</h2>
 * <pre>
 *      // Acquire the FrameworkLauncher instance,
 *      final FrameworkLauncher launcher = ....
 *
 *      // Stop the FrameworkLauncher; which will stop
 *      // the managed OSGi Framework.
 *      launcher.stop();
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface FrameworkLauncher {

    /**
     * The standard boot delegation packages (i.e. packages intended to be
     * loaded by the ClassLoader of the embedding container or runtime), originating
     * from the Sun/Oracle JVM.
     */
    String DEFAULT_JVM_BOOTDELEGATION_PACKAGES = "sun.*,com.sun.*,javax.transaction,javax.transaction.*";

    /**
     * Initializes this launcher using the provided configuration map.
     *
     * @param configuration A Map holding configuration data for the underlying OSGi container.
     * @throws IllegalStateException    if the container was already started; initializing a
     *                                  running container yields undefined results.
     * @throws IllegalArgumentException if the configuration was deemed inappropriate for
     *                                  initializing the underlying container.
     */
    void initialize(final Map<String, String> configuration) throws IllegalStateException;

    /**
     * Starts the embedded OSGi container.
     *
     * @throws BundleException       if the container could not be started.
     * @throws IllegalStateException if the underlying OSGi framework was not created/initialized.
     */
    void start() throws BundleException, IllegalStateException;

    /**
     * Acquires the OSGi framework from the underlying container.
     *
     * @return The Framework of the running Container, or <code>null</code> if the
     * Framework was not created (i.e. the FrameworkFactory was not run yet).
     */
    Framework getFramework();

    /**
     * Stops the embedded OSGi container.
     *
     * @throws BundleException       if the container could not be stopped.
     * @throws IllegalStateException if the underlying OSGi framework was not created/initialized.
     */
    void stop() throws BundleException, IllegalStateException;
}
