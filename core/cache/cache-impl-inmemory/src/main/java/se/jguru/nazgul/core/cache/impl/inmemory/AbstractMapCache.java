package se.jguru.nazgul.core.cache.impl.inmemory;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.clustering.api.AbstractSwiftClusterable;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract Map-backed implementation of the Cache interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AbstractMapCache extends AbstractSwiftClusterable implements Cache<String> {

    // Internal state
    private static final AtomicInteger threadCounter = new AtomicInteger(1);
    private final long timeoutMillis;
    private ConcurrentMap<String, Serializable> cache;
    private ConcurrentMap<String, CacheListener<String>> listeners;
    private ExecutorService listenerThreadService;

    /**
     * Creates a new AbstractIdentifiable and assigns the internal ID state.
     *
     * @param idGenerator   The ID generator used to acquire a cluster-unique
     *                      identifier for this AbstractClusterable instance.
     * @param timeoutMillis The cache element timeout in milliseconds.
     *                      A value of zero implies no timeout/autonomous eviction.
     * @param cache         The map used for caching instances.
     * @param listeners     The map used for storing cache listeners.
     */
    public AbstractMapCache(final IdGenerator idGenerator,
                            final long timeoutMillis,
                            final ConcurrentMap<String, Serializable> cache,
                            final ConcurrentMap<String, CacheListener<String>> listeners,
                            final int numEventListenerThreads) {
        super(idGenerator);

        // Check sanity
        Validate.isTrue(timeoutMillis < 0, "Cannot handle negative milliseconds argument.");
        Validate.notNull(cache, "Cannot handle null cache argument.");
        Validate.notNull(listeners, "Cannot handle null listeners argument.");

        // Assign internal state
        this.timeoutMillis = timeoutMillis;
        this.cache = cache;
        this.listeners = listeners;
        listenerThreadService = Executors.newFixedThreadPool(numEventListenerThreads,
                new NamedSequenceThreadFactory("CacheListener", numEventListenerThreads));
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

            final Set<CacheListener<String>> localListeners = new HashSet<CacheListener<String>>(listeners.values());

            listenerThreadService.execute(
                    new CacheListenerNotificationWorker(localListeners, CacheEvent.REMOVE, key, oldValue, null));
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

        // All done.
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
    public boolean addListener(final CacheListener<String> listener) {
        final CacheListener<String> putListener = listeners.putIfAbsent(listener.getClusterId(), listener);
        return putListener == listener;
    }

    /**
     * Retrieves an unmodifiable list of all listener IDs.
     * <p/>
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

    //
    // Helpers
    //

    class CacheListenerNotificationWorker implements Runnable {

        // Internal state
        private Set<CacheListener<String>> listeners;
        private CacheEvent event;
        private String key;
        private Serializable oldValue;
        private Serializable newValue;

        /**
         * Allocates a new <code>Thread</code> object. This constructor has
         * the same effect as <code>Thread(null, null, name)</code>.
         *
         * @param threadName the name of the new thread.
         * @see #Thread(ThreadGroup, Runnable, String)
         */
        CacheListenerNotificationWorker(final Set<CacheListener<String>> listeners,
                                        final CacheEvent event,
                                        final String key,
                                        final Serializable oldValue,
                                        final Serializable newValue) {
            super("ListenerNotification");

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
        public void run() {

            for (CacheListener<String> current : listeners) {

                switch (event) {

                    case PUT:
                        current.onPut(key, newValue);
                        break;

                    case UPDATE:
                        current.onUpdate(key, newValue, oldValue);
                        break;

                    case REMOVE:
                        current.onRemove(key, newValue);
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
                }
            }
        }
    }
}
