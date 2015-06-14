/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
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

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.TransactionController;
import net.sf.ehcache.transaction.manager.TransactionManagerLookup;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.cache.api.Cache;
import se.jguru.nazgul.core.cache.api.CacheListener;
import se.jguru.nazgul.core.cache.api.ReadOnlyIterator;
import se.jguru.nazgul.core.cache.api.transaction.AbstractTransactedAction;
import se.jguru.nazgul.core.cache.api.transaction.TransactedAction;
import se.jguru.nazgul.core.clustering.api.AbstractClusterable;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Nazgul cache api implementation backed by a transacted EhCache with Strings as CacheKeys.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class NonDistributedEhCache extends AbstractClusterable implements Cache<String, Serializable> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(NonDistributedEhCache.class);

    // Internal state
    private net.sf.ehcache.Ehcache cacheInstance;
    private CacheManager cacheManager;
    private TransactionManager tx;
    private TransactionController localTx;

    private Map<String, EhCacheListenerAdapter> locallyRegisteredListeners = new TreeMap<String, EhCacheListenerAdapter>();

    /**
     * Creates an LocalEhCache instance using the provided classpath-relative configuration file.
     *
     * @param classpathRelativeConfigurationFile The LocalEhCache configuration file.
     */
    public NonDistributedEhCache(final String classpathRelativeConfigurationFile) {

        // Initialize our ID.
        super(new EhCacheClusterIdGenerator());

        // Acquire the cacheManager
        cacheManager = getCacheManager(classpathRelativeConfigurationFile);
        final EhCacheClusterIdGenerator idGenerator = (EhCacheClusterIdGenerator) getIdGenerator();
        idGenerator.setCacheManager(cacheManager);

        // Perform common initialization.
        initialize();
    }

    /**
     * Creates an LocalEhCache instance from the provided CacheManager instance.
     *
     * @param manager The Manager used to create the LocalEhCache instance.
     */
    public NonDistributedEhCache(final CacheManager manager) {

        // Initialize our ID.
        super(new EhCacheClusterIdGenerator());

        // Check sanity
        Validate.notNull(manager, "Cannot handle null manager argument.");

        // Assign internal state
        cacheManager = manager;
        final EhCacheClusterIdGenerator idGenerator = (EhCacheClusterIdGenerator) getIdGenerator();
        idGenerator.setCacheManager(cacheManager);

        // Perform common initialization.
        initialize();
    }

    /**
     * Initializes this LocalEhCache. Override in subclasses to provide
     * custom initialization - but <strong>do not forget to call super.initialize()</strong>.
     */
    protected void initialize() {

        // Create the local-instance cache.
        if (cacheInstance == null) {
            cacheManager.addCacheIfAbsent("nonDistributedCache");
            cacheInstance = cacheManager.getCache("nonDistributedCache");
        }

        // Find the transactionLookup for the cacheInstance.
        TransactionManagerLookup transactionLookup =
                ((net.sf.ehcache.Cache) cacheInstance).getTransactionManagerLookup();
        if (transactionLookup != null) {
            tx = transactionLookup.getTransactionManager();
        }

        if (tx == null) {
            // We must revert to LocalTransactions
            localTx = cacheManager.getTransactionController();
        }
    }

    /**
     * @return The wrapped EhCache instance.
     */
    protected final Ehcache getCacheInstance() {
        return cacheInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<String> iterator() {

        // This mode of implementation is required since EhCache is stupid
        // enough to require that *all* operations in a transacted cache is
        // executed within transaction boundaries.

        final String errorMessage = "Could not retrieve cache keySet";
        final List<String> keys = new ArrayList<String>();

        AbstractElementReferenceTransactedAction getAction =
                new AbstractElementReferenceTransactedAction(errorMessage) {
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        for (Object current : cacheInstance.getKeys()) {
                            keys.add("" + current);
                        }
                    }
                };
        performTransactedAction(getAction);

        // All done.
        return new ReadOnlyIterator<String>(keys.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable get(final String key) {

        // This mode of implementation is required since EhCache is stupid
        // enough to require that *all* operations in a transacted cache is
        // executed within transaction boundaries.

        final String errorMessage = "Could not acquire the Element for key [" + key + "]";
        AbstractElementReferenceTransactedAction getAction =
                new AbstractElementReferenceTransactedAction(errorMessage) {
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        setAffectedElement(cacheInstance.get(key));
                    }
                };
        performTransactedAction(getAction);

        Element toReturn = getAction.getAffectedElement();
        if (toReturn == null) {
            return toReturn;
        }

        return (Serializable) toReturn.getObjectValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable put(final String key, final Serializable value) {

        // This mode of implementation is required since EhCache is stupid
        // enough to require that *all* operations in a transacted cache is
        // executed within transaction boundaries.

        final String errorMessage = "Could not assign [" + value + "] to key [" + key + "]";
        AbstractElementReferenceTransactedAction putAction =
                new AbstractElementReferenceTransactedAction(errorMessage) {
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        setAffectedElement(cacheInstance.get(key));
                        cacheInstance.put(new Element(key, value));
                    }
                };
        performTransactedAction(putAction);

        Element toReturn = putAction.getAffectedElement();
        if (toReturn == null) {
            return toReturn;
        }

        // All done.
        return (Serializable) toReturn.getObjectValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable remove(final String key) {


        // This mode of implementation is required since EhCache is stupid
        // enough to require that *all* operations in a transacted cache is
        // executed within transaction boundaries.

        final String errorMessage = "Could not remove Element for key [" + key + "]";
        AbstractElementReferenceTransactedAction removeAction =
                new AbstractElementReferenceTransactedAction(errorMessage) {
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        setAffectedElement(cacheInstance.get(key));
                        cacheInstance.remove(key);
                    }
                };
        performTransactedAction(removeAction);

        Element toReturn = removeAction.getAffectedElement();
        if (toReturn == null) {
            return toReturn;
        }

        // All done.
        return (Serializable) toReturn.getObjectValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addListener(final CacheListener<String, Serializable> listener) {

        // Wrap the CacheListener inside an EhCacheListenerAdapter.
        final EhCacheListenerAdapter toAdd = new EhCacheListenerAdapter(listener);

        if (locallyRegisteredListeners.containsKey(toAdd.getId())) {
            if (log.isWarnEnabled()) {
                EhCacheListenerAdapter alreadyRegistered
                        = locallyRegisteredListeners.get(toAdd.getId());
                log.warn("Already registered listener [" + alreadyRegistered.getId()
                        + "] holding CacheListener of type ["
                        + alreadyRegistered.getCacheListener().getClass().getName()
                        + "]. Aborting registration.");
            }

            // We will not replace the current listener.
            return false;
        }


        final String errMsg = "Could not add listener [" + listener.getClusterId() + "] of type ["
                + listener.getClass().getName() + "]";

        performTransactedAction(
                new AbstractTransactedAction(errMsg) {
                    @Override
                    public void doInTransaction() throws RuntimeException {
                        // Add to both the shared map and our internal state.
                        locallyRegisteredListeners.put(toAdd.getId(), toAdd);
                        cacheInstance.getCacheEventNotificationService().registerListener(toAdd);
                    }
                });

        // All done.
        return true;
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
    public List<String> getListenerIds() {
        return Collections.unmodifiableList(new ArrayList<String>(locallyRegisteredListeners.keySet()));
    }

    /**
     * Removes the CacheListener with the given key. <strong>This operation may be an asynchronous operation
     * depending on the underlying cache implementation.</strong>
     *
     * @param key The unique identifier for the given CacheListener to remove from operating on this Cache.
     */
    @Override
    public void removeListener(final String key) {

        // Acquire the Cachelistener with the given key.
        if (locallyRegisteredListeners.keySet().contains(key)) {

            final EhCacheListenerAdapter listener = locallyRegisteredListeners.get(key);

            final String errMsg = "Could not remove listener [" + listener.getId() + "] of type ["
                    + listener.getClass().getName() + "]";

            performTransactedAction(new AbstractTransactedAction(errMsg) {
                @Override
                public void doInTransaction() throws RuntimeException {

                    // Remove the CacheListenerAdapter and return its CacheListener.
                    EhCacheListenerAdapter removed = locallyRegisteredListeners.remove(key);
                    cacheInstance.getCacheEventNotificationService().unregisterListener(removed);
                }
            });

            return;
        }

        // The cacheListener was not found.
        if (log.isWarnEnabled()) {
            log.warn("CacheListener with id [" + key + "] was not locally registered.");
        }
    }

    /**
     * Returns true if this cache contains a mapping for the specified key
     *
     * @param key The <code>key</code> whose presence in this map is to be tested.
     * @return <code>true</code> if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(final String key) {

        // This mode of implementation is required since EhCache is stupid
        // enough to require that *all* operations in a transacted cache is
        // executed within transaction boundaries.

        final String errorMessage = "Could not acquire the Element for key [" + key + "]";
        AbstractElementReferenceTransactedAction containsKeyAction =
                new AbstractElementReferenceTransactedAction(errorMessage) {
                    @Override
                    public void doInTransaction() throws RuntimeException {

                        setAffectedElement(cacheInstance.get(key));
                    }
                };
        performTransactedAction(containsKeyAction);

        // All done.
        Element toReturn = containsKeyAction.getAffectedElement();
        return toReturn != null;
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

        if (tx == null) {

            // We are not participating in JTA or Appserver Transactions.
            // Use a LocalTransaction instance.
            try {
                localTx.begin();
                action.doInTransaction();
                localTx.commit();
            } catch (Exception ex) {

                // Whoops.
                log.error("(LocalTransaction) " + action.getRollbackErrorDescription(), ex);

                try {
                    localTx.rollback();
                } catch (Exception e) {
                    log.error("LocalTransaction Rollback failure", e);
                }
            }

        } else {

            try {
                tx.begin();
                action.doInTransaction();
                tx.commit();
            } catch (Exception ex) {

                // Whoops.
                log.error(action.getRollbackErrorDescription(), ex);

                try {
                    tx.rollback();
                } catch (SystemException e) {
                    log.error("Rollback failure", e);
                }
            }
        }
    }

    /**
     * Retrieves a CacheManager, using the configuration file located at the
     * classpathRelativeConfigurationFile, read by the classloader of the
     * EhCacheUtil class.
     *
     * @param classpathRelativeConfigurationFile an ehCache configuration file, i.e.
     *                                           read like "config/ehcache/someFile.xml"
     * @return An instance CacheManager.
     */
    public static CacheManager getCacheManager(final String classpathRelativeConfigurationFile) {
        final URL config = NonDistributedEhCache.class.getClassLoader().getResource(classpathRelativeConfigurationFile);
        return new CacheManager(config);
    }

    /**
     * Kills the CacheManager of the provided LocalEhCache.
     *
     * @param toShutDown the LocalEhCache whose CacheManager should be shut down.
     */
    public static void shutdownCache(final NonDistributedEhCache toShutDown) {
        toShutDown.getCacheInstance().getCacheManager().shutdown();
    }

    /**
     * Abstract TransactedAction skeleton implementation providing means to get/set an
     * Element to be returned from the operation or assigned/used by the operation.
     */
    abstract class AbstractElementReferenceTransactedAction extends AbstractTransactedAction {

        // Hold the Element to be returned
        private Element affectedElement;

        /**
         * Creates a new AbstractElementReferenceTransactedAction with the provided message
         * to be logged on rollback / transaction failure.
         *
         * @param rollbackErrorMessage An exception message logged if the TransactedAction failed.
         */
        protected AbstractElementReferenceTransactedAction(final String rollbackErrorMessage) {
            super(rollbackErrorMessage);
        }

        /**
         * @return The affected/returned element of the TransactedAction.
         */
        public final Element getAffectedElement() {
            return affectedElement;
        }

        /**
         * Assigns the affected Element of this TransactedAction.
         *
         * @param affectedElement the affected Element of this TransactedAction.
         */
        public void setAffectedElement(final Element affectedElement) {
            this.affectedElement = affectedElement;
        }
    }
}
