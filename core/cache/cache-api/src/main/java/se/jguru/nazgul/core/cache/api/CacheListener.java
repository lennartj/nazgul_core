/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
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
package se.jguru.nazgul.core.cache.api;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.EventListener;

/**
 * Callback listener for a Cache with KeyType type keys.
 *
 * @param <K> The type of key used within the cache to which this CacheListener should be attached.
 * @param <V> The type of value used within the cache to which this CacheListener should be attached.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CacheListener<K, V> extends Clusterable, EventListener {

    /**
     * Callback method invoked when the object with the given key
     * is stored within the underlying cache implementation.
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The cache key.
     * @param value The new value (i.e. the value which was created)
     */
    void onPut(K key, final V value);

    /**
     * Callback method invoked when the object with the given key
     * is updated within the underlying cache implementation.
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key      The cache key.
     * @param newValue The new value - after the update.
     * @param oldValue The former value - before the update. <strong>Note!</strong> Depending on the
     *                 underlying cache implementation, this value may be <code>null</code> if not known
     *                 and transmitted at the time of invocation.
     */
    void onUpdate(K key, V newValue, V oldValue);

    /**
     * Callback method invoked when the object with the given
     * key is actively removed from the underlying cache
     * implementation (by a user call).
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was removed.
     */
    void onRemove(K key, V value);

    /**
     * Callback method invoked when the underlying cache
     * is cleared (i.e. its state destroyed and all cached
     * objects evicted).
     */
    void onClear();

    /**
     * Callback method invoked when the object with the given key
     * is (re-)loaded into the underlying cache implementation.
     * This is assumed to be the result of an autonomous/internal
     * call within the underlying cache implementation, as opposed
     * to a call to <code>put(key, value)</code>.
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got loaded into the cache.
     * @param value The Object that was loaded.
     */
    void onAutonomousLoad(K key, V value);

    /**
     * <p>Callback method invoked when the object with the given
     * key is evicted from the underlying cache implementation.
     * This is assumed to be the result of an autonomous/internal
     * call within the underlying cache implementation, as opposed
     * to a call to <code>remove(key)</code> from a server
     * implementation.</p>
     * <p><strong>Note!</strong> The key and value must not be modified
     * within this callback method.</p>
     * <p><strong>Note 2!</strong> Depending on the underlying cache
     * implementation, the value may not be known (implying that it
     * is received as <code>null</code>).</p>
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was evicted, or null if the underlying
     *              cache implementation does not know the value at eviction time.
     */
    void onAutonomousEvict(K key, V value);

    /**
     * Assigns a filter, making this CacheListener only receive
     * events for properties matching the provided patternFilter. If unset,
     * all configuration change events are received by this CacheListener.
     *
     * @param patternFilter The java regexp pattern filter.
     */
    void setFilter(String patternFilter);
}
