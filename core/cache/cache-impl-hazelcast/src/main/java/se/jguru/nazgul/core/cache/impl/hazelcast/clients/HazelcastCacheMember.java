/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.config.Config;
import com.hazelcast.config.Interfaces;
import com.hazelcast.config.Join;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Message;
import com.hazelcast.nio.Address;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;
import se.jguru.nazgul.core.cache.impl.hazelcast.HazelcastCacheListenerAdapter;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Hazelcast cache implementation, using Strings as CacheKeys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheMember extends AbstractHazelcastInstanceWrapper {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheMember.class);

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
     *         the provided properties. The returned instance uses TCP/IP clustering only
     *         (Multicast is switched off).
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
     *         the provided properties. The returned instance uses TCP/IP clustering only
     *         (Multicast is switched off).
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
     * @param clusterID         The human-readable ID of the cluster, as provided by the startup
     *                          script. (i.e. "Nazgul_Production", "Dev_002", etc.)
     * @param localInterfaces   The ip where this HazelCast instance listens
     * @param localPort         The port where this HazelCast instance listens.
     * @param clusterMembers    A list of all other members within the cluster, on the
     *                          form [ip/dns]:[port],[ip/dns]:[port].
     * @param configurationFile The resource path to the Hazelcast configuration file.
     * @return A newly created AbstractHazelcastInstanceWrapper using the configuration as created from
     *         the provided properties. The returned instance uses TCP/IP clustering only
     *         (Multicast is switched off).
     * @throws IllegalArgumentException If any argument was null or empty or if the
     *                                  clusterMembers argument was not correctly formatted.
     */
    public static HazelcastCacheMember create(final String clusterID,
                                        final List<String> localInterfaces,
                                        final int localPort,
                                        final String clusterMembers,
                                        final String configurationFile)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(clusterID, "Cannot handle null or empty clusterID. Aborting.");
        Validate.notEmpty(localInterfaces, "Local interface list can not be null or empty. Aborting.");
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

            try {
                clusterMemberDefinitions.add(new Address(ipOrDNS, portNumber));
            } catch (final UnknownHostException e) {
                throw new IllegalArgumentException(
                        "Address " + ipOrDNS + " (" + portNumber + ") could not be resolved", e);
            }
        }

        // Get the proto-configuration.
        final Config config = HazelcastCacheMember.readConfigFile(configurationFile);

        // Configure local interfaces
        final Interfaces interfaces = config.getNetworkConfig().getInterfaces();
        interfaces.setInterfaces(localInterfaces);

        // Configure local port
        config.getNetworkConfig().setPortAutoIncrement(false);
        config.getNetworkConfig().setPort(parseAndValidatePort("" + localPort));

        // Update with the seed data
        config.getGroupConfig().setName(clusterID);
        config.getGroupConfig().setPassword("giveMeAccess2_" + clusterID);

        // Remake the NetworkConfig altogether.
        final Join activeJoin = config.getNetworkConfig().getJoin();
        final TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);

        // Add the clusterMembers.
        for (final Address current : clusterMemberDefinitions) {
            tcpIpConfig.addAddress(current);
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

    /**
     * Invoked when a message is received for the added topic.
     *
     * @param adminMessageMessage received message
     */
    @Override
    public void onMessage(final Message<AdminMessage> adminMessageMessage) {

        final AdminMessage message = adminMessageMessage.getMessageObject();

        switch (message.getCommand()) {
            case REMOVE_LISTENER:

                final String distributedObjectiD = message.getArguments().get(0);
                final String toRemoveId = message.getArguments().get(1);

                // Take no action if we do not own the listener.
                // Only the member that owns the listener should remove it.
                if (!getLocallyRegisteredListeners().containsKey(toRemoveId)) {

                    log.debug("(CacheID: " + getId() + "): No local registered listener with id [" + toRemoveId
                            + "] found. Ignoring remove request.");

                    return;
                }

                final String rollbackMessage =
                        "(CacheID: " + getId() + "): Could not remove listener with id [" + toRemoveId
                                + "] from distributedObject [" + distributedObjectiD + "]";

                performTransactedAction(new AbstractTransactedAction(rollbackMessage) {

                    @Override
                    public void doInTransaction() throws RuntimeException {

                        Instance distributedObject = null;

                        // Find the distributed object from which to remove the listener
                        for (final Instance current : getInstances()) {
                            if (distributedObjectiD.equals("" + current.getId())) {
                                distributedObject = current;
                                break;
                            }
                        }

                        // Remove the listener locally.
                        final HazelcastCacheListenerAdapter removed
                                = getLocallyRegisteredListeners().remove(toRemoveId);

                        // Remove the listener ID from the listenersIdMap,
                        // and update the TreeSet within the distributedListenersIdMap.
                        final TreeSet<String> idSet = getCacheListenersIDMap().get(distributedObjectiD);
                        final boolean removedOKFromIdMap = idSet.remove(toRemoveId);
                        getCacheListenersIDMap().put(distributedObjectiD, idSet);

                        // Remove the listener from the distributedObject.
                        switch (distributedObject.getInstanceType()) {
                            case MAP:
                                ((IMap) distributedObject).removeEntryListener(removed);
                                break;

                            case LIST:
                            case SET:
                            case QUEUE:
                                ((ICollection) distributedObject).removeItemListener(removed);
                                break;
                        }
                    }
                });

                break;

            case SHUTDOWN_INSTANCE:

                // Is it *this* instance that should be shut down?
                final String shutdownInstanceID = message.getArguments().get(0);
                if (!getId().equals(shutdownInstanceID)) {
                    return;
                }

                // Unregister all keys for listeners that we own.
                for (final Instance current : getInstances()) {

                    final Set<String> listenerIDs = getListenerIDsFor(current);
                    for (final String currentID : getLocallyRegisteredListeners().keySet()) {
                        if (listenerIDs.contains(currentID)) {
                            listenerIDs.remove(currentID);
                        }
                    }
                }

                // Now perform shutdown.
                // This will automatically remove all local listeners from their instances.
                HazelcastCacheMember.this.stopCache();
                break;

            case CREATE_INCACHE_INSTANCE:
                final AdminMessage.TypeDefinition toCreateType =
                        AdminMessage.TypeDefinition.valueOf(message.getArguments().get(0));
                final String clusterUniqueID = message.getArguments().get(1);

                switch (toCreateType) {

                    case SET:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.SET, clusterUniqueID);
                        break;

                    case COLLECTION:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.COLLECTION,
                                clusterUniqueID);
                        break;

                    case QUEUE:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.QUEUE, clusterUniqueID);
                        break;

                    case TOPIC:
                        getTopic(clusterUniqueID);
                        break;

                    case MAP:
                        getDistributedMap(clusterUniqueID);
                        break;
                }
                break;

            default:
                throw new UnsupportedOperationException("AdminMessage command [" + message.getCommand()
                        + "] not yet supported.");
        }
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
