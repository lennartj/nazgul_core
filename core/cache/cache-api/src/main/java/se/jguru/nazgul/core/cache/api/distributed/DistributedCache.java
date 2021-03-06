/*-
 * #%L
 * Nazgul Project: nazgul-core-cache-api
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

package se.jguru.nazgul.core.cache.api.distributed;

import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.api.CacheListener;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service interface definition for a distributed (clustered) cache.
 *
 * @param <K> The type of key used within this DistributedCache.
 * @param <V> The type of value used within this DistributedCache.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DistributedCache<K, V> extends Cache<K, V> {

    /**
     * Type indication for the distributed collection.
     */
    enum DistributedCollectionType {

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
     * for each time this method is called. (I.e. a cluster-wide sequence
     * of some kind).
     */
    @NotNull
    String getClusterUniqueID();

    /**
     * Gets a distributed collection with the given type and provided
     * key from the cache. Note that the distributed Collection will be
     * created on the provided key if it does not already exist.
     * A typical usage example would be
     * <pre>
     *  Collection cachedList = getDistributedCollection(COLLECTION, "someKey");
     * </pre>
     *
     * @param type The type of Collection desired. This should be an abstract type,
     *             such as <code>Collection, Set or Queue</code> etc.
     * @param key  The key where the distributed collection resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty) distributed
     * collection cached at the given key.
     * @throws ClassCastException               if the cache key [key] maps to an object not of the given type.
     * @throws UnsupportedDistributionException if the underlying cache implementation could
     *                                          not support creating a distributed collection
     *                                          of the given type.
     */
    Collection<? extends V> getDistributedCollection(@NotNull DistributedCollectionType type,
                                                     @NotNull String key)
            throws ClassCastException, UnsupportedDistributionException;

    /**
     * Gets a distributed Map with the provided key from the cache.
     * Note that the distributed Map will be created on the provided key
     * if it does not already exist.
     *
     * @param <MapKeyType>   The key type used within the distributed Map.
     * @param <MapValueType> The value type used within the distributed Map.
     * @param key            The key where the distributed Map resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty)
     * distributed Map cached at the given key.
     * @throws UnsupportedDistributionException if the underlying cache implementation could
     *                                          not support creating a distributed Map.
     */
    <MapKeyType, MapValueType> Map<MapKeyType, MapValueType> getDistributedMap(@NotNull String key)
            throws UnsupportedDistributionException;

    /**
     * Adds a CacheListener to the distributed object. The cacheListener will
     * be invoked when the properties/items/key-value pairs of the distributed
     * object are altered.
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should listen to changes.
     * @param listener          The CacheListener to register to the given distributed object.
     * @return <code>true</code> if the CacheListener was successfully registered,
     * and <code>false</code> otherwise.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  registering a CacheListener (i.e. incorrect type
     *                                  for the underlying cache implementation).
     */
    boolean addListenerFor(@NotNull Object distributedObject, @NotNull CacheListener<K, V> listener)
            throws IllegalArgumentException;

    /**
     * Removes the given CacheListener from the distributed object.
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should remove the CacheListener.
     * @param cacheListenerId   The ID of the CacheListener to remove from the given distributed object.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  removing a CacheListener (i.e. incorrect type for the
     *                                  underlying cache implementation).
     */
    void removeListenerFor(@NotNull Object distributedObject, @NotNull String cacheListenerId)
            throws IllegalArgumentException;

    /**
     * Retrieves all IDs of the CacheListeners bound to the given distributedObject.
     *
     * @param distributedObject The distributedObject whose CacheListener IDs we should retrieve.
     * @return The IDs of all CacheListener instances registered to the provided distributedObject.
     */
    List<String> getListenersIDsFor(@NotNull Object distributedObject);
}
