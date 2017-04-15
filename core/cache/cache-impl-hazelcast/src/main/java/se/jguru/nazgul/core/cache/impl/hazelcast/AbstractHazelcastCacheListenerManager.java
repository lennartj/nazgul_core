/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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

package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
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
 * HazelcastCacheListenerAdapters to cached objects. The AbstractHazelcastCacheListenerManager contains a local
 * (in-memory) listener Map, since Hazelcast (version 2) requires that the actual listener object is used to
 * de-register the listener.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings({"rawtypes", "unchecked", "serial"})
public abstract class AbstractHazelcastCacheListenerManager extends AbstractClusterable
        implements GridOperations, DestinationProvider<String, Object> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractHazelcastCacheListenerManager.class);

    // Internal state
    private Map<String, AbstractHazelcastCacheListenerAdapter> locallyRegisteredListeners = new TreeMap<>();
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
    public final boolean addListener(final CacheListener<String, Object> listener) {
        return addListenerFor(getSharedMap(), listener);
    }

    /**
     * Removes and returns the CacheListener with the given key.
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
     * include CacheListener instances wired to distributed objects, nor nor CacheListener instances wired to
     * other members within a distributed cache.
     */
    @Override
    public final List<String> getListenerIds() {

        final Set<String> tmp = new TreeSet<String>();
        final IMap<String, TreeSet<String>> listenersIdMap = getCacheListenersIDMap();

        for (final Map.Entry<String, TreeSet<String>> current : listenersIdMap.entrySet()) {
            for (final String currentListenerID : current.getValue()) {
                tmp.add(currentListenerID);
            }
        }

        // Return an unmodifiable copy.
        return Collections.unmodifiableList(new ArrayList<>(tmp));
    }

    /**
     * Adds a CacheListener to the distributed object. The cacheListener will
     * be invoked when the properties/items/key-value pairs of the distributed
     * object are altered.
     * <strong>This operation may be an asynchronous operation depending
     * on the underlying cache implementation.</strong>
     *
     * @param distributedObject The distributed object from which we should
     *                          listen to changes.
     * @param listener          The CacheListener to register to the given distributed object.
     * @return <code>true</code> if the CacheListener was successfully registered,
     * and <code>false</code> otherwise.
     * @throws IllegalArgumentException If the distributedObject was not appropriate for
     *                                  registering a CacheListener (i.e. incorrect type
     *                                  for the underlying cache implementation).
     */
    @Override
    public final boolean addListenerFor(final Object distributedObject, final CacheListener<String, Object> listener)
            throws IllegalArgumentException {

        final DistributedObject distObject = cast(distributedObject);

        // Is the listener already registered onto the shared map?
        final TreeSet<String> knownListenerIDs = getCacheListenersIDMap().get("" + distObject.getName());
        if (knownListenerIDs != null && knownListenerIDs.contains(listener.getClusterId())) {

            if (log.isWarnEnabled()) {
                log.warn("(CacheID: " + getClusterId() + "): CacheListener [" + listener.getClusterId()
                        + "] was already registered to the Instance [" + distObject.getName()
                        + "]. Aborting registration.");
            }

            // Bail out.
            return false;
        }

        // Do we have a mismatch between the knownListenerIDs and our locallyRegisteredListeners map?
        if (locallyRegisteredListeners.containsKey(listener.getClusterId())) {
            if (log.isWarnEnabled()) {
                final AbstractHazelcastCacheListenerAdapter alreadyRegistered =
                        locallyRegisteredListeners.get(listener.getClusterId());
                log.warn("Already registered listener [" + alreadyRegistered.getId()
                        + "] holding CacheListener of type ["
                        + alreadyRegistered.getCacheListener().getClass().getName() + "]. Aborting registration.");
            }

            // We should not re-register the listener.
            return false;
        }

        // Wrap the CacheListener inside a HazelcastCacheListenerAdapter.
        final AbstractHazelcastCacheListenerAdapter<String, Object> toAdd =
                new AbstractHazelcastCacheListenerAdapter(listener) {
                    @Override
                    protected Object convertFrom(final String distributedObjectId) {
                        return "" + distributedObjectId;
                    }

                    @Override
                    protected Object createFrom(final Object source) {
                        return source;
                    }
                };

        synchronized (lock) {
            performTransactedAction(new AbstractTransactedAction("Could not add listener [" + listener.getClusterId()
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
                    addListenerIdFor(distObject, toAdd.getId());

                    // Add the listener internally, to enable unregistering later on.
                    locallyRegisteredListeners.put(toAdd.getId(), toAdd);

                    // ... and add the listener to the instance it should listen to...
                    if (distObject instanceof IMap) {
                        ((IMap) distObject).addEntryListener(toAdd, true);
                    } else if (distObject instanceof ICollection) {
                        ((ICollection) distObject).addItemListener(toAdd, true);
                    } else {

                        // We can't handle this type of distObject...
                        final Class<?>[] handleableTypes = {IMap.class, ICollection.class};

                        final StringBuilder permitted = new StringBuilder("[");
                        for (final Class<?> current : handleableTypes) {
                            permitted.append(current.getName()).append(", ");
                        }

                        throw new IllegalArgumentException("Will not add listener to an instance of type ["
                                + distObject.getClass().getName() + "]. Supported types are "
                                + permitted.substring(0, permitted.length() - 2) + "].");
                    }

                    /*
                    switch (distObject.getInstanceType()) {
                        case MAP:
                            ((IMap) distObject).addEntryListener(toAdd, true);
                            break;

                        case LIST:
                        case SET:
                        case QUEUE:
                            ((ICollection) distObject).addItemListener(toAdd, true);
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
                                    + distObject.getInstanceType().name() + "]. Supported types are "
                                    + permitted.substring(0, permitted.length() - 2) + "].");
                    }

                    */
                }
            });
        }

        // All done.
        return true;
    }

    /**
     * Removes the given CacheListener from the distributed object.
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

        final DistributedObject distObject = cast(distributedObject);

        // Is the cacheListenerId registered?
        final TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + distObject.getName());
        if (listenerIDs == null || !listenerIDs.contains(cacheListenerId)) {

            throw new IllegalStateException("(CacheID: " + getClusterId() + "): Listener [" + cacheListenerId
                    + "] not registered for instance [" + distObject.getName() + "] in Hazelcast.");
        }

        // All seems sane.
        // Send the message that removes the listener.
        sendAdminMessage(AdminMessage.createRemoveListenerMessage("" + distObject.getName(), cacheListenerId));
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
        final DistributedObject instance = cast(distributedObject);

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
    protected final boolean isLocallyRegistered(final CacheListener<String, Object> listener) {

        return listener != null && locallyRegisteredListeners.containsKey(listener.getClusterId());
    }

    /**
     * @return The Map of locally registered listeners.
     */
    protected Map<String, AbstractHazelcastCacheListenerAdapter> getLocallyRegisteredListeners() {
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
    public DistributedObject cast(final Object distributedObject) throws IllegalArgumentException {

        if (!(distributedObject instanceof DistributedObject)) {
            throw new IllegalArgumentException("(CacheID: " + getClusterId()
                    + "): Only DistributedObject objects can be "
                    + "distributed in Hazelcast. Class [" + distributedObject.getClass().getName()
                    + "] is not an instance.");
        }

        // Cast the distributedObject for type switching.
        return (DistributedObject) distributedObject;
    }

    /**
     * Retrieves all IDs of the CacheListeners bound to the given distributedObject Instance.
     *
     * @param distributedObject The distributedObject Instance whose CacheListener IDs we should retrieve.
     * @return The IDs of all CacheListener instances registered to the provided distributedObject.
     */
    protected final Set<String> getListenerIDsFor(final DistributedObject distributedObject) {

        final TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + distributedObject.getName());
        if (listenerIDs == null) {

            // This is not a distributed/replicated structure,
            // implying that it can only be returned READ-ONLY
            // to the calling API client.
            return new HashSet<String>();
        }

        return listenerIDs;
    }
}
