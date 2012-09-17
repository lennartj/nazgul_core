/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Instance;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.Transaction;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.distributed.UnsupportedDistributionException;
import se.jguru.nazgul.core.cache.api.distributed.async.DistributedExecutor;
import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopic;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.api.transaction.TransactedAction;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.AdminMessage;
import se.jguru.nazgul.core.cache.impl.hazelcast.grid.DataSerializableAdapter;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Abstract implementation managing registration and deregistration of HazelcastCacheListenerAdapter instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHazelcastInstanceWrapper extends AbstractHazelcastCacheListenerManager
        implements MessageListener<AdminMessage>, DistributedExecutor<String> {

    // Internal state
    private HazelcastInstance cacheInstance;
    private static final int messageWaitMillis = 100;

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
     */
    public final void addInstanceListener(final CacheListener listener) {

        if (listener == null || isLocallyRegistered(listener)) {
            throw new IllegalArgumentException("Cannot add null or already registered listener");
        }

        synchronized (lock) {
            final HazelcastCacheListenerAdapter wrapper = new HazelcastCacheListenerAdapter(listener);
            getLocallyRegisteredListeners().put(listener.getId(), wrapper);
            cacheInstance.addInstanceListener(wrapper);
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
            cacheInstance.removeInstanceListener(getLocallyRegisteredListeners().remove(listenerID));
        }

        return true;
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

        final Transaction trans = cacheInstance.getTransaction();
        trans.begin();

        try {

            // Perform the action, and commit.
            action.doInTransaction();
            trans.commit();

        } catch (final Exception ex) {

            // Whoops.
            trans.rollback();

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
        return cacheInstance.getExecutorService();
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
    public final Serializable get(final String key) {
        return extractExternalizableIfWrapped(getSharedMap().get(key));
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
    public final Serializable put(final String key, final Serializable value) {

        if (value instanceof Externalizable) {
            // Wrap in Dataserializable
            return getSharedMap().put(key, new DataSerializableAdapter((Externalizable) value));
        }

        // Fall through.
        return getSharedMap().put(key, value);
    }

    /**
     * Removes the object with the given key from the underlying cache implementation, returning the value held before
     * the object was removed.
     *
     * @param key The cache key for which the value should be removed.
     * @return The object to remove.
     */
    @Override
    public final Serializable remove(final String key) {
        return extractExternalizableIfWrapped(getSharedMap().remove(key));
    }

    /**
     * Gets a distributed collection with the given type and provided key from the cache. Note that the distributed
     * Collection will be created on the provided key if it does not already exist.
     * <p/>
     * A typical usage example would be
     * <p/>
     * <p/>
     * <pre>
     *  List cachedList = getDistributedCollection(List.class, "someListKey");
     * </pre>
     *
     * @param type The type of Collection desired. This should be an abstract type, such as <code>List, Map, Set</code>
     *             etc.
     * @param key  The key where the distributed collection resides or will be created to.
     * @return The created (empty) or acquired (potentially not empty) distributed collection cached at the given key.
     * @throws ClassCastException if the cache key [key] maps to an object not of the given type.
     * @throws UnsupportedDistributionException
     *                            if the underlying cache implementation could not support creating
     *                            a distributed collection of the given type.
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
    public final IMap<String, Serializable> getSharedMap() {
        return cacheInstance.getMap(CLUSTER_SHARED_CACHE_MAP);
    }

    /**
     * @return The shared Map relating ids for distributed objects [String::key] to a List of ids for all registered
     *         listeners to the given distributed object [List[String]::value].
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
            Thread.sleep(messageWaitMillis);
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
     * @throws UnsupportedDistributionException
     *          if the underlying cache implementation could not support creating a distributed Map.
     */
    @Override
    public final <K extends Serializable, V extends Serializable> Map<K, V> getDistributedMap(final String key)
            throws UnsupportedDistributionException {
        return cacheInstance.getMap(key);
    }

    /**
     * Adds the given listenerID to the listenerIdSet for the provided distributedObject.
     *
     * @param distributedObject The instance for which a listener ID should be registered.
     * @param listenerId        The id of the Listener to register to the provided distributedObject.
     * @return <code>true</code> if the registration was successful, and false otherwise.
     */
    @Override
    public boolean addListenerIdFor(final Instance distributedObject, final String listenerId) {

        TreeSet<String> listenerIDs = getCacheListenersIDMap().get("" + distributedObject.getId());

        if (listenerIDs == null) {
            listenerIDs = new TreeSet<String>();
        }

        // Add the new listenerId, and update the listenersIdMap.
        // Remember, this is the lifecycle required for non-proxy stored collections in HC.
        listenerIDs.add(listenerId);
        getCacheListenersIDMap().put("" + distributedObject.getId(), listenerIDs);

        return true;
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
     * @return A Collection holding the (distributed) Instances known
     *         to the wrapped HazelcastInstance.
     */
    protected final Collection<Instance> getInstances() {
        return cacheInstance.getInstances();
    }

    //
    // Private helpers.
    //

    private Serializable extractExternalizableIfWrapped(final Serializable serializable) {

        // Check sanity
        if (serializable == null) {
            return null;
        }

        // If this is a DataSerializableAdapter, extract its content.
        if (serializable instanceof DataSerializableAdapter) {
            return ((DataSerializableAdapter) serializable).getWrappedExternalizable();
        }

        // All done.
        return serializable;
    }
}