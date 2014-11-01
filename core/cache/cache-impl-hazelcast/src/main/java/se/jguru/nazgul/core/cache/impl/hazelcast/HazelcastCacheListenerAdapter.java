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
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HazelcastCacheListenerAdapter implements DistributedObjectListener, ItemListener,
        EntryListener, Serializable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HazelcastCacheListenerAdapter.class);

    /**
     * Key used for callback methods whenever this cacheListenerAdapter is wired to a distributed Collection, and a key
     * argument is expected. Since Collections do not use keys, this illusory key is used instead.
     */
    public static final String ILLUSORY_KEY_FOR_COLLECTIONS = "collectionsDontUseKeys-OnlyValues";

    // Internal state
    private CacheListener listener;

    /**
     * Creates a new HazelcastCacheListenerAdapter for a Cache instance.
     *
     * @param listener A cacheListener to which we the events of this HazelcastCacheListenerAdapter will be delegated.
     * @throws IllegalArgumentException if the listener argument is null.
     */
    public HazelcastCacheListenerAdapter(final CacheListener listener) throws IllegalArgumentException {

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

        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HazelcastCacheListenerAdapter)) {
            return false;
        }

        // Delegate to our identifier.
        final HazelcastCacheListenerAdapter that = (HazelcastCacheListenerAdapter) obj;
        return getId().equals(that.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((listener == null) ? 0 : listener.getClusterId().hashCode());
        return result;
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
    public void entryAdded(final EntryEvent entryEvent) {

        logEntryEvent(entryEvent, "Added");

        listener.onPut(getSerializable(entryEvent.getKey()), (Serializable) entryEvent.getValue());
    }

    /**
     * Invoked when an entry is removed.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryRemoved(final EntryEvent entryEvent) {

        logEntryEvent(entryEvent, "Removed");

        listener.onRemove(getSerializable(entryEvent.getKey()), (Serializable) entryEvent.getValue());
    }

    /**
     * Invoked when an entry is updated.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryUpdated(final EntryEvent entryEvent) {

        logEntryEvent(entryEvent, "Updated");

        listener.onUpdate(getSerializable(entryEvent.getKey()), (Serializable) entryEvent.getValue(),
                (Serializable) entryEvent.getOldValue());
    }

    /**
     * Invoked when an entry is evicted.
     *
     * @param entryEvent entry event
     */
    @Override
    public void entryEvicted(final EntryEvent entryEvent) {

        logEntryEvent(entryEvent, "Evicted");

        listener.onAutonomousEvict(getSerializable(entryEvent.getKey()), (Serializable) entryEvent.getValue());
    }

    /**
     * Invoked when an item is added.
     *
     * @param itemEvent event with the removed item.
     */
    @Override
    public void itemAdded(final ItemEvent itemEvent) {

        final Object item = itemEvent.getItem();
        log.debug("Added item of type [" + item.getClass().getName() + "]");

        listener.onPut(ILLUSORY_KEY_FOR_COLLECTIONS, (Serializable) item);
    }

    /**
     * Invoked when an item is removed.
     *
     * @param itemEvent event with the removed item.
     */
    @Override
    public void itemRemoved(final ItemEvent itemEvent) {

        final Object item = itemEvent.getItem();
        log.debug("Removed item of type [" + item.getClass().getName() + "]");

        listener.onRemove(ILLUSORY_KEY_FOR_COLLECTIONS, (Serializable) item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void distributedObjectCreated(final DistributedObjectEvent event) {

        log.debug("Created DistributedObject of type [" + event.getDistributedObject().getClass().getName() + "]");

        final DistributedObject theInstance = event.getDistributedObject();
        listener.onPut("" + theInstance.getName(), (Serializable) theInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void distributedObjectDestroyed(final DistributedObjectEvent event) {

        log.debug("Destroyed DistributedObject of type [" + event.getDistributedObject().getClass().getName() + "]");

        final DistributedObject theInstance = event.getDistributedObject();
        listener.onRemove("" + theInstance.getName(), (Serializable) theInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapEvicted(final MapEvent event) {

        log.debug("Evicted IMap [" + event.getName() + "]");

        final Object eventSource = event.getSource();
        final String eventSourceType = eventSource == null ? "<null EventSource>" : eventSource.getClass().getName();
        final Serializable src = eventSource instanceof Serializable
                ? (Serializable) eventSource
                : "<non-serializable eventSource of type [" + eventSourceType + "]>";

        listener.onRemove("" + event.getName(), src);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapCleared(final MapEvent event) {
        log.debug("Cleared IMap [" + event.getName() + "]");

        final Object eventSource = event.getSource();
        final String eventSourceType = eventSource == null ? "<null EventSource>" : eventSource.getClass().getName();
        final Serializable src = eventSource instanceof Serializable
                ? (Serializable) eventSource
                : "<non-serializable eventSource of type [" + eventSourceType + "]>";

        listener.onRemove("" + event.getName(), src);
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
    private void logEntryEvent(final EntryEvent entryEvent, final String action) {

        if (log.isDebugEnabled()) {

            String localMember = entryEvent.getMember() == null ? "<no member>"
                    : entryEvent.getMember().localMember() ? " (local member)" : "";
            final String memberIP = entryEvent.getMember() == null
                    ? ""
                    : entryEvent.getMember().getInetSocketAddress().toString();

            log.debug("(Listener " + getId() + "): " + action + " entry [" + entryEvent.getName() + "] from member ["
                    + memberIP + "]" + localMember + ". Key: " + entryEvent.getKey() + ", Value: "
                    + entryEvent.getValue());
        }
    }

    /**
     * Converts the provided object to a Serializable to be used for CacheListener keys.
     *
     * @param obj The object to convert.
     * @return The serializable to be used as a key.
     */
    private Serializable getSerializable(final Object obj) {

        if (obj instanceof Serializable) {
            return (Serializable) obj;
        }

        final String toReturn = obj.toString();

        if (log.isWarnEnabled()) {
            log.warn("Got non-serializable key [" + obj.getClass().getName() + "]. Emitting string version ["
                    + toReturn + "]");
        }

        return toReturn;
    }
}
