/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api.distributed;

import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.api.CacheListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service interface definition for a distributed (clustered) cache.
 *
 * @param <KeyType> The type of keys used within this DistributedCache.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DistributedCache<KeyType extends Serializable> extends Cache<KeyType> {

    /**
     * Type indication for the distributed collection.
     */
    public enum DistributedCollectionType {

        /**
         * Indicate a distributed Collection.
         * The Collection may be ordered (i.e. like a List) if the
         * underlying implementation provide such capabilities.
         */
        COLLECTION,

        /**
         * Indicate a distributed Set (i.e. a collection with unique values).
         * The Set may ordered (i.e. like a TreeSet) if the
         * underlying implementation provide such capabilities.
         */
        SET,

        /**
         * Indicate a distributed Queue. Since Queues imply ordering, the
         * performance of distributed Queues are (much) inferior to that
         * of Collections or Sets. Use Queues only when required to.
         */
        QUEUE
    }

    /**
     * @return An identifier unique to the active cache cluster, but different
     *         for each time this method is called. (I.e. a cluster-wide sequence
     *         of some kind).
     */
    String getClusterUniqueID();

    /**
     * Gets a distributed collection with the given type and provided
     * key from the cache. Note that the distributed Collection will be
     * created on the provided key if it does not already exist.
     * <p/>
     * A typical usage example would be
     * <p/>
     * <pre>
     *  Collection cachedList = getDistributedCollection(COLLECTION, "someKey");
     * </pre>
     *
     * @param type The type of Collection desired. This should be an abstract type,
     *             such as <code>Collection, Set or Queue</code> etc.
     * @param key  The key where the distributed collection resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty) distributed
     *         collection cached at the given key.
     * @throws ClassCastException if the cache key [key] maps to an object not of the given type.
     * @throws UnsupportedDistributionException
     *                            if the underlying cache implementation could
     *                            not support creating a distributed collection
     *                            of the given type.
     */
    Collection<? extends Serializable> getDistributedCollection(DistributedCollectionType type, String key)
            throws ClassCastException, UnsupportedDistributionException;

    /**
     * Gets a distributed Map with the provided key from the cache.
     * Note that the distributed Map will be created on the provided key
     * if it does not already exist.
     *
     * @param <K> The key type used within the distributed Map.
     * @param <V> The value type used within the distributed Map.
     * @param key The key where the distributed Map resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty)
     *         distributed Map cached at the given key.
     * @throws UnsupportedDistributionException
     *          if the underlying cache implementation could
     *          not support creating a distributed Map.
     */
    <K extends Serializable, V extends Serializable> Map<K, V> getDistributedMap(String key)
            throws UnsupportedDistributionException;

    /**
     * Adds a CacheListener to the distributed object. The cacheListener will
     * be invoked when the properties/items/key-value pairs of the distributed
     * object are altered.
     * <p/>
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should
     *                          listen to changes.
     * @param listener          The CacheListener to register to the given distributed object.
     * @return <code>true</code> if the CacheListener was successfully registered,
     *         and <code>false</code> otherwise.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  registering a CacheListener (i.e. incorrect type
     *                                  for the underlying cache implementation).
     */
    boolean addListenerFor(Object distributedObject, CacheListener listener)
            throws IllegalArgumentException;

    /**
     * Removes the given CacheListener from the distributed object.
     * <p/>
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should remove the CacheListener.
     * @param cacheListenerId   The ID of the CacheListener to remove from the given distributed object.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  removing a CacheListener (i.e. incorrect type for the
     *                                  underlying cache implementation).
     */
    void removeListenerFor(Object distributedObject, String cacheListenerId)
            throws IllegalArgumentException;

    /**
     * Retrieves all IDs of the CacheListeners bound to the given distributedObject.
     *
     * @param distributedObject The distributedObject whose CacheListener IDs we should retrieve.
     * @return The IDs of all CacheListener instances registered to the provided distributedObject.
     */
    List<String> getListenersIDsFor(Object distributedObject);
}
