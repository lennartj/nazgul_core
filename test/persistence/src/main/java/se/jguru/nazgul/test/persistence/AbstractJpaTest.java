/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.test.persistence;

import org.eclipse.persistence.jpa.rs.PersistenceContextFactory;
import org.eclipse.persistence.jpa.rs.PersistenceContextFactoryProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.persistence.classloader.PersistenceRedirectionClassLoader;
import se.jguru.nazgul.test.persistence.helpers.JpaPersistenceTestOperations;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Abstract superclass for JPA based tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJpaTest {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractJpaTest.class);

    /**
     * Convenience rule providing the currently executing test method name.
     */
    @Rule
    @SuppressWarnings("all")
    public TestName activeTestName = new TestName();

    /**
     * The unit-test scoped access to JpaOperations, created by this AbstractJpaTest.
     */
    protected PersistenceTestOperations jpa;

    /**
     * The currently active EntityTransaction.
     */
    protected EntityTransaction transaction;

    /**
     * The active JPA EntityManager.
     */
    protected EntityManager entityManager;

    /**
     * The active JPA persistence Cache.
     */
    protected Cache jpaCache;

    // Internal state
    private ClassLoader originalClassLoader;

    /**
     * This flag controls if the database should be shut down during the teardown method.
     */
    protected boolean shutdownDatabaseInTeardown = true;

    /**
     * Setting up the JPA framework which setup the Persistence Entity Manager.
     * The framework begins a database transaction before each Test.
     *
     * @throws Exception if an error occurred.
     */
    @Before
    public void setUp() throws Exception {

        originalClassLoader = getClass().getClassLoader();

        // Redirect the classloader
        final PersistenceRedirectionClassLoader redirectionClassLoader = new PersistenceRedirectionClassLoader(
                originalClassLoader, getPersistenceXmlFile());
        Thread.currentThread().setContextClassLoader(redirectionClassLoader);

        // Create a JpaPersistenceOperations and a corresponding Transaction.
        final SortedMap<String, String> props = getEntityManagerFactoryProperties();

        if (log.isDebugEnabled()) {

            // Log the EntityManagerFactory properties used.
            final StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> current : props.entrySet()) {
                builder.append("  [").append(current.getKey()).append("]: ").append(current.getValue()).append("\n");
            }

            log.debug("Test Class " + getClass().getSimpleName() + " - EntityManagerFactoryProperties:\n"
                    + builder.toString() + "\n");
        }

        EntityManagerFactory factory = null;
        try {
            factory = Persistence.createEntityManagerFactory(getPersistenceUnitName(), props);

            // Clean out the JPA cache, to prevent phantom reads from earlier tests.
            jpaCache = factory.getCache();
            jpaCache.evictAll();

            entityManager = factory.createEntityManager();
        } catch (Exception e) {

            // Could not create the entityManager
            final String factoryType = factory == null ? "<unknown>" : factory.getClass().getName();
            throw new IllegalStateException("Could not create EntityManager from factory of type ["
                    + factoryType + "]", e);
        }

        jpa = new JpaPersistenceTestOperations(entityManager);
        transaction = entityManager.getTransaction();
    }

    /**
     * Retrieves/unwraps the SQL Connection used by the EntityManager. Note that for some JPA implementations -
     * notably EclipseLink - unwrapping a Connection from the EntityManager will retrieve {@code null} unless the
     * EntityManager is in a transaction.
     * To avoid any followup problems caused by this, an IllegalStateException will be thrown unless a non-null
     * Connection can properly be retrieved.
     *
     * @param startTransactionIfRequired If the entityManager does not have an active EntityTransaction, and the
     *                                   startNewTransaction parameter is {@code true},
     *                                   the EntityManager's Transaction will be started. Otherwise,
     *                                   an Exception will be thrown.
     * @return The SQL Connection used by the entityManager.
     * @throws java.lang.IllegalStateException if {@code entityManager.getTransaction().isActive()} was {@code false}.
     */
    protected final Connection getJpaUnitTestConnection(final boolean startTransactionIfRequired)
            throws IllegalStateException {

        // Check sanity
        if (!entityManager.getTransaction().isActive()) {

            if (startTransactionIfRequired) {
                entityManager.getTransaction().begin();
            } else {
                throw new IllegalStateException("EclipseLink - and perhaps other JPA implementations - considers it "
                        + "an Exception to unwrap the DB Connection from the JPA EntityManager unless there is an "
                        + "active Transaction. Fix your testcase [" + getClass().getSimpleName()
                        + "], method [" + activeTestName.getMethodName() + "] to adhere to these mechanics.");
            }
        }

        // All done.
        final Connection toReturn = entityManager.unwrap(Connection.class);
        if (toReturn == null) {
            log.warn("EntityManager unwrapped a null JDBC Connection. Proceeding anyways; insane states may occur.");
        }
        return toReturn;
    }

    /**
     * Override to supply any additional EntityManagerFactory properties.
     * The properties are supplied as the latter argument to the
     * {@code Persistence.createEntityManagerFactory} method.
     * The properties supplied within this Map override property definitions
     * given in the persistence.xml file.
     *
     * @return Properties supplied to the EntityManagerFactory, implying they do not
     * need to be declared within the persistence.xml file.
     * @see Persistence#createEntityManagerFactory(String, java.util.Map)
     */
    protected SortedMap<String, String> getEntityManagerFactoryProperties() {
        return new TreeMap<String, String>();
    }

    /**
     * Tear down the JPA framework and rollback any started transactions.
     */
    @After
    public void tearDown() {

        try {
            // Clean up the test schema
            cleanupTestSchema(shutdownDatabaseInTeardown);

            // Ensure that EclipseLink actually reloads its persistence unit,
            // by ensuring that we always close the PersistenceContextFactoryProvider.
            //
            // As indicated in https://bugs.eclipse.org/bugs/show_bug.cgi?id=408015, this does not always happen.
            final ServiceLoader<PersistenceContextFactoryProvider> persistenceContextFactoryProviderLoader
                    = ServiceLoader.load(PersistenceContextFactoryProvider.class,
                    Thread.currentThread().getContextClassLoader());
            for (PersistenceContextFactoryProvider current : persistenceContextFactoryProviderLoader) {
                PersistenceContextFactory persistenceContextFactory = current.getPersistenceContextFactory(null);
                if (persistenceContextFactory != null) {
                    persistenceContextFactory.close();
                }
            }

            // rollback
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception e) {
                    throw new IllegalStateException("Could not rollback active EntityTransaction.", e);
                }
            }
            transaction = null;

            // Close the database, and the EntityManager
            if (entityManager != null && entityManager.isOpen()) {
                try {
                    entityManager.close();

                    if (log.isDebugEnabled()) {
                        log.debug("Closed EntityManager.");
                    }

                } catch (Exception e) {
                    throw new IllegalStateException("Could not close EntityManager.", e);
                }
            }
        } finally {

            // Restore ClassLoader
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * Invoked during teardown to clean up the schema used for test.
     *
     * @param shutdownDatabase if {@code true}, the database should be shutdown after cleaning the schema.
     */
    protected abstract void cleanupTestSchema(final boolean shutdownDatabase);

    /**
     * Retrieves the classpath-relative path to the persistence.xml file used in this AbstractJpaTest.
     *
     * @return classpath-relative path to the persistence.xml file used in this AbstractJpaTest.
     */
    protected abstract String getPersistenceXmlFile();

    /**
     * Retrieves the name of the PersistenceUnit, as defined within the Persistence.xml file
     * and also within @Persistence annotations in user classes.
     *
     * @return the name of the active PersistenceUnit.
     */
    protected abstract String getPersistenceUnitName();

    /**
     * Commits the currently active EntityTransaction, and starts a new EntityTransaction.
     *
     * @see #commit(boolean)
     */
    protected final void commitAndStartNewTransaction() {
        commit(true);
    }

    /**
     * Performs rollback on the currently active EntityTransaction, and starts a new EntityTransaction.
     *
     * @see #commit(boolean)
     */
    protected final void rollbackAndStartNewTransaction() {
        rollback(true);
    }

    /**
     * Performs rollback on the currently active EntityTransaction, and starts a new EntityTransaction
     * if told to do so.
     *
     * if {@code true}, starts a new EntityTransaction following the rollback of the currently active one.
     * @throws java.lang.IllegalStateException if the Transaction could not be rolled back or begun anew.
     */
    protected final void rollback(final boolean startNewTransaction) throws IllegalStateException {
        endTransactionAndStartAnother(false, startNewTransaction);
    }

    /**
     * Commits the currently active EntityTransaction, and starts a new EntityTransaction if so ordered.
     *
     * @param startNewTransaction if {@code true}, starts a new EntityTransaction following the commit of the
     *                            currently active one.
     * @throws java.lang.IllegalStateException if the Transaction could not be committed or begun anew.
     */
    protected final void commit(final boolean startNewTransaction) throws IllegalStateException {
        endTransactionAndStartAnother(true, startNewTransaction);
    }

    //
    // Private helpers
    //

    private void endTransactionAndStartAnother(final boolean commitActiveTransaction,
                                               final boolean startNewTransaction)
        throws IllegalStateException {

        try {

            // Either commit or rollback the transaction
            if(commitActiveTransaction) {
                transaction.commit();
            } else {
                transaction.rollback();
            }

            // Create a new EntityTransaction
            transaction = entityManager.getTransaction();

        } catch (Exception e) {
            throw new IllegalStateException("Could not create a new EntityTransaction", e);
        }

        if (startNewTransaction) {
            try {
                transaction.begin();
            } catch (Exception e) {
                throw new IllegalStateException("Could not begin() the newly created EntityTransaction.", e);
            }
        }
    }
}
