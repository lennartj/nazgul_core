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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalMBeanServerRuleTest extends AbstractJmxTest {

    @Before
    public void onSetup() {
        Assert.assertNotNull(rule);
    }

    @After
    public void onTeardown() {
        Assert.assertNotNull(rule);
    }

    @Test
    public void validateAcquiringDomainsFromServer() {

        // Assemble
        final String thisClassName = getClass().getName();

        // Act
        final String testDomain = getTestDomain();
        final String defaultDomain = rule.getMBeanServer().getDefaultDomain();
        final SortedSet<String> domains = rule.getDomains();

        // Assert
        Assert.assertEquals(thisClassName, testDomain);
        Assert.assertNotNull(domains);
        Assert.assertNotNull(defaultDomain);
    }

    @Test
    public void validateRetrievingJmxNamesAndInstances() throws Exception {

        // Assemble
        final String langDomain = "java.lang";
        final ObjectName classLoaderObjectName = new ObjectName("java.lang:type=ClassLoading");
        final String classLoaderAttributeName = "LoadedClassCount";

        // Act
        final Set<ObjectName> javaLangNames = getNamesInDomain(langDomain);
        final Set<ObjectInstance> javaLangBeans = getMBeansInDomain(langDomain);

        final ObjectInstance classLoaderObject = rule.getMBeanServer().getObjectInstance(classLoaderObjectName);
        final Object loadedClassCountAttribute = getAttribute(classLoaderObjectName, classLoaderAttributeName);

        // Assert
        Assert.assertNotNull(javaLangNames);
        Assert.assertNotNull(javaLangBeans);
        Assert.assertNotNull(classLoaderObject);
        Assert.assertNotNull(loadedClassCountAttribute);
        Assert.assertTrue(((Integer) loadedClassCountAttribute) > 0);
    }

    @Test
    public void validateExceptionOnExecutingMethodInProxyForNonexistentObjectName() throws Exception {

        // Assemble
        final ObjectName nonexistentObject = new ObjectName(
                "non.existent.domain",
                "someNonExistentKey",
                "someNonExistentValue");

        // Act & Assert
        final Runnable mxBeanProxy = getMXBeanProxy(nonexistentObject, Runnable.class);

        try {
            mxBeanProxy.run();
            Assert.fail("Nonexistent JMX Object proxies should throw UndeclaredThrowableExceptions when invoked.");

        } catch (UndeclaredThrowableException e) {
            Assert.assertTrue(e.getCause() instanceof InstanceNotFoundException);
        } catch (Exception e) {
            Assert.fail("Expected UndeclaredThrowableException, but got [" + e.getClass().getName() + "]");
        }
    }

    @Test
    public void validateGettingAttributesFromJmxObjects() throws Exception {

        // Assemble
        final MBeanServer srv = rule.getMBeanServer();

        // Act
        final ObjectInstance objectInstance = getFirstReadableAttributeInfoObject(srv);
        final ObjectName objectName = objectInstance.getObjectName();
        final MBeanAttributeInfo[] attrInfo = srv.getMBeanInfo(objectName).getAttributes();
        final Object attribute = getAttribute(objectInstance.getObjectName(), attrInfo[0].getName());

        // Assert
        Assert.assertNotNull(attribute);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnIncorrectNullJmxDomain() {

        // Act & Assert
        rule.getMBeansInDomain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnMalformedJmxDomain() {

        // Act & Assert
        rule.getMBeansInDomain("foo:*");
    }

    //
    // Private helpers
    //

    private SortedMap<String, MBeanAttributeInfo> getAttributeInfoFor(final ObjectName anObjectName) {

        final SortedMap<String, MBeanAttributeInfo> toReturn = new TreeMap<>();

        try {
            MBeanInfo mBeanInfo = rule.getMBeanServer().getMBeanInfo(anObjectName);
            if (mBeanInfo != null) {
                for (MBeanAttributeInfo current : mBeanInfo.getAttributes()) {

                    final String key = current.getName() + "_" + current.getType();
                    toReturn.put(key, current);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // All done.
        return toReturn;
    }

    private ObjectInstance getFirstReadableAttributeInfoObject(final MBeanServer srv) {

        try {
            for (ObjectInstance current : srv.queryMBeans(null, null)) {
                final MBeanAttributeInfo[] attributes = srv.getMBeanInfo(current.getObjectName()).getAttributes();
                if (attributes != null && attributes.length > 0 && attributes[0].isReadable()) {
                    return current;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not acquire attribute info", e);
        }

        // Nothing found.
        return null;
    }
}
