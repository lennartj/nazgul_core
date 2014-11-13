/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.Address;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Hazelcast cache implementation, using Strings as CacheKeys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheMember extends AbstractHazelcastInstanceWrapper {

    /**
     * Path to the Main configuration file, used by the Main factory method.
     */
    private static final String MAIN_CONFIGURATION_FILE = "config/cache/hazelcast/MainConfiguration.xml";

    /**
     * Main factory method which creates a new AbstractHazelcastInstanceWrapper instance with
     * Multicast networking switched off, participating within the provided clusterID and connected
     * to the given clusterMembers.
     *
     * @param clusterID      The human-readable ID of the cluster, as provided by the startup
     *                       script. (i.e. "Nazgul_Production", "Dev_002", etc.)
     * @param localIp        The ip where this HazelCast instance listens
     * @param localPort      The port where this HazelCast instance listens.
     * @param clusterMembers A list of all other members within the cluster, on the
     *                       form [ip/dns]:[port],[ip/dns]:[port].
     * @return A newly created AbstractHazelcastInstanceWrapper using the configuration as created from
     * the provided properties. The returned instance uses TCP/IP clustering only
     * (Multicast is switched off).
     * @throws IllegalArgumentException If any argument was null or empty or if the
     *                                  clusterMembers argument was not correctly formatted.
     */
    public static HazelcastCacheMember create(final String clusterID,
                                              final String localIp,
                                              final int localPort,
                                              final String clusterMembers)
            throws IllegalArgumentException {
        return create(clusterID, localIp, localPort, clusterMembers, MAIN_CONFIGURATION_FILE);
    }

    /**
     * Main factory method which creates a new AbstractHazelcastInstanceWrapper instance with
     * Multicast networking switched off, participating within the provided clusterID and connected
     * to the given clusterMembers.
     *
     * @param clusterID         The human-readable ID of the cluster, as provided by the startup
     *                          script. (i.e. "Nazgul_Production", "Dev_002", etc.)
     * @param localIp           The ip where this HazelCast instance listens
     * @param localPort         The port where this HazelCast instance listens.
     * @param clusterMembers    A list of all other members within the cluster, on the
     *                          form [ip/dns]:[port],[ip/dns]:[port].
     * @param configurationFile The resource path to the Hazelcast configuration file.
     * @return A newly created AbstractHazelcastInstanceWrapper using the configuration as created from
     * the provided properties. The returned instance uses TCP/IP clustering only
     * (Multicast is switched off).
     * @throws IllegalArgumentException If any argument was null or empty or if the
     *                                  clusterMembers argument was not correctly formatted.
     */
    public static HazelcastCacheMember create(final String clusterID,
                                              final String localIp,
                                              final int localPort,
                                              final String clusterMembers,
                                              final String configurationFile) {

        Validate.notEmpty(localIp, "Local IP can not be null");

        return create(clusterID, Arrays.asList(localIp), localPort, clusterMembers, configurationFile);
    }

    /**
     * Main factory method which creates a new AbstractHazelcastInstanceWrapper instance with
     * Multicast networking switched off, participating within the provided clusterID and connected
     * to the given clusterMembers.
     *
     * @param clusterID             The human-readable ID of the cluster, as provided by the startup
     *                              script. (i.e. "Nazgul_Production", "Dev_002", etc.)
     * @param localNetworkAddresses The ip where this HazelCast instance listens
     * @param localPort             The port where this HazelCast instance listens.
     * @param clusterMembers        A list of all other members within the cluster, on the
     *                              form [ip/dns]:[port],[ip/dns]:[port].
     * @param configurationFile     The resource path to the Hazelcast configuration file.
     * @return A newly created AbstractHazelcastInstanceWrapper using the configuration as created from
     * the provided properties. The returned instance uses TCP/IP clustering only
     * (Multicast is switched off).
     * @throws IllegalArgumentException If any argument was null or empty or if the
     *                                  clusterMembers argument was not correctly formatted.
     */
    public static HazelcastCacheMember create(final String clusterID,
                                              final List<String> localNetworkAddresses,
                                              final int localPort,
                                              final String clusterMembers,
                                              final String configurationFile)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(clusterID, "Cannot handle null or empty clusterID. Aborting.");
        Validate.notEmpty(localNetworkAddresses, "Local interface list can not be null or empty. Aborting.");
        Validate.notEmpty(clusterMembers, "Cannot handle null or empty clusterID. Aborting.");

        // Parse the clusterMembers definition
        final List<Address> clusterMemberDefinitions = new ArrayList<Address>();
        final StringTokenizer tok = new StringTokenizer(clusterMembers, ",", false);
        while (tok.hasMoreTokens()) {

            final String current = tok.nextToken();

            final StringTokenizer subTok = new StringTokenizer(current, ":", false);
            if (subTok.countTokens() != 2) {
                throw new IllegalArgumentException("Expected clusterMember definition on the form (ip/dns):(port). "
                        + "Found incorrect expression '" + current + "'. Aborting.");
            }

            // Acquire the clusterMember data.
            final String ipOrDNS = subTok.nextToken();
            final int portNumber = parseAndValidatePort(subTok.nextToken());
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(ipOrDNS, portNumber);
            clusterMemberDefinitions.add(new Address(inetSocketAddress));
        }

        // Get the proto-configuration.
        final Config config = HazelcastCacheMember.readConfigFile(configurationFile);

        // Configure local interfaces
        final InterfacesConfig interfaces = config.getNetworkConfig().getInterfaces();
        interfaces.setInterfaces(localNetworkAddresses);

        // Configure local port
        config.getNetworkConfig().setPortAutoIncrement(false);
        config.getNetworkConfig().setPort(parseAndValidatePort("" + localPort));

        // Update with the seed data
        config.getGroupConfig().setName(clusterID);
        config.getGroupConfig().setPassword("giveMeAccess2_" + clusterID);

        // Remake the NetworkConfig altogether.
        final JoinConfig activeJoin = config.getNetworkConfig().getJoin();
        final TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);

        // Add the clusterMembers.
        for (final Address current : clusterMemberDefinitions) {
            tcpIpConfig.addMember(current.getHost() + ":" + current.getPort());
        }

        // Update the active Join, and carry on.
        activeJoin.setTcpIpConfig(tcpIpConfig);

        // Disable multicast; use proper TCP/IP clustering.
        final MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        activeJoin.setMulticastConfig(multicastConfig);

        // All done.
        return new HazelcastCacheMember(config);
    }

    /**
     * Creates a new AbstractHazelcastInstanceWrapper node member from the provided configuration.
     *
     * @param cacheConfig The configuration of the Hazelcast cache.
     */
    public HazelcastCacheMember(final Config cacheConfig) {

        super(Hazelcast.newHazelcastInstance(cacheConfig));
    }

    /**
     * Creates a HazelcastConfiguration Config by reading and parsing the provided configFileResource.
     *
     * @param configFileResource A classpath-relative configuration file for Hazelcast.
     * @return A Hazelcast Config instance.
     */
    public static Config readConfigFile(final String configFileResource) {

        if (configFileResource == null) {
            throw new IllegalArgumentException("Cannot handle null configFileResource.");
        }

        final ClassLoader[] hierarchy =
                new ClassLoader[]{Thread.currentThread().getContextClassLoader(),
                        HazelcastCacheMember.class.getClassLoader()};

        InputStream in = null;
        for (final ClassLoader current : hierarchy) {
            if (in == null) {
                in = current.getResourceAsStream(configFileResource);
            }
        }

        if (in == null) {
            throw new IllegalArgumentException("Could not load [" + configFileResource + "] from classpath.");
        }

        // Construct the Config and return it
        return new XmlConfigBuilder(in).build();
    }

    //
    // Private helpers
    //

    /**
     * Parses and validates a configured port.
     *
     * @param potentialPort The potential port.
     * @return The parsed and validated port number.
     */
    private static int parseAndValidatePort(final String potentialPort) {

        final int toReturn = Integer.parseInt(potentialPort);

        final int maxPortNumber = 65536;
        if (toReturn < 0 || toReturn > maxPortNumber) {
            throw new IllegalArgumentException("Encountered illegal port number [" + toReturn
                    + "]. Permitted range is [0, " + maxPortNumber + "]. Aborting.");
        }

        return toReturn;
    }
}
