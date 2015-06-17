/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-inmemory
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
package se.jguru.nazgul.core.cache.impl.inmemory;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.transaction.TransactedAction;
import se.jguru.nazgul.core.clustering.api.AbstractSwiftClusterable;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.clustering.api.UUIDGenerator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract Map-backed implementation of the Cache interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InMemoryMapCache extends AbstractSwiftClusterable implements Cache<String, Serializable> {

    // Internal state
    private long timeoutMillis;
    private ConcurrentMap<String, Serializable> cache;
    private ConcurrentMap<String, CacheListener<String, Serializable>> listeners;
    private boolean fakeTransactions;
    private String threadPoolPrefix;
    private int numEventListenerThreads;

    private transient ExecutorService listenerThreadService;

    /**
     * Default convenience constructor, using an UUIDGenerator, 20 minutes timeout,
     * empty ConcurrentHashMaps for cache and listener maps, 15 threads in the pool
     * and not faking transactedActions.
     */
    public InMemoryMapCache() {

        this(new UUIDGenerator(), 20 * 60 * 60L,
                new ConcurrentHashMap<String, Serializable>(),
                new ConcurrentHashMap<String, CacheListener<String, Serializable>>(),
                15,
                false);
    }

    /**
     * Creates a new AbstractIdentifiable and assigns the internal ID state.
     *
     * @param idGenerator      The ID generator used to acquire a cluster-unique
     *                         identifier for this AbstractClusterable instance.
     * @param timeoutMillis    The cache element timeout in milliseconds.
     *                         A value of zero implies no timeout/autonomous eviction.
     * @param cache            The map used for caching instances.
     * @param listeners        The map used for storing cache listeners.
     * @param fakeTransactions if {@code true}, this InMemoryMapCache does not throw an UnsupportedOperationException
     *                         when requested to perform transacted actions.
     */
    public InMemoryMapCache(final IdGenerator idGenerator,
                            final long timeoutMillis,
                            final ConcurrentMap<String, Serializable> cache,
                            final ConcurrentMap<String, CacheListener<String, Serializable>> listeners,
                            final int numEventListenerThreads,
                            final boolean fakeTransactions) {
        super(idGenerator);

        // Check sanity
        Validate.isTrue(timeoutMillis > 0, "Cannot handle zero or negative milliseconds argument.");
        Validate.notNull(cache, "Cannot handle null cache argument.");
        Validate.notNull(listeners, "Cannot handle null listeners argument.");

        // Assign internal state
        this.timeoutMillis = timeoutMillis;
        this.cache = cache;
        this.listeners = listeners;
        this.numEventListenerThreads = numEventListenerThreads;
        this.threadPoolPrefix = "InMemoryCacheListener-(" + hashCode() + ")";
        listenerThreadService = Executors.newFixedThreadPool(
                numEventListenerThreads, new NamedSequenceThreadFactory(threadPoolPrefix));
        this.fakeTransactions = fakeTransactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable remove(final String key) {

        // Remove the value
        final Serializable oldValue = cache.remove(key);

        // Notify any listeners
        if (!listeners.isEmpty()) {

            final Set<CacheListener<String, Serializable>> localListeners
                    = new HashSet<CacheListener<String, Serializable>>(listeners.values());

            listenerThreadService.execute(
                    new CacheListenerNotificationWorker(localListeners, CacheEventType.REMOVE, key, oldValue, null));
        }

        // All done.
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable put(final String key, final Serializable value) {

        // Put the value in the cache
        final Serializable oldValue = cache.put(key, value);

        // Notify any listeners
        if (!listeners.isEmpty()) {

            final Set<CacheListener<String, Serializable>> localListeners
                    = new HashSet<CacheListener<String, Serializable>>(listeners.values());
            final CacheEventType cacheEventType = oldValue == null ? CacheEventType.PUT : CacheEventType.UPDATE;

            listenerThreadService.execute(
                    new CacheListenerNotificationWorker(localListeners, cacheEventType, key, oldValue, value));
        }

        // All done.
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable get(final String key) {
        return cache.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final String key) {
        return cache.containsKey(key);
    }

    /**
     * Returns an iterator to a readonly version of the cache map.
     *
     * @return an Iterator to a readonly version of the cache map.
     */
    @Override
    public Iterator<String> iterator() {
        return Collections.unmodifiableMap(cache).keySet().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addListener(final CacheListener<String, Serializable> listener) {
        final CacheListener<String, Serializable> putListener
                = listeners.putIfAbsent(listener.getClusterId(), listener);
        return putListener == listener;
    }

    /**
     * Retrieves an unmodifiable list of all listener IDs.
     * {@inheritDoc}
     */
    @Override
    public List<String> getListenerIds() {
        return Collections.unmodifiableList(new ArrayList<String>(listeners.keySet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(final String key) {
        listeners.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performWriteExternal(final ObjectOutput out) throws IOException {

        // Write out state
        out.writeLong(timeoutMillis);
        out.writeBoolean(fakeTransactions);
        out.writeInt(numEventListenerThreads);
        out.writeUTF(threadPoolPrefix);
        out.writeObject(listeners);
        out.writeObject(cache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performReadExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        // Read in state
        timeoutMillis = in.readLong();
        fakeTransactions = in.readBoolean();
        numEventListenerThreads = in.readInt();
        threadPoolPrefix = in.readUTF();
        listeners = (ConcurrentMap<String, CacheListener<String, Serializable>>) in.readObject();
        cache = (ConcurrentMap<String, Serializable>) in.readObject();

        // Re-create the (transient) ExecutorService
        this.listenerThreadService = Executors.newFixedThreadPool(
                numEventListenerThreads, new NamedSequenceThreadFactory(threadPoolPrefix));
    }

    /**
     * Acquires a Transactional context from this Cache, and Executes the
     * TransactedAction::doInTransaction method within it.
     *
     * @param action The TransactedAction to be executed within a Cache Transactional context.
     * @throws UnsupportedOperationException if the underlying Cache implementation does not
     *                                       support Transactions.
     */
    @Override
    public void performTransactedAction(final TransactedAction action) throws UnsupportedOperationException {

        if (fakeTransactions) {
            action.doInTransaction();
        } else {
            throw new UnsupportedOperationException("InMemoryMapCaches do not support transactions. Use "
                    + "the fakeTransactions initialization parameter to simulate transacted behaviour.");
        }
    }

    //
    // Helpers
    //

    class CacheListenerNotificationWorker implements Runnable {

        // Internal state
        private Set<CacheListener<String, Serializable>> listeners;
        private CacheEventType event;
        private String key;
        private Serializable oldValue;
        private Serializable newValue;

        /**
         * Creates a CacheListenerNotificationWorker runnable which is used by the ExecutorService to perform
         * asynchronous callbacks to cache listeners.
         *
         * @param listeners The set of listeners which should be notified.
         * @param event     The event type generated.
         * @param key       The cache key.
         * @param oldValue  The old value of the cache entry.
         * @param newValue  The new value of the cache entry.
         */
        CacheListenerNotificationWorker(final Set<CacheListener<String, Serializable>> listeners,
                                        final CacheEventType event,
                                        final String key,
                                        final Serializable oldValue,
                                        final Serializable newValue) {
            super();

            // Check sanity
            Validate.notNull(event, "Cannot handle null CacheEventType argument.");
            Validate.notEmpty(listeners, "Cannot handle null or empty listeners argument.");
            Validate.notEmpty(key, "Cannot handle null or empty key argument.");

            // Assign internal state
            this.listeners = listeners;
            this.event = event;
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("all")
        public void run() {

            for (CacheListener<String, Serializable> current : listeners) {

                switch (event) {

                    case PUT:
                        current.onPut(key, newValue);
                        break;

                    case UPDATE:
                        current.onUpdate(key, newValue, oldValue);
                        break;

                    case REMOVE:
                        current.onRemove(key, oldValue);
                        break;

                    case CLEAR:
                        current.onClear();
                        break;

                    case AUTONOMOUS_EVICT:
                        current.onAutonomousEvict(key, newValue);
                        break;

                    case AUTONOMOUS_LOAD:
                        current.onAutonomousLoad(key, newValue);
                        break;

                    default:
                        throw new IllegalStateException("Could not identify cache event [" + event + "]");
                }
            }
        }
    }
}
