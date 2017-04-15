/*
 * #%L
 * Nazgul Project: nazgul-core-jmx-test
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.jmx.test.mbeanserver;

import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * jUnit rule implementation to manage a local (i.e. in-process) MBeanServer during a Test Case.
 * This has significance for all tests requiring a local and controllable MBean server.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalMBeanServerRule extends TestWatcher {

    // Shared state
    private final Object[] lock = new Object[0];
    private SortedSet<String> domains;
    private MBeanServer mBeanServer;
    private boolean unregisterAllBeansInTestDomain = true;
    private String jmxBeanTestDomain;

    /**
     * Default constructor creating a new LocalMBeanServerRule which unregisters all beans in its TestDomain
     * after the test completes (i.e. in the finished() method of this class).
     */
    public LocalMBeanServerRule() {
        this(true);
    }

    /**
     * Compound constructor creating a new LocalMBeanServerRule which performs automatic de-registration
     * of all beans in the test JMX domain.
     *
     * @param unregisterAllBeansInTestDomain If {@code true}, all beans in the test JMX domain are unregistered
     *                                       when the test finishes.
     * @see #finished(Description)
     */
    public LocalMBeanServerRule(final boolean unregisterAllBeansInTestDomain) {

        // Assign internal state
        this.unregisterAllBeansInTestDomain = unregisterAllBeansInTestDomain;
    }

    /**
     * <p>Starts a local/in-process MBeanServer, and validates that the JMX test domain is empty (i.e. not
     * registered as a domain in the JMX server when the test starts). The test domain is identical to the
     * class name of the test class:</p>
     * <pre>
     * <code>
     *     // Stash the name of the test JMX domain.
     *     setJmxBeanTestDomain(description.getClassName());
     * </code>
     * </pre>
     *
     * @see #getJmxBeanTestDomain()
     * @see #setJmxBeanTestDomain(String)
     */
    @Override
    protected void starting(final Description description) {

        // Acquire the local MBeanServer.
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Assert.assertNotNull("Could not retrieve PlatformMBeanServer.", mBeanServer);

        // Stash the name of the test JMX domain.
        setJmxBeanTestDomain(description.getClassName());

        // Retrieve the existing domains from the mBeanServer.
        domains = new TreeSet<>();
        getDomains();

        // The JMX bean test domain should be empty at the start of the unit test.
        Assert.assertFalse("Test domain [" + getJmxBeanTestDomain() + "] already present within local mBeanServer. "
                        + "Your unit test state should be cleaned out before launching a JMX test.",
                this.domains.contains(getJmxBeanTestDomain()));
    }

    /**
     * Calls {@code mBeanServer.unregisterMBean(objectName);} for each MBean whose objectName starts with the
     * jmxBeanTestDomain string - which normally is the same as the fully qualified class name of the test
     * class being executed.
     *
     * @throws MBeanServerUnregisterException if the LocalMBeanServerRule was instructed to unregister all beans in
     *                                        the test domain and was unable to do so.
     */
    @Override
    protected void finished(final Description description) {

        final List<Exception> exceptions = new ArrayList<>();

        // Remove the test domain and all its sub-elements, if asked to.
        if (unregisterAllBeansInTestDomain) {
            for (ObjectInstance current : mBeanServer.queryMBeans(null, null)) {
                final ObjectName objectName = current.getObjectName();
                if (objectName.getDomain().startsWith(getJmxBeanTestDomain())) {
                    try {
                        mBeanServer.unregisterMBean(objectName);
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
            }
        }

        if (exceptions.size() > 0) {
            final MBeanServerUnregisterException ex = new MBeanServerUnregisterException("unregistering MBeans");
            ex.getExceptions().addAll(exceptions);
        }
    }

    /**
     * Override to provide a JMX domain other than the default test domain.
     *
     * @return {@code getClass().getPackage().getName()}, unless overridden in subclasses.
     */
    public String getJmxBeanTestDomain() {
        return jmxBeanTestDomain;
    }

    /**
     * Assigns the JMX bean test domain, which is a domain where test code is applied.
     * The JMX bean test domain is normally empty at the start of the unit.
     *
     * @param jmxBeanTestDomain The JMX bean test domain to assign to the local MBeanServer.
     */
    public void setJmxBeanTestDomain(final String jmxBeanTestDomain) {

        // Check sanity
        Validate.notNull(jmxBeanTestDomain, "jmxBeanTestDomain");

        // Assign internal state
        this.jmxBeanTestDomain = jmxBeanTestDomain;
    }

    /**
     * @return An unmodifiable SortedSet containing the domains currently registered within the MBean server.
     */
    public SortedSet<String> getDomains() {

        final String[] currentDomains = mBeanServer.getDomains();
        if (currentDomains.length != domains.size()) {
            synchronized (lock) {

                // Retrieve the existing domains from the mBeanServer.
                domains.clear();
                Collections.addAll(domains, currentDomains);
            }
        }

        // All done.
        return Collections.unmodifiableSortedSet(domains);
    }

    /**
     * Retrieves the value of the attribute with the supplied name from the given ObjectName.
     *
     * @param objectName    The objectName of the MBean whose readable attribute should be retrieved.
     * @param attributeName The name of the attribute. <strong>Note!</strong> The attribute should be capitalized,
     *                      implying that a JavaBean getter {@code String getFoo()} implies an attributeName
     *                      "Foo" (not "foo"), according to the JMX specification.
     * @param <T>           The type of the JMX attribute requested.
     * @return The value of the attribute in the supplied objectName.
     */
    public final <T> T getAttribute(final ObjectName objectName, final String attributeName) {

        try {
            return (T) mBeanServer.getAttribute(objectName, attributeName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Attribute [" + attributeName + "] was not found in objectName ["
                    + objectName + "]", e);
        }
    }

    /**
     * Retrieves an MBean proxy with the supplied interface type from the local MBeanServer.
     *
     * @param objectName    The objectName of the MBean which must implement the supplied interfaceType.
     * @param interfaceType The interface type which should be retrieved.
     * @param <T>           The interface type to retrieve.
     * @return An MBean proxy implementing the supplied interfaceType.
     */
    public final <T> T getMXBeanProxy(final ObjectName objectName, final Class<T> interfaceType) {
        return JMX.newMXBeanProxy(mBeanServer, objectName, interfaceType, true);
    }

    /**
     * Retrieves all ObjectInstances within the JMX domain supplied.
     *
     * @param jmxDomain a name of a JMX domain. Cannot be null.
     * @return a Set containing all ObjectInstances (i.e. MBeans) within the supplied JMX domain.
     */
    public Set<ObjectInstance> getMBeansInDomain(final String jmxDomain) {
        return mBeanServer.queryMBeans(getSearchObjectNameFor(jmxDomain), null);
    }

    /**
     * Retrieves all ObjectNames within the JMX domain supplied.
     *
     * @param jmxDomain a name of a JMX domain. Cannot be null.
     * @return a Set containing all ObjectNames within the supplied JMX domain.
     */
    public Set<ObjectName> getNamesInDomain(final String jmxDomain) {
        return mBeanServer.queryNames(getSearchObjectNameFor(jmxDomain), null);
    }

    /**
     * Retrieves the active MBeanServer from this Rule.
     * Normally, this
     *
     * @return the active MBeanServer from this Rule.
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    //
    // Private helpers
    //

    private ObjectName getSearchObjectNameFor(final String jmxDomain) {

        // Check sanity
        Validate.notNull(jmxDomain, "jmxDomain");

        try {
            return new ObjectName(jmxDomain + ":*");
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Could not create ObjectName for jmxDomain '" + jmxDomain + "'", e);
        }
    }
}
