/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;
import java.util.EventListener;

/**
 * Callback listener for a Cache with KeyType type keys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface CacheListener<KeyType extends Serializable>
        extends Clusterable, EventListener {

    /**
     * Callback method invoked when the object with the given key
     * is stored within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The cache key.
     * @param value The new value (i.e. the value which was created)
     */
    void onPut(KeyType key, Serializable value);

    /**
     * Callback method invoked when the object with the given key
     * is updated within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key      The cache key.
     * @param newValue The new value - after the update.
     * @param oldValue The former value - before the update. <strong>Note!</strong> Depending on the
     *                 underlying cache implementation, this value may be <code>null</code> if not known
     *                 and transmitted at the time of invocation.
     */
    void onUpdate(final KeyType key, final Serializable newValue, final Serializable oldValue);

    /**
     * Callback method invoked when the object with the given
     * key is actively removed from the underlying cache
     * implementation (by a user call).
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was removed.
     */
    void onRemove(KeyType key, Serializable value);

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
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got loaded into the cache.
     * @param value The Object that was loaded.
     */
    void onAutonomousLoad(KeyType key, Serializable value);

    /**
     * Callback method invoked when the object with the given
     * key is evicted from the underlying cache implementation.
     * This is assumed to be the result of an autonomous/internal
     * call within the underlying cache implementation, as opposed
     * to a call to <code>remove(key)</code> from a server
     * implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     * <p/>
     * <strong>Note 2!</strong>. Depending on the underlying cache
     * implementation, the value may not be known (implying that it
     * is received as <code>null</code>).
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was evicted, or null if the underlying
     *              cache implementation does not know the value at eviction time.
     */
    void onAutonomousEvict(final KeyType key, final Serializable value);

    /**
     * Assigns a filter, making this CacheListener only receive
     * events for properties matching the provided patternFilter. If unset,
     * all configuration change events are received by this CacheListener.
     *
     * @param patternFilter The java regexp pattern filter.
     */
    void setFilter(final String patternFilter);
}