/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
public abstract class AbstractCacheListener<K extends Serializable, V>
        extends AbstractSwiftClusterable implements CacheListener<K, V> {

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
    public final void onPut(final K key, final V value) {

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
    protected void doOnPut(final K key, final V value) {

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
    public final void onUpdate(final K key, final V newValue, final V oldValue) {

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
    protected void doOnUpdate(final K key, final V newValue, final V oldValue) {

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
    public final void onRemove(final K key, final V value) {

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
    protected void doOnRemove(final K key, final V value) {

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
    public final void onAutonomousLoad(final K key, final V value) {
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
    protected void doOnAutonomousLoad(final K key, final V value) {

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
    public final void onAutonomousEvict(final K key, final V value) {
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
    protected void doOnAutonomousEvict(final K key, final V value) {

    }

    /**
     * Assigns a filter, making this ConfigurationEventListener only receive
     * events for properties matching the provided patternFilter. If unset,
     * all configuration change events are received by this ConfigurationEventListener.
     *
     * @param patternFilter The java regexp pattern filter.
     */
    @Override
    public final void setFilter(final String patternFilter) {
        this.filter = Pattern.compile(patternFilter);
    }

    //
    // Private helpers
    //

    private boolean accept(final K key) {

        // No filter ==> accept all keys.
        return this.filter == null || filter.matcher(key.toString()).matches();
    }
}
