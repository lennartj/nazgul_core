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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.transaction.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.ReadOnlyIterator;
import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;
import se.jguru.nazgul.core.cache.api.distributed.UnsupportedDistributionException;
import se.jguru.nazgul.core.cache.api.distributed.async.DistributedExecutor;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.api.transaction.TransactedAction;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Abstract implementation managing registration and de-registration of HazelcastCacheListenerAdapter instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public abstract class AbstractHazelcastInstanceWrapper extends AbstractHazelcastCacheListenerManager
        implements MessageListener<AdminMessage>, DistributedExecutor<String, Object> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractHazelcastInstanceWrapper.class);

    // Constants
    private static final String EXECUTOR_NAME = "NazulCoreCacheImplHazelcast_Executor";

    // Internal state
    private HazelcastInstance cacheInstance;
    private static final int MESSAGE_WAIT_MILLIS = 100;

    /**
     * Creates a new AbstractHazelcastInstanceWrapper instance wrapping the provided HazelcastInstance
     * (which can be a HazelcastInstance [part of the cluster] or a HazelcastClient [not part of the cluster]).
     *
     * @param internalInstance The HazelcastInstance wrapped by this AbstractHazelcastInstanceWrapper.
     */
    protected AbstractHazelcastInstanceWrapper(final HazelcastInstance internalInstance) {
        super(internalInstance.getName());
        cacheInstance = internalInstance;

        // Create or acquire the cluster-wide shared collections.
        getAdminMessageTopic().addMessageListener(this);
        getOrCreateInClusterIMap(CLUSTER_SHARED_CACHE_MAP);
        getOrCreateInClusterIMap(CLUSTER_KNOWN_LISTENERIDS);
    }

    /**
     * Adds an instanceListener to the wrapped HazelcastInstance.
     *
     * @param listener The listener to add.
     * @return the HazelCast ID of the listener added.
     */
    public final String addInstanceListener(final CacheListener<String, Object> listener) {

        if (listener == null || isLocallyRegistered(listener)) {
            throw new IllegalArgumentException("Cannot add null or already registered listener");
        }

        synchronized (lock) {
            final StringKeyedHazelcastListenerAdapter<Object> wrapper
                    = new StringKeyedHazelcastListenerAdapter<Object>(listener);
            final String listenerId = cacheInstance.addDistributedObjectListener(wrapper);
            if (listenerId != null) {
                getLocallyRegisteredListeners().put(listener.getClusterId(), wrapper);
            } else {
                log.warn("Could not add DistributedObjectListener [" + wrapper.toString() + "]");
            }

            return listenerId;
        }
    }

    /**
     * Removes an instanceListener to the wrapped HazelcastInstance.
     *
     * @param listenerID The id of the listener to remove.
     * @return true if the listener was removed, and false otherwise.
     */
    public final boolean removeInstanceListener(final String listenerID) {

        if (listenerID == null || !getLocallyRegisteredListeners().keySet().contains(listenerID)) {
            return false;
        }

        synchronized (lock) {
            final boolean successfullyRemovedListener = cacheInstance.removeDistributedObjectListener(listenerID);
            if (successfullyRemovedListener) {
                getLocallyRegisteredListeners().remove(listenerID);
            }
            return successfullyRemovedListener;
        }
    }

    /**
     * Shuts down the local cacheInstance.
     * If you need to shut down all Hazelcast activities, simply use
     * <code>Hazelcast.shutdownAll()</code>.
     */
    public final void stopCache() {

        cacheInstance.getLifecycleService().shutdown();
        cacheInstance = null;
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
    public final void performTransactedAction(final TransactedAction action)
            throws UnsupportedOperationException {

        final TransactionContext trans = cacheInstance.newTransactionContext();
        trans.beginTransaction();

        try {

            // Perform the action, and commit.
            action.doInTransaction();
            trans.commitTransaction();

        } catch (final Exception ex) {

            // Whoops.
            trans.rollbackTransaction();

            // Perform custom rollback, if applicable.
            if (action instanceof AbstractTransactedAction) {
                ((AbstractTransactedAction) action).onRollback();
            }

            // Re-throw
            throw new IllegalStateException(action.getRollbackErrorDescription(), ex);
        }
    }

    /**
     * Returns true if this cache contains a mapping for the specified key
     *
     * @param key The <code>key</code> whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the specified key.
     */
    @Override
    public final boolean containsKey(final String key) {
        return getSharedMap().containsKey(key);
    }

    /**
     * @return The ExecutorService of the underlying cache implementation.
     */
    @Override
    public final ExecutorService getExecutorService() {
        return cacheInstance.getExecutorService(EXECUTOR_NAME);
    }

    /**
     * @return An identifier unique to the active cache cluster.
     */
    @Override
    public final String getClusterUniqueID() {
        return "" + cacheInstance.getIdGenerator("nazgul_hazelcast").newId();
    }

    /**
     * Retrieves an object from this Cache.
     *
     * @param key The key of the instance to retrieve.
     * @return The value corresponding to the provided key, or <code>null</code> if no object was found.
     */
    @Override
    public final Object get(final String key) {
        return getSharedMap().get(key);
    }

    /**
     * Stores the provided object in this Cache, associated with the provided key. Will overwrite existing objects with
     * identical key.
     *
     * @param key   The key under which to cache the provided value. This parameter should not be <code>null</code>.
     * @param value The value to cache.
     * @return The previous value associated with <code>key</code>, or <code>null</code> if no such object exists.
     */
    @Override
    public final Object put(final String key, final Object value) {
        return getSharedMap().put(key, getSerializable(value));
    }

    /**
     * Removes the object with the given key from the underlying cache implementation, returning the value held before
     * the object was removed.
     *
     * @param key The cache key for which the value should be removed.
     * @return The object to remove.
     */
    @Override
    public final Object remove(final String key) {
        return getSharedMap().remove(key);
    }

    /**
     * Gets a distributed collection with the given type and provided key from the cache. Note that the distributed
     * Collection will be created on the provided key if it does not already exist.
     * A typical usage example would be
     * <pre>
     *  List cachedList = getDistributedCollection(List.class, "someListKey");
     * </pre>
     *
     * @param type The type of Collection desired. This should be an abstract type, such as <code>List, Map, Set</code>
     *             etc.
     * @param key  The key where the distributed collection resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty) distributed collection cached at the given key.
     * @throws ClassCastException               if the cache key [key] maps to an object not of the given type.
     * @throws UnsupportedDistributionException if the underlying cache implementation could not support creating
     *                                          a distributed collection of the given type.
     */
    @Override
    public final Collection<? extends Serializable> getDistributedCollection(final DistributedCollectionType type,
                                                                             final String key)
            throws ClassCastException, UnsupportedDistributionException {

        switch (type) {
            case COLLECTION:
                return cacheInstance.getList(key);

            case SET:
                return cacheInstance.getSet(key);

            case QUEUE:
                return cacheInstance.getQueue(key);
        }

        // This should never happen.
        throw new UnsupportedDistributionException("Could not create collection of type [" + type + "]");
    }

    /**
     * Retrieves a LightweightTopic with the provided topicId from
     * the DestinationProvider. Depending on the capabilities of the underlying
     * implementation, the topic can be dynamically
     *
     * @param <MessageType> The type of message transmitted by this LightweightTopic.
     * @param topicId       The ID of the LightweightTopic to retrieve.
     * @return The LightweightTopic with the provided topicId.
     */
    @Override
    public final <MessageType extends Serializable> LightweightTopic<MessageType> getTopic(final String topicId) {
        final ITopic<MessageType> topic = cacheInstance.getTopic(topicId);
        return new HazelcastLightweightTopic<MessageType>(topic);
    }

    /**
     * @return The shared Map holding the default (direct-level) cached instances.
     */
    @Override
    public final IMap<String, Object> getSharedMap() {
        return cacheInstance.getMap(CLUSTER_SHARED_CACHE_MAP);
    }

    /**
     * @return The shared Map relating ids for distributed objects [String::key] to a List of ids for all registered
     * listeners to the given distributed object [List[String]::value].
     */
    @Override
    public final IMap<String, TreeSet<String>> getCacheListenersIDMap() {
        return cacheInstance.getMap(CLUSTER_KNOWN_LISTENERIDS);
    }

    /**
     * Sends the provided AdminMessage to all members of the Cluster.
     *
     * @param message The message to send.
     */
    @Override
    public final void sendAdminMessage(final AdminMessage message) {
        getAdminMessageTopic().publish(message);

        // Wait 0.1 seconds for the message to be echoed
        // to the cluster nodes before proceeding.
        //
        // This will provide a cushion for permitting sequential
        // messages interacting with the same distributed Instance
        // without too much of a race condition ...
        try {
            Thread.sleep(MESSAGE_WAIT_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The shared topic transmitting AdminMessage instances.
     */
    protected final ITopic<AdminMessage> getAdminMessageTopic() {
        return cacheInstance.getTopic(CLUSTER_ADMIN_TOPIC);
    }

    /**
     * Gets a distributed Map with the provided key from the cache. Note that the distributed Map
     * will be created on the provided key if it does not already exist.
     *
     * @param <K> The key type of the distributed Map returned.
     * @param <V> The value type of the distributed Map returned.
     * @param key The key where the distributed Map resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty) distributed Map cached at the given key.
     * @throws UnsupportedDistributionException if the underlying cache implementation could not support creating a distributed Map.
     */
    @Override
    public <K, V> Map<K, V> getDistributedMap(final String key) throws UnsupportedDistributionException {
        return cacheInstance.getMap(key);
    }

    /**
     * Adds the given listenerID to the listenerIdSet for the provided distributedObject.
     *
     * @param distributedObject The DistributedObject for which a listener ID should be registered.
     * @param listenerId        The id of the Listener to register to the provided distributedObject.
     * @return <code>true</code> if the registration was successful, and false otherwise.
     */
    @Override
    public boolean addListenerIdFor(final DistributedObject distributedObject, final String listenerId) {

        TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + distributedObject.getName());

        if (listenerIDs == null) {
            listenerIDs = new TreeSet<String>();
        }

        // Add the new listenerId, and update the listenersIdMap.
        // Remember, this is the lifecycle required for non-proxy stored collections in HC.
        listenerIDs.add(listenerId);
        getCacheListenersIDMap().put(distributedObject.getName(), listenerIDs);

        return true;
    }

    /**
     * Invoked when a message is received for the added topic.
     *
     * @param adminMessageMessage received message
     */
    @Override
    @SuppressWarnings(value = {"PMD.UnusedLocalVariable", "unchecked", "rawtypes"})
    public void onMessage(final Message<AdminMessage> adminMessageMessage) {

        final AdminMessage message = adminMessageMessage.getMessageObject();

        switch (message.getCommand()) {
            case REMOVE_LISTENER:

                final String distributedObjectiD = message.getArguments().get(0);
                final String toRemoveId = message.getArguments().get(1);

                // Take no action if we do not own the listener.
                // Only the member that owns the listener should remove it.
                if (!getLocallyRegisteredListeners().containsKey(toRemoveId)) {

                    log.debug("(CacheID: " + getClusterId() + "): No local registered listener with id [" + toRemoveId
                            + "] found. Ignoring remove request.");

                    return;
                }

                final String rollbackMessage =
                        "(CacheID: " + getClusterId() + "): Could not remove listener with id [" + toRemoveId
                                + "] from distributedObject [" + distributedObjectiD + "]";

                performTransactedAction(new AbstractTransactedAction(rollbackMessage) {

                    @SuppressWarnings({"incomplete-switch", "unused"})
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        DistributedObject distributedObject = null;

                        // Find the distributed object from which to remove the listener
                        for (final DistributedObject current : getInstances()) {
                            if (distributedObjectiD.equals("" + current.getName())) {
                                distributedObject = current;
                                break;
                            }
                        }

                        // Remove the listener locally.
                        final AbstractHazelcastCacheListenerAdapter removed
                                = getLocallyRegisteredListeners().remove(toRemoveId);

                        // Remove the listener ID from the listenersIdMap,
                        // and update the TreeSet within the distributedListenersIdMap.
                        final TreeSet<String> idSet = getCacheListenersIDMap().get(distributedObjectiD);
                        final boolean removedOKFromIdMap = idSet.remove(toRemoveId);
                        getCacheListenersIDMap().put(distributedObjectiD, idSet);

                        // Remove the listener from the distributedObject.
                        if (distributedObject instanceof IMap) {
                            ((IMap) distributedObject).removeEntryListener(toRemoveId);
                        } else if (distributedObject instanceof ICollection) {
                            ((ICollection) distributedObject).removeItemListener(toRemoveId);
                        } else {

                            // We can't handle this type of distObject...
                            final Class<?>[] handleableTypes = {IMap.class, ICollection.class};

                            final String distObjectType = distributedObject == null
                                    ? "<null>"
                                    : distributedObject.getClass().getName();
                            final StringBuilder permitted = new StringBuilder("[");
                            for (final Class<?> current : handleableTypes) {
                                permitted.append(current.getName()).append(", ");
                            }

                            throw new IllegalArgumentException("Will not add listener to an instance of type ["
                                    + distObjectType + "]. Supported types are "
                                    + permitted.substring(0, permitted.length() - 2) + "].");
                        }
                    }
                });

                break;

            case SHUTDOWN_INSTANCE:

                // Is it *this* instance that should be shut down?
                final String shutdownInstanceID = message.getArguments().get(0);
                if (!getClusterId().equals(shutdownInstanceID)) {
                    return;
                }

                // Unregister all keys for listeners that we own.
                for (final DistributedObject current : getInstances()) {

                    final Set<String> listenerIDs = getListenerIDsFor(current);
                    for (final String currentID : getLocallyRegisteredListeners().keySet()) {

                        // Just remove to save the extra processing in checking if the key exists.
                        listenerIDs.remove(currentID);
                    }
                }

                // Now perform shutdown.
                // This will automatically remove all local listeners from their instances.
                AbstractHazelcastInstanceWrapper.this.stopCache();
                break;

            case CREATE_INCACHE_INSTANCE:
                final AdminMessage.TypeDefinition toCreateType =
                        AdminMessage.TypeDefinition.valueOf(message.getArguments().get(0));
                final String clusterUniqueID = message.getArguments().get(1);

                switch (toCreateType) {

                    case SET:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.SET, clusterUniqueID);
                        break;

                    case COLLECTION:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.COLLECTION,
                                clusterUniqueID);
                        break;

                    case QUEUE:
                        getDistributedCollection(DistributedCache.DistributedCollectionType.QUEUE, clusterUniqueID);
                        break;

                    case TOPIC:
                        getTopic(clusterUniqueID);
                        break;

                    case MAP:
                        getDistributedMap(clusterUniqueID);
                        break;
                }
                break;

            default:
                throw new UnsupportedOperationException("AdminMessage command [" + message.getCommand()
                        + "] not yet supported.");
        }
    }

    /**
     * Retrieves a ReadOnlyIterator for the shared cache map of this AbstractHazelcastInstanceWrapper instance.
     */
    @Override
    public Iterator<String> iterator() {
        return new ReadOnlyIterator<String>(getSharedMap().keySet().iterator());
    }

    /**
     * Creates a serializable ISet instance within the cluster.
     *
     * @param clusterWideUniqueID The cluster-wide unique ID of the ISet to be created.
     * @return a serializable ISet instance, which is created by a HazelcastInstance member.
     */
    protected ISet<String> getOrCreateInClusterISet(final String clusterWideUniqueID) {
        return cacheInstance.getSet(clusterWideUniqueID);
    }

    /**
     * Creates a serializable IMap instance within the cluster.
     *
     * @param clusterWideUniqueID The cluster-wide unique ID of the ISet to be created.
     * @return a serializable ISet instance, which is created by a HazelcastInstance member.
     */
    protected IMap<String, Serializable> getOrCreateInClusterIMap(final String clusterWideUniqueID) {
        return cacheInstance.getMap(clusterWideUniqueID);
    }

    /**
     * @return A Collection holding the DistributedObjects known to the wrapped HazelcastInstance.
     */
    protected final Collection<DistributedObject> getInstances() {
        return cacheInstance.getDistributedObjects();
    }

    //
    // Private helpers
    //

    private Serializable getSerializable(final Object object) {

        if (object == null || object instanceof Serializable) {
            return (Serializable) object;
        }

        // This is a non-null object which is not Serializable.
        throw new IllegalArgumentException("Could not convert [" + object.getClass().getName() + "] to Serializable.");
    }
}
