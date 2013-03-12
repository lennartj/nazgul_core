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
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.TreeMap;

/**
 * Convenience bean to wrap required [system] properties for launching the Cache.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PropertyHolder {

    /**
     * The property defining the id of the cluster to which this cache belongs.
     * Typical use in an appserver startup file:
     * <code>JAVA_OPTS="$JAVA_OPTS -Dcache.hazelcast.cluster.id=nazgul_production</code>
     */
    public static final String CACHE_CLUSTER_KEY = "cache.hazelcast.cluster.id";

    /**
     * The property defining the ip of the member.
     * Typical use in an appserver startup file:
     * <code>JAVA_OPTS="$JAVA_OPTS -Dcache..hazelcast.cluster.member.ip=10.0.0.3</code>
     */
    public static final String CACHE_IP_KEY = "cache.hazelcast.cluster.member.ip";

    /**
     * The startup property defining the listening port for this cache.
     * Typical use in an appserver startup file:
     * <code>JAVA_OPTS="$JAVA_OPTS -Dcache.cluster..hazelcast.member.port=5701</code>
     */
    public static final String CACHE_PORT_KEY = "cache.hazelcast.cluster.member.port";

    /**
     * The startup property defining a comma separated list of cache cluster members, each defined with a
     * <code>[DNS or IP]:[port]</code> structure.
     * Typical use in an appserver startup file:
     * <code>JAVA_OPTS="$JAVA_OPTS -Dcache.cluster.members=127.0.0.1:5710,10.0.0.2:5701</code>
     */
    public static final String CACHE_MEMBERS_KEY = "cache.cluster.members";

    // Internal state
    private Map<String, String> properties;

    /**
     * Creates a new PropertyHolder using the provided properties instance as a source of configuration.
     *
     * @param properties the properties Map holding the cluster properties.
     * @throws IllegalArgumentException if the provided properties Map does not hold
     *                                  all the required cluster properties.
     */
    public PropertyHolder(final Map<String, String> properties) throws IllegalArgumentException {

        validateRequiredProperties(properties);
        this.properties = properties;
    }

    /**
     * Creates a new PropertyHolder, reading all properties from the <code>System.getProperties()</code>.
     *
     * @throws IllegalArgumentException if the System.getProperties() does not hold
     *                                  all the required cluster properties. (This implies an incorrect configuration).
     */
    public PropertyHolder() throws IllegalArgumentException {

        TreeMap<String, String> sysPropCopy = new TreeMap<String, String>();
        for (Object current : System.getProperties().keySet()) {

            final String currentKey = "" + current;
            sysPropCopy.put(currentKey, System.getProperty(currentKey));
        }

        validateRequiredProperties(sysPropCopy);
        this.properties = sysPropCopy;
    }

    /**
     * @return The listening ip for this cache.
     */
    public String getLocalListeningIp() {
        return properties.get(CACHE_IP_KEY);
    }

    /**
     * @return The local listening port for this cache.
     */
    public Integer getLocalListeningPort() {
        return Integer.valueOf(properties.get(CACHE_PORT_KEY));
    }

    /**
     * @return The id of the cache.
     */
    public String getCacheClusterId() {
        return properties.get(CACHE_CLUSTER_KEY);
    }

    /**
     * @return The un-parsed list of cache members.
     */
    public String getCacheMembers() {
        return properties.get(CACHE_MEMBERS_KEY);
    }

    //
    // Private helpers
    //

    @SuppressWarnings("PMD.PreserveStackTrace")
    private void validateRequiredProperties(final Map<String, String> properties) {

        Validate.notNull(properties, "Cannot handle null properties Map.");
        Validate.notEmpty(properties.get(CACHE_CLUSTER_KEY), getPropertyErrorMessage(CACHE_CLUSTER_KEY));
        Validate.notEmpty(properties.get(CACHE_MEMBERS_KEY), getPropertyErrorMessage(CACHE_MEMBERS_KEY));
        Validate.notEmpty(properties.get(CACHE_IP_KEY), getPropertyErrorMessage(CACHE_IP_KEY));
        Validate.notEmpty(properties.get(CACHE_PORT_KEY), getPropertyErrorMessage(CACHE_PORT_KEY));

        try {
            Integer.valueOf(properties.get(CACHE_PORT_KEY));
        } catch (Exception e) {
            throw new IllegalArgumentException("Property [" + CACHE_PORT_KEY + "] must be a positive integer.");
        }
    }

    private String getPropertyErrorMessage(final String key) {

        return "Property [" + key + "] cannot be null or empty. "
                + "This implies a configuration error [most likely in run.conf].";
    }
}
