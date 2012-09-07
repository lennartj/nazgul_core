/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.api;

import se.jguru.nazgul.core.clustering.api.AbstractSwiftClusterable;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Abstract (skeleton) implementation of the CacheListener interface.
 * Convenience class, where all method bodies are empty. Override
 * this class if you need a CacheListener skeleton.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCacheListener<KeyType extends Serializable> extends AbstractSwiftClusterable
        implements CacheListener<KeyType> {

    // Internal state
    private transient Pattern filter;

    /**
     * Creates a new AbstractCacheListener with the provided id and filterDefinition.
     *
     * @param id               The identifier of this CacheListenerAdapter.
     * @param filterDefinition A filter definition of this AbstractCacheListener.
     *                         The filter definition is a java regExp pattern matching all
     *                         cache keys for which this AbstractCacheListener should receive events.
     */
    protected AbstractCacheListener(final String id, final String filterDefinition) {

        super(id);
        setFilter(filterDefinition);
    }

    /**
     * Creates a new AbstractCacheListener and assigns the internal ID state.
     *
     * @param id The identifier of this CacheListenerAdapter.
     */
    protected AbstractCacheListener(final String id) {
        super(id);
    }

    /**
     * Serialization-usable constructor, and not part of the public API of this
     * AbstractCacheListener. <strong>This is for framework use only</strong>.
     */
    protected AbstractCacheListener() {
        super((IdGenerator) null);
    }

    /**
     * Callback method invoked when the object with the given key
     * is stored within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object
     * @param value The value which was put.
     */
    @Override
    public final void onPut(final KeyType key, final Serializable value) {

        if (accept(key)) {
            doOnPut(key, value);
        }
    }

    /**
     * Callback method invoked when the object with the given key
     * is stored within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object
     * @param value The value which was put.
     */
    protected void doOnPut(final KeyType key, final Serializable value) {

    }

    /**
     * Callback method invoked when the object with the given key
     * is updated within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key      The cache key.
     * @param newValue The new value - after the update.
     * @param oldValue The former value - before the update.
     */
    @Override
    public final void onUpdate(final KeyType key, final Serializable newValue, final Serializable oldValue) {

        if (accept(key)) {
            doOnUpdate(key, newValue, oldValue);
        }
    }

    /**
     * Callback method invoked when the object with the given key
     * is updated within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key      The cache key.
     * @param newValue The new value - after the update.
     * @param oldValue The former value - before the update.
     */
    protected void doOnUpdate(final KeyType key, final Serializable newValue, final Serializable oldValue) {

    }

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
    @Override
    public final void onRemove(final KeyType key, final Serializable value) {

        if (accept(key)) {
            doOnRemove(key, value);
        }
    }

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
    protected void doOnRemove(final KeyType key, final Serializable value) {

    }

    /**
     * Callback method invoked when the underlying cache
     * is cleared (i.e. its state destroyed and all cached
     * objects evicted).
     */
    @Override
    public void onClear() {

    }

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
    @Override
    public final void onAutonomousLoad(final KeyType key, final Serializable value) {
        if (accept(key)) {
            doOnAutonomousLoad(key, value);
        }
    }

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
    protected void doOnAutonomousLoad(final KeyType key, final Serializable value) {

    }

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
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was evicted.
     */
    @Override
    public final void onAutonomousEvict(final KeyType key, final Serializable value) {
        if (accept(key)) {
            doOnAutonomousEvict(key, value);
        }
    }

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
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was evicted.
     */
    protected void doOnAutonomousEvict(final KeyType key, final Serializable value) {

    }

    /**
     * Assigns a filter, making this ConfigurationEventListener only receive
     * events for properties matching the provided patternFilter. If unset,
     * all configuration change events are received by this ConfigurationEventListener.
     *
     * @param patternFilter The java regexp pattern filter.
     */
    @Override
    public void setFilter(final String patternFilter) {
        this.filter = Pattern.compile(patternFilter);
    }

    //
    // Private helpers
    //

    private boolean accept(final KeyType key) {

        // No filter ==> accept all keys.
        return this.filter == null || filter.matcher(key.toString()).matches();
    }
}
