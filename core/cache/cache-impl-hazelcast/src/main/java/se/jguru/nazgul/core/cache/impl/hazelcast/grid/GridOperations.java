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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.cache.impl.hazelcast.grid;

import com.hazelcast.core.IMap;
import com.hazelcast.core.Instance;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Specification and constants for various grid-related operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface GridOperations {

    /**
     * The clusterwide Map[String, List[String]] relating ID of all distributed objects to a
     * List holding IDs of its listeners.
     */
    String CLUSTER_KNOWN_LISTENERIDS = "hazelcast_cluster_distributedObjectID2cacheListenerIDsMap";

    /**
     * The id of the clusterwide shared cache map.
     */
    String CLUSTER_SHARED_CACHE_MAP = "hazelcast_cluster_commonCacheMap";

    /**
     * The id of the clusterwide topic which transmits AdminMessage instances which - among other things - handles
     * removal of named listeners.
     */
    String CLUSTER_ADMIN_TOPIC = "hazelcast_cluster_adminTopic";

    /**
     * Sends the provided AdminMessage to all members of the Cluster.
     *
     * @param message The message to send.
     */
    void sendAdminMessage(final AdminMessage message);

    /**
     * @return The shared Map holding the default (direct-level) cached instances.
     */
    IMap<String, Serializable> getSharedMap();

    /**
     * @return The shared Map relating IDs for distributed objects [key] to
     *         a Set of String IDs for all registered listeners to the
     *         given distributed object [value].
     */
    IMap<String, TreeSet<String>> getCacheListenersIDMap();

    /**
     * Adds the given listenerID to the listenerIdSet for the provided distributedObject.
     *
     * @param distributedObject The instance for which a listener ID should be registered.
     * @param listenerId        The id of the Listener to register to the provided distributedObject.
     * @return <code>true</code> if the registration was successful, and false otherwise.
     */
    boolean addListenerIdFor(final Instance distributedObject, final String listenerId);

    /**
     * Validates that the provided distributedObject is a Hazelcast Instance.
     *
     * @param distributedObject The object to validate.
     * @return The distributedObject, type cast to a Hazelcast Instance.
     * @throws IllegalArgumentException if the distributedObject was not a Hazelcast Instance.
     */
    Instance cast(final Object distributedObject) throws IllegalArgumentException;
}
