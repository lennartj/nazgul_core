/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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
package se.jguru.nazgul.core.algorithms.api.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.management.JMX;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"Unused", "WeakerAccess"})
public final class JmxAlgorithms {

    /**
     * Our logger.
     */
    public static final Logger log = LoggerFactory.getLogger(JmxAlgorithms.class);

    /**
     * The standard JMX property for the Interface type.
     */
    public static final String JMX_INTERFACE_TYPE = "jmxInterfaceType";

    /**
     * The descriptor field name of the MXBean class name for an {@link ObjectName}
     */
    public static final String JMX_INTERFACE_TYPENAME = "interfaceClassName";

    /**
     * Retrieves the platform MBeanServer.
     *
     * @return The Platform MBeanServer.
     */
    public static MBeanServer getPlatformServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Synthesizes a JMX ObjectName for the given MXBean interface.
     * Also indicates which system it is
     *
     * @param interfaceType The public interface type of the MXBean for which to retrieve an ObjectName.
     * @return The ObjectName synthesized from the supplied InterfaceType.
     */
    public static <T> ObjectName getNaturalObjectNameFor(
            final Class<T> interfaceType) {
        return getNaturalObjectNameFor(interfaceType, null);
    }

    /**
     * Synthesizes a JMX ObjectName for the given MXBean interface.
     * Also indicates which system it is
     *
     * @param interfaceType The public interface type of the MXBean for which to retrieve an ObjectName.
     * @param properties    An optional (i.e. nullable) Map containing JMX properties for the ObjectName.
     * @param <T>           The type of the MXBean interface.
     * @return The ObjectName synthesized from the supplied InterfaceType.
     */
    public static <T> ObjectName getNaturalObjectNameFor(
            final Class<T> interfaceType,
            final Map<String, String> properties) {

        // Convert the inbound properties, since JMX requires a Hashtable.
        //
        final Hashtable<String, String> props = properties == null ? new Hashtable<>() : new Hashtable<>(properties);
        props.put(JMX_INTERFACE_TYPE, interfaceType.getSimpleName());

        try {
            return new ObjectName(interfaceType.getPackage().getName(), props);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Could not create JMX ObjectName", e);
        }
    }

    /**
     * Registers an MXBean using the supplied JMX public type (i.e. the MXBean interface) and
     * the supplied implementation (object).
     *
     * @param mxBeanType           The public interface type of the MXBean to register.
     * @param mxBeanImplementation The actual JMX object implementation.
     * @return The JMX ObjectInstance to register.
     */
    public static <T, I extends T> ObjectInstance registerMXBean(
            final Class<T> mxBeanType,
            final I mxBeanImplementation) {
        return registerMXBean(mxBeanType, mxBeanImplementation, null);
    }

    /**
     * Registers an MXBean using the supplied JMX public type (i.e. the MXBean interface) and
     * the supplied implementation (object).
     *
     * @param mxBeanType           The public interface type of the MXBean to register.
     * @param mxBeanImplementation The actual JMX object implementation.
     * @param jmxAttributes        An optional (i.e. nullable) Map containing ObjectName properties for use in
     *                             binding the MXBean within the JMX Platform server.
     * @param <T>                  The MXBean interface type, which must pass the `JMX.isMXBeanInterface(mxBeanType)`
     *                             method to be compliant.
     * @param <I>                  The MXBean object to register, which must implement the supplied JMX type.
     * @return The JMX ObjectInstance to register.
     */
    public static <T, I extends T> ObjectInstance registerMXBean(
            final Class<T> mxBeanType,
            final I mxBeanImplementation,
            final Map<String, String> jmxAttributes) {

        // Check sanity
        if (!JMX.isMXBeanInterface(mxBeanType)) {
            throw new IllegalArgumentException("Class [" + mxBeanType.getName() + "] was not an MXBean interface.");
        }

        try {

            // Register the bean using its "natural" JMX ObjectName
            final ObjectInstance toReturn = getPlatformServer().registerMBean(
                    mxBeanImplementation,
                    getNaturalObjectNameFor(mxBeanType, jmxAttributes));

            if (log.isDebugEnabled()) {
                log.debug("Registered JMX ObjectInstance '" + toReturn.toString() + "'");
            }

            // All Done.
            return toReturn;

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not register MXBean. ", e);
        }
    }

    /**
     * Retrieves an MXBean Proxy for the supplied interfaceType within the local/platform MBean server.
     *
     * @param interfaceType The type of interface
     * @param objectName    The object name to use to retrieve the MXBean from the MBeanServer.
     * @param <T>           The type of interface for the MXBean.
     * @return The T object retrieved from JMX.
     */
    public static <T> T getMXBeanProxy(final Class<T> interfaceType, final ObjectName objectName) {

        // Check sanity
        Validate.notNull(objectName, "objectName");
        Validate.notNull(interfaceType, "interfaceType");

        return JMX.newMXBeanProxy(
                getPlatformServer(),
                objectName,
                interfaceType, true);
    }

    /**
     * Retrieves a Set containing the [ObjectInstance]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectInstance]s.
     * @return A set of [ObjectInstance]s for the objects within the supplied domain.
     */
    public static Set<ObjectInstance> getMBeansInDomain(final String jmxDomain) {
        return getMBeansInDomain(jmxDomain, null);
    }

    /**
     * Retrieves a Set containing the [ObjectInstance]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectInstance]s.
     * @param queryExp  The JMX query expression - or null to retrieve all MBeans within the jmxDomain.
     * @return A set of [ObjectInstance]s for the objects within the supplied domain.
     */
    public static Set<ObjectInstance> getMBeansInDomain(final String jmxDomain, final QueryExp queryExp) {
        return getPlatformServer().queryMBeans(getSearchObjectNameFor(jmxDomain), queryExp);
    }

    /**
     * Retrieves a Set containing the [ObjectName]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectName]s.
     * @return A Set of [ObjectName]s for the objects within the supplied domain.
     */
    public static Set<ObjectName> getNamesInDomain(final String jmxDomain) {
        return getNamesInDomain(jmxDomain, null);
    }

    /**
     * Retrieves a Set containing the [ObjectName]s within the supplied jmxDomain.
     *
     * @param jmxDomain The JMX domain for which we should retrieve [ObjectName]s.
     * @return A Set of [ObjectName]s for the objects within the supplied domain.
     */
    public static Set<ObjectName> getNamesInDomain(final String jmxDomain, final QueryExp queryExp) {
        return getPlatformServer().queryNames(getSearchObjectNameFor(jmxDomain), queryExp);
    }

    /**
     * Fetches the MXBean (or MBean) interface which is the public API of the supplied [ObjectName]
     */
    public static String getMBeanInterfaceName(final ObjectName objectName) {

        try {
            final MBeanInfo info = JmxAlgorithms.getPlatformServer().getMBeanInfo(objectName);
            return "" + info.getDescriptor().getFieldValue(JMX_INTERFACE_TYPENAME);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire MBeanInterfaceName", e);
        }
    }

    //
    // Private helpers
    //

    private static ObjectName getSearchObjectNameFor(final String jmxDomain) {

        try {
            return new ObjectName(jmxDomain + ":*");
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException("Could not create ObjectName for jmxDomain '" + jmxDomain + "'", ex);
        }
    }
}
