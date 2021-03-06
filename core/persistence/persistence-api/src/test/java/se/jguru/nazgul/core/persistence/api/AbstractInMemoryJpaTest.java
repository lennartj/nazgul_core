/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
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

package se.jguru.nazgul.core.persistence.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractInMemoryJpaTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractInMemoryJpaTest.class);

    /**
     * System property defining the JPA provider class name.
     */
    public static final String JPA_SPECIFICATION_PROPERTY = "jpa_provider_class";

    /**
     * The name of the PersistenceUnit, used in all unit tests derived from this AbstractInMemoryJpaTest.
     */
    protected static final String UNITTEST_PERSISTENCE_UNIT = "InmemoryPU";

    @Rule
    public TestName testName = new TestName();

    // Internal state
    protected ClassLoader originalClassLoader;
    protected EntityManager unitTestEM;
    protected EntityTransaction trans;
    protected JpaPersistenceOperations unitUnderTest;
    protected String persistenceXmlFile;
    protected String jpaPersistenceProviderClass;

    @Before
    public void setupSharedState() throws Exception {

        log.info("Launching setup for [" + testName.getMethodName() + "]");

        // Debug somewhat
        printSystemProperties();

        // Find the Persistence XML file.
        persistenceXmlFile = "testdata/" + getPersistenceFileName() + ".xml";
        log.debug("Using PersistenceXmlFile: " + persistenceXmlFile);

        // Stash the original ClassLoader; create the PersistenceRedirectionClassLoader
        originalClassLoader = getClass().getClassLoader();
        final PersistenceRedirectionClassLoader redirectionClassLoader =
                new PersistenceRedirectionClassLoader(getClass().getClassLoader(), persistenceXmlFile);

        // Find the JPA persistence provider class.
        jpaPersistenceProviderClass = getJpaPersistenceProviderClass(originalClassLoader, redirectionClassLoader);

        // Assign the PersistenceRedirectionClassLoader as the Context ClassLoader.
        try {
            Thread.currentThread().setContextClassLoader(redirectionClassLoader);
        } catch (Throwable e) {
            throw new IllegalStateException("Could not assign the Thread Context ClassLoader", e);
        }

        // Create EntityManager and Transaction.
        unitTestEM = getEntityManager(UNITTEST_PERSISTENCE_UNIT);
        unitUnderTest = new JpaPersistenceOperations(unitTestEM);
        trans = unitTestEM.getTransaction();
        trans.begin();

        log.debug("EntityManager of type [" + unitTestEM.getClass().getCanonicalName()
                + "] created. Transaction is active: " + trans.isActive()
                + ". Delegating to custom setup.");

        // Delegate
        doCustomSetup();
    }

    @After
    public final void teardownSharedState() throws Exception {

        // First, delegate
        doCustomTeardown();

        // Restore the original ClassLoader
        try {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        } catch (Throwable thr) {
            System.err.println("Could not restore original ClassLoader [" + originalClassLoader + "]");
            thr.printStackTrace();
        }
    }

    protected abstract String getPersistenceFileName();

    protected void doCustomTeardown() {
        // Do nothing.
    }

    protected void doCustomSetup() {
        // Do nothing.
    }

    protected final void dropDbTable(final String tableName) {

        // Check sanity
        if (!trans.isActive()) {
            trans.begin();
        }

        try {

            final String tableVerificationSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_NAME = '" + tableName + "'";
            final String dropTableSql = "DROP TABLE IF EXISTS '" + tableName + "' CASCADE ;";

            //
            // #1) Validate that the table actually exists.
            //
            final Query verificationQ = unitTestEM.createNativeQuery(tableVerificationSql);
            final List resultList = verificationQ.getResultList();
            if (resultList.size() == 1) {

                final Query dropQ = unitTestEM.createNativeQuery(dropTableSql);
                int affectedRows = dropQ.executeUpdate();
                log.info("Removed table. Rows affected [" + affectedRows + "]");

                if (trans.getRollbackOnly()) {
                    trans.rollback();
                } else {
                    trans.commit();
                }
            }
        } catch (final Exception e) {
            log.info("Could not commit the DB Transaction.", e);
        }
    }

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
            trans.commit();
            trans = unitTestEM.getTransaction();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a new EntityTransacion", e);
        }

        if (startNewTransaction) {
            try {
                trans.begin();
            } catch (Exception e) {
                throw new IllegalStateException("Could not begin() the newly created EntityTransaction.", e);
            }
        }
    }

    /**
     * Acquires the className of the JPA persistence provider. Normally retrieves it from the value of the System
     * property defined within the {@code JPA_SPECIFICATION_PROPERTY}.
     *
     * @param originalClassLoader    The original ClassLoader used.
     * @param redirectionClassLoader The redirectionClassLoader not yet set when invoking this method.
     * @return A class name of the JPA Persistence Provider.
     * @see #JPA_SPECIFICATION_PROPERTY
     */
    protected String getJpaPersistenceProviderClass(final ClassLoader originalClassLoader,
                                                    final ClassLoader redirectionClassLoader) {

        // Find the JPA specification
        String candidate = System.getProperty(JPA_SPECIFICATION_PROPERTY);
        Assert.assertNotNull("The persistence provider System property [" + JPA_SPECIFICATION_PROPERTY + "] should "
                        + "be set to contain the JPA persistence provider. This is normally done in a Maven profile.",
                candidate);

        // All done.
        return candidate;
    }

    //
    // Private helpers
    //

    private EntityManager getEntityManager(final String persistenceUnitName) {

        // Add
        final Map<String, String> extraProperties = getEntityManagerFactoryProperties();
        extraProperties.put("eclipselink.persistencexml", persistenceXmlFile);

        final StringBuilder builder = new StringBuilder(" EntityManagerFactory properties\n");
        for (Map.Entry<String, String> current : extraProperties.entrySet()) {
            builder.append("  [").append(current.getKey()).append("]: ").append(current.getValue()).append("\n");
        }
        log.debug(builder.toString());

        // Create an EntityManager factory.
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, extraProperties);
        return emf.createEntityManager();
    }

    private void printSystemProperties() {

        // Debug somewhat
        final SortedMap<String, String> sysProps = new TreeMap<>();
        final Properties props = System.getProperties();
        for (String current : props.stringPropertyNames()) {
            sysProps.put(current, System.getProperty(current));
        }

        try {
            final List<URL> cpList = Collections.list(Thread.currentThread().getContextClassLoader().getResources(""));
            for (int i = 0; i < cpList.size(); i++) {
                log.info("[" + i + "]: " + cpList.get(i).toString());
            }
        } catch (Exception e) {
            log.error("Could not harvest classpath: " + e);
        }

        StringBuilder builder = new StringBuilder(" ===== [System Properties] ===== \n");
        for (Map.Entry<String, String> current : sysProps.entrySet()) {
            String value = "[" + current.getKey() + "]: " + current.getValue();
            builder.append(value).append("\n");
        }
        builder.append(" ===== [End System Properties] ===== \n");
        final String result = builder.toString();

        log.info(result);
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
     * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
     */
    protected Map<String, String> getEntityManagerFactoryProperties() {

        // Get standard properties
        // final String jdbcURL = getDatabaseType().getUnitTestJdbcURL(DEFAULT_DB_NAME, getTargetDirectory());
        // final String persistenceProviderClass = getPersistenceProviderType().getPersistenceProviderClass();

        final Map<String, String> toReturn = new TreeMap<String, String>();

        final String[][] persistenceProviderProps = new String[][]{

                // Generic properties
                {"javax.persistence.provider", jpaPersistenceProviderClass},
                {"javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver"},
                {"javax.persistence.jdbc.url", "jdbc:hsqldb:mem:unittestDatabaseID"},
                {"javax.persistence.jdbc.user", "sa"},
                {"javax.persistence.jdbc.password", ""},

                // OpenJPA properties
                {"openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.HSQLDictionary"},

                // Note!
                // These OpenJPA provider properties are now replaced by the standardized
                // properties, "javax.persistence....".
                // It is now an exception to define both the standardized property and the
                // corresponding legacy openjpa property for the commented-out properties
                // below. These properties will remain commented-out to indicate which openjpa
                // properties are now replaced by javax.persistence properties.
                //
                // {"openjpa.ConnectionDriverName", jdbcDriverClass},
                // {"openjpa.ConnectionURL", jdbcURL},
                // {"openjpa.ConnectionUserName", DEFAULT_DB_UID},
                // {"openjpa.ConnectionPassword", DEFAULT_DB_PASSWORD},
                {"openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"},
                {"openjpa.InverseManager", "true"},
                //
                // Valid values for log levels are:
                // TRACE, INFO, WARN, ERROR or FATAL.
                //
                // That is ... "DEBUG" is not among them.
                //
                // {"openjpa.Log", "DefaultLevel=TRACE, Tool=TRACE, RUNTIME=TRACE, SQL=INFO"},
                {"openjpa.Log", "slf4j"},
                {"openjpa.jdbc.SchemaFactory", "native(ForeignKeys=true)"},
                {"openjpa.jdbc.TransactionIsolation", "serializable"},
                {"openjpa.RuntimeUnenhancedClasses", "supported"},
                {"openjpa.DynamicEnhancementAgent", "true"},

                // Eclipselink properties
                // {"eclipselink.target-database", getDatabaseType().getHibernatePlatformClass()},
                {"eclipselink.logging.level", "FINER"},
                {"eclipselink.orm.throw.exceptions", "true"},
                {"eclipselink.ddl-generation", "drop-and-create-tables"},
                {"eclipselink.ddl-generation.output-mode", "database"},
                // {"eclipselink.persistencexml", getPersistenceXmlFile()}
        };

        // First, add the default values - and then overwrite them with
        // any given system properties.
        for (String[] current : persistenceProviderProps) {
            toReturn.put(current[0], current[1]);
        }

        // Now, overwrite with appropriate system properties
        final List<String> overridablePrefixes = Arrays.asList("javax.persistence.", "openjpa.", "eclipselink.");
        for (Map.Entry<Object, Object> current : System.getProperties().entrySet()) {

            final String currentPropertyName = "" + current.getKey();
            for (String currentPrefix : overridablePrefixes) {
                if (currentPropertyName.trim().toLowerCase().startsWith(currentPrefix)) {
                    toReturn.put(currentPropertyName, "" + current.getValue());
                }
            }
        }

        // All done.
        return toReturn;
    }
}
