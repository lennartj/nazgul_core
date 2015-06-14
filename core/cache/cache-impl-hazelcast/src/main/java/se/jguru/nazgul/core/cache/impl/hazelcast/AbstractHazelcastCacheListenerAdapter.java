/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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

package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.MapEvent;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.CacheListener;

import java.io.Serializable;

/**
 * Adapter delegating Hazelcast event calls to the CacheListener interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHazelcastCacheListenerAdapter<K, V>
        implements DistributedObjectListener, ItemListener<V>, EntryListener<K, V>, Serializable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractHazelcastCacheListenerAdapter.class);

    /**
     * Key used for callback methods whenever this cacheListenerAdapter is wired to a distributed Collection, and a key
     * argument is expected. Since Collections do not use keys, this illusory key is used instead.
     */
    public static final String ILLUSORY_KEY_FOR_COLLECTIONS = "collectionsDontUseKeys-OnlyValues";

    // Internal state
    private CacheListener<K, V> listener;

    /**
     * Creates a new HazelcastCacheListenerAdapter for a Cache instance.
     *
     * @param listener A cacheListener to which we the events of this HazelcastCacheListenerAdapter will be delegated.
     * @throws IllegalArgumentException if the listener argument is null.
     */
    public AbstractHazelcastCacheListenerAdapter(final CacheListener<K, V> listener) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(listener, "Cannot handle null listener argument.");

        // Assign internal state
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (obj == null || !(obj instanceof AbstractHazelcastCacheListenerAdapter)) {
            return false;
        }

        // Delegate to our identifier.
        final AbstractHazelcastCacheListenerAdapter that = (AbstractHazelcastCacheListenerAdapter) obj;
        return getId().equals(that.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        int toReturn = 0;
        if (listener != null) {
            toReturn = listener.getClusterId().hashCode();
        }

        // All done
        return toReturn;
    }

    /**
     * @return The identifier of the contained CacheListener.
     */
    public final String getId() {
        return listener.getClusterId();
    }

    /**
     * Invoked when an entry is added.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryAdded(final EntryEvent<K, V> entryEvent) {
        logEntryEvent(entryEvent, "Added");
        listener.onPut(entryEvent.getKey(), entryEvent.getValue());
    }

    /**
     * Invoked when an entry is removed.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryRemoved(final EntryEvent<K, V> entryEvent) {
        logEntryEvent(entryEvent, "Removed");
        listener.onRemove(entryEvent.getKey(), entryEvent.getValue());
    }

    /**
     * Invoked when an entry is updated.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryUpdated(final EntryEvent<K, V> entryEvent) {
        logEntryEvent(entryEvent, "Updated");
        listener.onUpdate(entryEvent.getKey(), entryEvent.getValue(), entryEvent.getOldValue());
    }

    /**
     * Invoked when an entry is evicted.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryEvicted(final EntryEvent<K, V> entryEvent) {
        logEntryEvent(entryEvent, "Evicted");
        listener.onAutonomousEvict(entryEvent.getKey(), entryEvent.getValue());
    }

    /**
     * Invoked when an item is added.
     *
     * @param itemEvent event with the removed item.
     */
    @Override
    public void itemAdded(final ItemEvent<V> itemEvent) {

        final V item = itemEvent.getItem();
        log.debug("Added item of type [" + item.getClass().getName() + "]");

        // Create the synthetic key and delegate the put to the
        listener.onPut(convertFrom(ILLUSORY_KEY_FOR_COLLECTIONS), item);
    }

    /**
     * Acquires a synthetic key of type K used to pad the Cache listener specification when a cache key
     * is not available from an event generated by the cache. Typically, this is the case when
     *
     * @param distributedObjectId The ID of the distributed object for which this synthetic key should be retrieved.
     * @return a synthetic key of type K used to satisfy the Cache listener API when a cache key is not available.
     */
    protected abstract K convertFrom(final String distributedObjectId);

    /**
     * Creates a V type value from the supplied source object.
     *
     * @param source a non-null source.
     * @return A cache-compliant value source.
     */
    protected abstract V createFrom(final Object source);

    /**
     * Invoked when an item is removed.
     *
     * @param itemEvent event with the removed item.
     */
    @Override
    public void itemRemoved(final ItemEvent<V> itemEvent) {

        final V item = itemEvent.getItem();
        log.debug("Removed item of type [" + item.getClass().getName() + "]");

        listener.onRemove(convertFrom(ILLUSORY_KEY_FOR_COLLECTIONS), item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void distributedObjectCreated(final DistributedObjectEvent event) {

        log.debug("Created DistributedObject of type [" + event.getDistributedObject().getClass().getName() + "]");

        final DistributedObject theInstance = event.getDistributedObject();
        listener.onPut(convertFrom(theInstance.getName()), createFrom(theInstance));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void distributedObjectDestroyed(final DistributedObjectEvent event) {

        log.debug("Destroyed DistributedObject of type [" + event.getDistributedObject().getClass().getName() + "]");

        final DistributedObject theInstance = event.getDistributedObject();
        listener.onRemove(convertFrom(theInstance.getName()), createFrom(theInstance));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapEvicted(final MapEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Evicted IMap [" + event.getName() + "], affecting ["
                    + event.getNumberOfEntriesAffected() + "] entries.");
        }
        listener.onRemove(convertFrom(event.getName()), createFrom(event.getSource()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapCleared(final MapEvent event) {
        log.debug("Cleared IMap [" + event.getName() + "]");
        listener.onRemove(convertFrom(event.getName()), createFrom(event.getSource()));
    }

    /**
     * @return the wrapped CacheListener instance.
     */
    public final CacheListener getCacheListener() {
        return listener;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return the ID of this object.
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + " wrapping CacheListener (" + listener.getClusterId() + ")]";
    }

    //
    // Private helpers
    //

    /**
     * Internal logger method.
     *
     * @param entryEvent The EntryEvent from Hazelcast.
     * @param action     The EntryEvent action, such as "Added".
     */
    private void logEntryEvent(final EntryEvent<K, V> entryEvent, final String action) {

        if (log.isDebugEnabled()) {

            String localMember = entryEvent.getMember() == null ? "<no member>"
                    : entryEvent.getMember().localMember() ? " (local member)" : "";
            final String memberIP = entryEvent.getMember() == null
                    ? ""
                    : entryEvent.getMember().getSocketAddress().toString();

            log.debug("(Listener " + getId() + "): " + action + " entry [" + entryEvent.getName() + "] from member ["
                    + memberIP + "]" + localMember + ". Key: " + entryEvent.getKey() + ", Value: "
                    + entryEvent.getValue());
        }
    }
}
