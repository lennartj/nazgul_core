/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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
package se.jguru.nazgul.core.cache.impl.hazelcast.clients;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;

/**
 * Hazelcast client implementation, i.e. a HazelcastInstance which is not a member
 * of the cluster. Otherwise provides the same properties as the AbstractHazelcastInstanceWrapper.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheClient extends AbstractHazelcastInstanceWrapper {

    /**
     * The number of milliseconds before this HazelcastCacheClient considers a connection to be a timed out.
     */
    public static final int DEFAULT_RECONNECTION_TIMEOUT = 5000;

    /**
     * The number of milliseconds after a timeout before this HazelcastCacheClient will attempt another connection.
     */
    public static final int DEFAULT_CONNECTION_ATTEMPT_PERIOD = 3000;

    /**
     * The amount of connections which will be attempted initially.
     */
    public static final int DEFAULT_INITIAL_CONNECTION_ATTEMPTS = 5;

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
     * @param groupName              The name of the Hazelcast group to which this HazelcastCacheClient
     *                               should belong.
     * @param groupPassword          The password for the given Hazelcast group.
     * @param connectionAttemptLimit The number of attempts made by this HazelcastCacheClient to connect to
     *                               its cluster instance nodes before giving up. A value of zero implies no
     *                               limit on the number of attempts to connect (i.e. attempt indefinitely).
     * @param connectionTimeout      The number of milliseconds for nodes to accept a connection from this
     *                               HazelcastCacheClient before indicating timeout. A value of zero implies no
     *                               limit on the connection wait (i.e. wait until the connection is established or
     *                               an exception is raised).
     * @param reconnectInterval      If the HazelcastCacheClient fails to connect to its cluster instance,
     *                               it should wait this many milliseconds before attempting another connection.
     * @return The fully configured ClientConfig.
     * @see com.hazelcast.client.config.ClientConfig
     */
    public static ClientConfig getClientConfig(final String groupName,
                                               final String groupPassword,
                                               final int connectionAttemptLimit,
                                               final int connectionTimeout,
                                               final int reconnectInterval) {

        // Check sanity
        Validate.notNull(groupName, "Cannot handle null groupName argument.");
        Validate.notNull(groupPassword, "Cannot handle null groupPassword argument.");

        final ClientConfig toReturn = new ClientConfig()
                .setGroupConfig(new GroupConfig(groupName, groupPassword));

        // Configure the Network setting sin the ClientConfig.
        toReturn.getNetworkConfig()
                .setConnectionAttemptLimit(connectionAttemptLimit)
                .setConnectionTimeout(connectionTimeout)
                .setConnectionAttemptPeriod(reconnectInterval);

        /*
        // Hazelcast version 2.x configuration settings.

        ClientConfig config = new ClientConfig();
        config.setInitialConnectionAttemptLimit(initConnectionAttemptsLimit)
                .setReconnectionAttemptLimit(reconnectAttemptsLimit)
                .setReConnectionTimeOut(reconnectionTimeoutInMillis)
        */

        // All done.
        return toReturn;
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
        return getClientConfig(groupName,
                groupPassword,
                DEFAULT_INITIAL_CONNECTION_ATTEMPTS,
                DEFAULT_CONNECTION_ATTEMPT_PERIOD,
                DEFAULT_RECONNECTION_TIMEOUT);
    }
}
