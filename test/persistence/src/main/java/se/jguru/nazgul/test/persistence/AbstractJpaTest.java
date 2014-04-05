/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
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
package se.jguru.nazgul.test.persistence;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.persistence.classloader.PersistenceRedirectionClassLoader;
import se.jguru.nazgul.test.persistence.helpers.JpaPersistenceTestOperations;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for JPA based tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJpaTest {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractJpaTest.class);

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

    // Internal state
    private ClassLoader originalClassLoader;

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
        final Map<String, String> props = getEntityManagerFactoryProperties();

        if (log.isDebugEnabled()) {

            // Log the EntityManagerFactory properties used.
            final StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> current : props.entrySet()) {
                builder.append("  [").append(current.getKey()).append("]: ").append(current.getValue()).append("\n");
            }

            log.debug("Testcase " + getClass().getSimpleName() + " - EntityManagerFactoryProperties:\n"
                    + builder.toString() + "\n");
        }

        EntityManagerFactory factory = null;
        try {
            factory = Persistence.createEntityManagerFactory(getPersistenceUnitName(), props);
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
     * Retrieves/unwraps the SQL Connection used by the EntityManager.
     * Note that for some JPA implementations - notably EclipseLink - unwrapping a Connection
     * from the EntityManager will retrieve {@code null} unless the EntityManager is in
     * a transaction.
     *
     * @return The SQL Connection used by the entityManager, or {@code null} if no Connection could be retrieved.
     */
    protected final Connection getJpaUnitTestConnection() {
        return entityManager.unwrap(Connection.class);
    }

    /**
     * Override to supply any additional EntityManagerFactory properties.
     * The properties are supplied as the latter argument to the
     * {@code Persistence.createEntityManagerFactory} method.
     * <p/>
     * The properties supplied within this Map override property definitions
     * given in the persistence.xml file.
     *
     * @return Properties supplied to the EntityManagerFactory, implying they do not
     * need to be declared within the persistence.xml file.
     * @see Persistence#createEntityManagerFactory(String, java.util.Map)
     */
    protected Map<String, String> getEntityManagerFactoryProperties() {
        return new HashMap<String, String>();
    }

    /**
     * Tear down the JPA framework and rollback any started transactions.
     */
    @After
    public void tearDown() {

        try {
            // Clean up the test schema
            cleanupTestSchema();

            // rollback
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception e) {
                    throw new IllegalStateException("Could not rollback active EntityTransaction.", e);
                }
            }
            transaction = null;

            // close
            if (entityManager != null && entityManager.isOpen()) {
                try {
                    entityManager.close();
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
     */
    protected abstract void cleanupTestSchema();

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
     * Commits the currently active EntityTransaction, and starts a new EntityTransaction if so ordered.
     *
     * @param startNewTransaction if {@code true}, starts a new EntityTransaction following the commit of the
     *                            currently active one.
     */
    protected final void commit(final boolean startNewTransaction) {

        try {
            transaction.commit();
            transaction = entityManager.getTransaction();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a new EntityTransacion", e);
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
