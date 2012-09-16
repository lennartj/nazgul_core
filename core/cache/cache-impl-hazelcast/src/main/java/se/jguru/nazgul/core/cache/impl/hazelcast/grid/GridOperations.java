/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
    public static final String CLUSTER_KNOWN_LISTENERIDS = "hazelcast_cluster_distributedObjectID2cacheListenerIDsMap";

    /**
     * The id of the clusterwide shared cache map.
     */
    public static final String CLUSTER_SHARED_CACHE_MAP = "hazelcast_cluster_commonCacheMap";

    /**
     * The id of the clusterwide topic which transmits AdminMessage instances which - among other things - handles
     * removal of named listeners.
     */
    public static final String CLUSTER_ADMIN_TOPIC = "hazelcast_cluster_adminTopic";

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
    // was: IMap<String, TreeSet<String>> getListenersIdMap();

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
