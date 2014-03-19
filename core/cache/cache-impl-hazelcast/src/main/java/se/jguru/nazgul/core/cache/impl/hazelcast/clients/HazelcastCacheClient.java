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

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.GroupConfig;
import se.jguru.nazgul.core.cache.impl.hazelcast.AbstractHazelcastInstanceWrapper;

/**
 * Hazelcast client implementation, i.e. a HazelcastInstance which is not a member
 * of the cluster. Otherwise provides the same properties as the AbstractHazelcastInstanceWrapper.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheClient extends AbstractHazelcastInstanceWrapper {

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
}
