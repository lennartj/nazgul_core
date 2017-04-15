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

import org.junit.Rule;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.Set;

/**
 * Convenience superclass for JMX-related tests, wrapping a standard LocalMBeanServerRule
 * and some convenience methods for invoking methods within the rule.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class AbstractJmxTest {

    /**
     * Include a LocalMBeanServerRule with automatic un-registration of test-domain
     * JMX beans for this AbstractJmxTest.
     *
     * @see LocalMBeanServerRule
     */
    @Rule public LocalMBeanServerRule rule = new LocalMBeanServerRule(true);

    /**
     * Retrieves the JMX MBean Domain for test-scope objects.
     * This JMX domain will be cleaned after the test completes.
     *
     * @return {@code rule.getJmxBeanTestDomain()}
     * @see LocalMBeanServerRule#getJmxBeanTestDomain()
     */
    protected final String getTestDomain() {
        return rule.getJmxBeanTestDomain();
    }

    /**
     * Retrieves all MBeans in the supplied JMX domain.
     *
     * @param jmxDomain a non-null JMX domain, such as "java.lang" or {@code getTestDomain()}
     * @return The ObjectInstances in the supplied JMX domain.
     * @see LocalMBeanServerRule#getMBeansInDomain(String)
     */
    protected final Set<ObjectInstance> getMBeansInDomain(final String jmxDomain) {
        return rule.getMBeansInDomain(jmxDomain);
    }

    /**
     * Retrieves all ObjectNames in the supplied JMX domain.
     *
     * @param jmxDomain a non-null JMX domain, such as "java.lang" or {@code getTestDomain()}
     * @return The ObjectNames in the supplied JMX domain.
     * @see LocalMBeanServerRule#getNamesInDomain(String)
     */
    protected final Set<ObjectName> getNamesInDomain(final String jmxDomain) {
        return rule.getNamesInDomain(jmxDomain);
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
     * @see LocalMBeanServerRule#getAttribute(ObjectName, String)
     */
    protected final <T> T getAttribute(final ObjectName objectName, final String attributeName) {
        return rule.getAttribute(objectName, attributeName);
    }

    /**
     * Retrieves an MBean proxy with the supplied interface type from the local MBeanServer.
     *
     * @param objectName    The objectName of the MBean which must implement the supplied interfaceType.
     * @param interfaceType The interface type which should be retrieved.
     * @param <T>           The interface type to retrieve.
     * @return An MBean proxy implementing the supplied interfaceType.
     * @see LocalMBeanServerRule#getMXBeanProxy(ObjectName, Class)
     */
    protected final <T> T getMXBeanProxy(final ObjectName objectName, final Class<T> interfaceType) {
        return rule.getMXBeanProxy(objectName, interfaceType);
    }
}
