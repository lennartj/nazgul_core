/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
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
package se.jguru.nazgul.core.cache.api;

import se.jguru.nazgul.core.cache.api.transaction.TransactedAction;
import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;
import java.util.List;

/**
 * Service interface definition for a cache with a parameterized key type. Since all keys may be sent across a
 * distributed system, KeyType must be Serializable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Cache<KeyType extends Serializable> extends Clusterable {

    /**
     * Retrieves an object from this Cache.
     *
     * @param key The key of the instance to retrieve.
     * @return The value corresponding to the provided key, or <code>null</code> if no object was found.
     */
    Serializable get(KeyType key);

    /**
     * Stores the provided object in this Cache, associated with the provided key. Will overwrite existing objects with
     * identical key.
     *
     * @param key   The key under which to cache the provided value. This parameter should not be <code>null</code>.
     * @param value The value to cache.
     * @return The previous value associated with <code>key</code>, or <code>null</code> if no such object exists.
     */
    Serializable put(KeyType key, Serializable value);

    /**
     * Removes the object with the given key from the underlying cache implementation, returning the value held before
     * the object was removed.
     *
     * @param key The cache key for which the value should be removed.
     * @return The object to remove.
     */
    Serializable remove(KeyType key);

    /**
     * Adds a listener to events on this cache. All listeners on the local cache node must have unique IDs; should a
     * registered CacheListener exist with the same ID as the listener provided, the provided listener will not be
     * added. <strong>This operation may be an asynchronous operation depending on the underlying cache
     * implementation.</strong>
     *
     * @param listener The listener to add.
     * @return <code>true</true> if the CacheListener was properly added
     *         or <code>false</code> if it could not be added.
     */
    boolean addListener(CacheListener<KeyType> listener);

    /**
     * Acquires the list of all active Listeners of this Cache instance. Note that this does not include CacheListener
     * instances wired to distributed objects, nor CacheListener instances wired to other members within a distributed
     * cache.
     *
     * @return a List holding all IDs of the active Listeners onto this (local member) Cache. Note that this does not
     *         include CacheListener instances wired to distributed objects, nor nor CacheListener instances wired to
     *         other members within a distributed cache.
     */
    List<String> getListenerIds();

    /**
     * Removes the CacheListener with the given key. <strong>This operation may be an asynchronous operation
     * depending on the underlying cache implementation.</strong>
     *
     * @param key The unique identifier for the given CacheListener to remove from operating on this Cache.
     */
    void removeListener(KeyType key);

    /**
     * Returns true if this cache contains a mapping for the specified key
     *
     * @param key The <code>key</code> whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the specified key.
     */
    boolean containsKey(KeyType key);

    /**
     * Acquires a Transactional context from this Cache, and Executes the
     * TransactedAction::doInTransaction method within it.
     *
     * @param action The TransactedAction to be executed within a Cache Transactional context.
     * @throws UnsupportedOperationException if the underlying Cache implementation does not
     *                                       support Transactions.
     */
    void performTransactedAction(final TransactedAction action)
            throws UnsupportedOperationException;
}
