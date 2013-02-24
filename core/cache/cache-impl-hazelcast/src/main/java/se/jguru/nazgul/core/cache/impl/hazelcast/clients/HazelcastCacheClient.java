/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;
import se.jguru.nazgul.core.cache.impl.hazelcast.HazelcastCacheListenerAdapter;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;

import java.util.Set;
import java.util.TreeSet;

/**
 * Hazelcast client implementation, i.e. a HazelcastInstance which is not a member
 * of the cluster. Otherwise provides the same properties as the AbstractHazelcastInstanceWrapper.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheClient extends AbstractHazelcastInstanceWrapper {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheClient.class);

    /**
     * Creates a new HazelcastCacheClient instance using the provided ClientProperties and
     * InternetPort address instance.
     *
     * @param clientConfig The ClientProperties instance to configure the client.
     */
    public HazelcastCacheClient(final ClientConfig clientConfig) {

        super(HazelcastClient.newHazelcastClient(clientConfig));

        // Global instances should already be created, as we require a
        // HazelcastCache instance to execute before a HazelcastCacheClient
        // instance can be launched.
    }

    /**
     * Creates ClientConfig from the provided primitives.
     *
     * @param groupName                   The name of the Hazelcast group.
     * @param groupPassword               The password for the given Hazelcast group.
     * @param initConnectionAttemptsLimit The maximum initial connection attempts.
     * @param reconnectAttemptsLimit      The maximum number of reconnection attempts.
     * @param reconnectionTimeoutInMillis The timeout for each reconnection in milliseconds.
     * @return The fully configured ClientProperties.
     */
    public static ClientConfig getClientConfig(final String groupName,
                                               final String groupPassword,
                                               final int initConnectionAttemptsLimit,
                                               final int reconnectAttemptsLimit,
                                               final int reconnectionTimeoutInMillis) {

        ClientConfig config = new ClientConfig();
        config.setInitialConnectionAttemptLimit(initConnectionAttemptsLimit)
                .setReconnectionAttemptLimit(reconnectAttemptsLimit)
                .setReConnectionTimeOut(reconnectionTimeoutInMillis)
                .setGroupConfig(new GroupConfig(groupName, groupPassword));

        return config;
    }

    /**
     * Creates a ClientConfig from the provided primitives using default
     * settings for other ClientConfig values.
     *
     * @param groupName     The name of the Hazelcast group.
     * @param groupPassword The password for the given Hazelcast group.
     * @return The fully configured ClientProperties.
     */
    public static ClientConfig getClientConfig(final String groupName, final String groupPassword) {

        return getClientConfig(groupName, groupPassword, 5, 5, 5000);
    }

    /**
     * Invoked when a message is received for the added topic. Note that topic guarantees message ordering.
     * Therefore there is only one thread invoking onMessage. The user shouldn't keep the thread busy and preferably
     * dispatch it via an Executor. This will increase the performance of the topic.
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

                    log.debug("(CacheID: " + getClusterId() + "): No local registered listener with id [" + toRemoveId
                            + "] found. Ignoring remove request.");

                    return;
                }

                final String rollbackMessage =
                        "(CacheID: " + getClusterId() + "): Could not remove listener with id [" + toRemoveId
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
                if (!getClusterId().equals(shutdownInstanceID)) {
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
                HazelcastCacheClient.this.stopCache();
                break;

            case CREATE_INCACHE_INSTANCE:
                // Do nothing; this is a HazelcastCacheClient.
                break;
        }
    }
}