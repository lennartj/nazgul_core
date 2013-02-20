/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.distributed.async.DestinationProvider;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.GridOperations;
import se.jguru.nazgul.core.clustering.api.AbstractClusterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Abstract identifiable handling local registration and de-registration of
 * HazelcastCacheListenerAdapters to cached objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHazelcastCacheListenerManager extends AbstractClusterable
        implements GridOperations, DestinationProvider<String> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractHazelcastCacheListenerManager.class);

    // Internal state
    private Map<String, HazelcastCacheListenerAdapter> locallyRegisteredListeners = new TreeMap<String, HazelcastCacheListenerAdapter>();
    protected final Object lock = new Object();

    /**
     * Creates a new AbstractHazelcastCacheListenerManager and assigns the internal ID state.
     *
     * @param id The identifier of this AbstractHazelcastCacheListenerManager.
     */
    protected AbstractHazelcastCacheListenerManager(final String id) {
        super(id);
    }

    /**
     * Adds a listener to events on this cache, unless another existing CacheListener
     * is found with the same id as the provided CacheListener.
     *
     * @param listener The listener to add.
     * @return true if the cacheListener was added, and false otherwise.
     */
    @Override
    public final boolean addListener(final CacheListener listener) {
        return addListenerFor(getSharedMap(), listener);
    }

    /**
     * Removes and returns the CacheListener with the given key.
     * <p/>
     * <strong>This operation is asynchronous.</strong>
     *
     * @param key The unique identifier for the given CacheListener to remove from operating on this Cache.
     */
    @Override
    public final void removeListener(final String key) {
        removeListenerFor(getSharedMap(), key);
    }

    /**
     * Acquires the list of all active Listeners of this Cache instance. Note that this does not include CacheListener
     * instances wired to distributed objects, nor CacheListener instances wired to other members within a distributed
     * cache.
     *
     * @return a List holding all IDs of the active Listeners onto this (local member) Cache. Note that this does not
     *         include CacheListener instances wired to distributed objects, nor nor CacheListener instances wired to
     *         other members within a distributed cache.
     */
    @Override
    public final List<String> getListenerIds() {

        final Set<String> tmp = new TreeSet<String>();
        final IMap<String, TreeSet<String>> listenersIdMap = getCacheListenersIDMap();

        for (final String current : listenersIdMap.keySet()) {
            for (final String currentListenerID : listenersIdMap.get(current)) {
                tmp.add(currentListenerID);
            }
        }

        // Return an unmodifiable copy.
        return Collections.unmodifiableList(new ArrayList<String>(tmp));
    }

    /**
     * Adds a CacheListener to the distributed object. The cacheListener will
     * be invoked when the properties/items/key-value pairs of the distributed
     * object are altered.
     * <p/>
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should
     *                          listen to changes.
     * @param listener          The CacheListener to register to the given distributed object.
     * @return <code>true</code> if the CacheListener was successfully registered,
     *         and <code>false</code> otherwise.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  registering a CacheListener (i.e. incorrect type
     *                                  for the underlying cache implementation).
     */
    @Override
    public final boolean addListenerFor(final Object distributedObject, final CacheListener listener)
            throws IllegalArgumentException {

        final Instance instance = cast(distributedObject);

        // Is the listener already registered onto the shared map?
        final TreeSet<String> knownListenerIDs = getCacheListenersIDMap().get("" + instance.getId());
        if (knownListenerIDs != null && knownListenerIDs.contains(listener.getId())) {

            if (log.isWarnEnabled()) {
                log.warn("(CacheID: " + getId() + "): CacheListener [" + listener.getId()
                        + "] was already registered to the Instance [" + instance.getId()
                        + "]. Aborting registration.");
            }

            return false;
        }

        // Do we have a mismatch between the knownListenerIDs and our locallyRegisteredListeners map?
        if (locallyRegisteredListeners.containsKey(listener.getId())) {
            if (log.isWarnEnabled()) {
                final HazelcastCacheListenerAdapter alreadyRegistered =
                        locallyRegisteredListeners.get(listener.getId());
                log.warn("Already registered listener [" + alreadyRegistered.getId()
                        + "] holding CacheListener of type ["
                        + alreadyRegistered.getCacheListener().getClass().getName() + "]. Aborting registration.");
            }

            // TODO: Loop through all Instances and remove the dangling locallyRegisteredListener instance.
            // for(Instance current : cacheInstance.getInstances()) {
            // }
            return false;
        }

        // Wrap the CacheListener inside a HazelcastCacheListenerAdapter.
        final HazelcastCacheListenerAdapter toAdd = new HazelcastCacheListenerAdapter(listener);

        synchronized (lock) {
            performTransactedAction(new AbstractTransactedAction("Could not add listener [" + listener.getId()
                    + "] of type [" + listener.getClass().getName() + "]") {

                /**
                 * Defines a method invoked within a Transactional
                 * boundary, using the Cache as context.
                 *
                 * @throws RuntimeException if the implementation needs to signal
                 *                          a rollback to the Cache Transaction manager.
                 */
                @Override
                public void doInTransaction() throws RuntimeException {

                    // Add the listener id to the CLUSTERWIDE_LISTENERID_MAP
                    addListenerIdFor(instance, toAdd.getId());

                    // Add the listener internally, to enable unregistering later on.
                    locallyRegisteredListeners.put(toAdd.getId(), toAdd);

                    // ... and add the listener to the instance it should listen to...
                    switch (instance.getInstanceType()) {
                        case MAP:
                            ((IMap) instance).addEntryListener(toAdd, true);
                            break;

                        case LIST:
                        case SET:
                        case QUEUE:
                            ((ICollection) instance).addItemListener(toAdd, true);
                            break;

                        default:

                            final Instance.InstanceType[] permittedTypes =
                                    {Instance.InstanceType.LIST, Instance.InstanceType.SET, Instance.InstanceType.QUEUE,
                                            Instance.InstanceType.MAP};

                            final StringBuffer permitted = new StringBuffer("[");
                            for (final Instance.InstanceType current : permittedTypes) {
                                permitted.append(current.name()).append(", ");
                            }

                            throw new IllegalArgumentException("Will not add listener to an instance of type ["
                                    + instance.getInstanceType().name() + "]. Supported types are "
                                    + permitted.substring(0, permitted.length() - 2) + "].");
                    }
                }
            });
        }

        // All done.
        return true;
    }

    /**
     * Removes the given CacheListener from the distributed object.
     * <p/>
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should remove the CacheListener.
     * @param cacheListenerId   The ID of the CacheListener to remove from the given distributed object.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  removing a CacheListener (i.e. incorrect type for the
     *                                  underlying cache implementation).
     */
    @Override
    public final void removeListenerFor(final Object distributedObject, final String cacheListenerId)
            throws IllegalArgumentException {

        final Instance instance = cast(distributedObject);

        // Is the cacheListenerId registered?
        final TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + instance.getId());
        if (listenerIDs == null || !listenerIDs.contains(cacheListenerId)) {

            throw new IllegalStateException("(CacheID: " + getId() + "): Listener [" + cacheListenerId
                    + "] not registered for instance [" + instance.getId() + "] in Hazelcast.");
        }

        // All seems sane.
        // Send the message that removes the listener.
        sendAdminMessage(AdminMessage.createRemoveListenerMessage("" + instance.getId(), cacheListenerId));
    }

    /**
     * Retrieves all IDs of the CacheListeners bound to the given distributedObject.
     *
     * @param distributedObject The distributedObject whose CacheListener IDs we should retrieve.
     * @return The IDs of all CacheListener instances registered to the provided distributedObject.
     */
    @Override
    public final List<String> getListenersIDsFor(final Object distributedObject) {

        // Cast, and acquire our listeners.
        final Instance instance = cast(distributedObject);

        // Wrap and return
        return Collections.unmodifiableList(new ArrayList<String>(getListenerIDsFor(instance)));
    }

    //
    // Helpers
    //

    /**
     * Checks if the provided listener is locally registered within this AbstractHazelcastCacheListenerManager.
     *
     * @param listener The CacheListener to check.
     * @return <code>true</code> if the CacheListener is registered within this AbstractHazelcastCacheListenerManager.
     */
    protected final boolean isLocallyRegistered(final CacheListener listener) {

        return listener != null && locallyRegisteredListeners.containsKey(listener.getId());
    }

    /**
     * @return The Map of locally registered listeners.
     */
    protected Map<String, HazelcastCacheListenerAdapter> getLocallyRegisteredListeners() {
        return locallyRegisteredListeners;
    }

    /**
     * Validates that the provided distributedObject is a Hazelcast Instance.
     *
     * @param distributedObject The object to validate.
     * @return The distributedObject, type cast to a Hazelcast Instance.
     * @throws IllegalArgumentException if the distributedObject was not a Hazelcast Instance.
     */
    @Override
    public Instance cast(final Object distributedObject) throws IllegalArgumentException {

        if (!(distributedObject instanceof Instance)) {
            throw new IllegalArgumentException("(CacheID: " + getId() + "): Only Instance objects can be "
                    + "distributed in Hazelcast. Class [" + distributedObject.getClass().getName()
                    + "] is not an instance.");
        }

        // Cast the distributedObject for type switching.
        return (Instance) distributedObject;
    }

    /**
     * Retrieves all IDs of the CacheListeners bound to the given distributedObject Instance.
     *
     * @param distributedObject The distributedObject Instance whose CacheListener IDs we should retrieve.
     * @return The IDs of all CacheListener instances registered to the provided distributedObject.
     */
    protected final Set<String> getListenerIDsFor(final Instance distributedObject) {

        final TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + distributedObject.getId());
        if (listenerIDs == null) {

            // This is not a distributed/replicated structure,
            // implying that it can only be returned READ-ONLY
            // to the calling API client.
            return new HashSet<String>();
        }

        return listenerIDs;
    }
}
