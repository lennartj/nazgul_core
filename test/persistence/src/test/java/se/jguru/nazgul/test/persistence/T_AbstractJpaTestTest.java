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

import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.persistence.pets.Bird;
import sun.util.locale.provider.SPILocaleProviderAdapter;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractJpaTestTest {

    // Shared state
    private ClassLoader originalClassLoader;
    private String jpaProviderClass;
    private DatabaseType dbType;

    @Before
    public void setupSharedState() {

        // Stash the original ClassLoader
        final Thread activeThread = Thread.currentThread();
        originalClassLoader = activeThread.getContextClassLoader();

        // Ensure that we have a correct Maven profile set.
        jpaProviderClass = System.getProperty(StandardPersistenceTest.JPA_PROVIDER_CLASS_SYSPROPKEY);
        Assert.assertNotNull("No JPA provider found. Ensure that system property ["
                        + StandardPersistenceTest.JPA_PROVIDER_CLASS_SYSPROPKEY + "] contains the JPA provider class. "
                        + "This should be done automatically by Maven.",
                jpaProviderClass);

        dbType = DatabaseType.HSQL;
    }

    @After
    public void teardownSharedState() {

        // Restore the original classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void validateStandardLifecycle() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.emProperties = getHsqldbEntityManagerFactoryProperties(persistenceXmlFile);

        final Bird bird = new Bird("birdName", "cool birds");

        // Act & Assert #1: Validate the entitymanager properties
        unitUnderTest.setUp();
        Assert.assertNotNull(unitUnderTest.entityManager);
        Assert.assertNotNull(unitUnderTest.jpa);
        Assert.assertNotNull(unitUnderTest.getJpaUnitTestConnection());

        EntityTransaction userTransaction = unitUnderTest.transaction;
        Assert.assertNotNull(userTransaction);
        Assert.assertFalse(userTransaction.isActive());

        // Act & Assert #2: Begin the user transaction
        final int firstTransactionHashCode = unitUnderTest.hashCode();
        userTransaction.begin();
        Assert.assertTrue(userTransaction.isActive());

        // Act & Assert #3: Persist a Bird instance, and commit without starting a new transaction.
        unitUnderTest.entityManager.persist(bird);
        unitUnderTest.commitAndStartNewTransaction();
        userTransaction = unitUnderTest.transaction;
        Assert.assertNotSame(firstTransactionHashCode, userTransaction.hashCode());

        // Act & Assert #4: Teardown
        unitUnderTest.tearDown();
        Assert.assertNull(unitUnderTest.transaction);
    }

    @Test
    public void validateTransactionlessReadDoesNotYieldException() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.emProperties = getHsqldbEntityManagerFactoryProperties(persistenceXmlFile);

        // Act & Assert #1: Close transaction, and check state
        unitUnderTest.setUp();
        unitUnderTest.transaction.begin();
        unitUnderTest.commit(false);
        Assert.assertFalse(unitUnderTest.transaction.isActive());

        // Act & Assert #2:
        final Query query = unitUnderTest.entityManager.createQuery("select b from Bird b order by b.name");
        final List<Bird> birds = query.getResultList();
        Assert.assertNotNull(birds);
        Assert.assertEquals(0, birds.size());

        Assert.assertFalse(unitUnderTest.entityManager.getTransaction().isActive());
        Assert.assertSame(unitUnderTest.transaction, unitUnderTest.entityManager.getTransaction());
    }

    @Test
    public void validateNoExceptionOnTeardownWithoutSetup() {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.cleanupSchemaInTeardown = false;

        // Act & Assert
        unitUnderTest.tearDown();
    }

    @Test
    public void validateFireQuery() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.emProperties = getHsqldbEntityManagerFactoryProperties(persistenceXmlFile);

        final Bird bird = new Bird("birdName", "cool birds");

        // Act & Assert #1: Persist a Bird instance
        unitUnderTest.setUp();
        EntityTransaction userTransaction = unitUnderTest.transaction;
        userTransaction.begin();

        unitUnderTest.entityManager.persist(bird);
        unitUnderTest.commitAndStartNewTransaction();
        userTransaction = unitUnderTest.transaction;

        // Act & Assert #2: Fire an arbitrary JPQL query
        final List<Bird> birds = unitUnderTest.jpa.fireJpaQuery("select a from Bird a order by a.name");
        Assert.assertEquals(1, birds.size());

        final Bird retrieved = birds.get(0);
        Assert.assertEquals("birdName", retrieved.getName());

        // Act & Assert #3: Teardown
        unitUnderTest.tearDown();
        Assert.assertNull(unitUnderTest.transaction);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNonexistentPersistenceUnit() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/incorrectpersistenceunit.xml";
        final String persistenceUnit = "nonexistentPersistenceUnit";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.emProperties = getHsqldbEntityManagerFactoryProperties(persistenceXmlFile);

        // Act & Assert
        unitUnderTest.setUp();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnIncorrectPersistenceUnit() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/incorrectpersistenceunit.xml";
        final String persistenceUnit = "incorrectPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit);
        unitUnderTest.emProperties = getHsqldbEntityManagerFactoryProperties(persistenceXmlFile);

        // Act & Assert
        unitUnderTest.setUp();
    }

    //
    // Private helpers
    //


    protected File getTargetDirectory() {

        // Use CodeSource
        final URL location = getClass().getProtectionDomain().getCodeSource().getLocation();

        // Check sanity
        Validate.notNull(location, "CodeSource location not found for class [" + getClass().getSimpleName() + "]");

        // All done.
        return new File(location.getPath()).getParentFile();
    }

    protected Map<String, String> getHsqldbEntityManagerFactoryProperties(final String persistenceXmlFile) {

        // Get standard properties
        final String jdbcDriverClass = dbType.getJdbcDriverClass();
        final String jdbcURL = dbType.getUnitTestJdbcURL(StandardPersistenceTest.DEFAULT_DB_NAME,
                getTargetDirectory());

        final Map<String, String> toReturn = new TreeMap<String, String>();

        final String[][] persistenceProviderProps = new String[][]{

                // Generic properties
                {"javax.persistence.provider", jpaProviderClass},
                {"javax.persistence.jdbc.driver", jdbcDriverClass},
                {"javax.persistence.jdbc.url", jdbcURL},
                {"javax.persistence.jdbc.user", StandardPersistenceTest.DEFAULT_DB_UID},
                {"javax.persistence.jdbc.password", StandardPersistenceTest.DEFAULT_DB_PASSWORD},

                // OpenJPA properties
                {"openjpa.jdbc.DBDictionary", dbType.getDatabaseDialectClass()},

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
                {"openjpa.Log", "DefaultLevel=WARN, Tool=INFO, RUNTIME=WARN, SQL=WARN"},
                {"openjpa.jdbc.SchemaFactory", "native(ForeignKeys=true)"},
                {"openjpa.jdbc.TransactionIsolation", "serializable"},
                {"openjpa.RuntimeUnenhancedClasses", "supported"},

                // Eclipselink properties
                {"eclipselink.target-database", dbType.getHibernatePlatformClass()},
                {"eclipselink.logging.level", "FINER"},
                {"eclipselink.orm.throw.exceptions", "true"},
                {"eclipselink.ddl-generation", "drop-and-create-tables"},
                {"eclipselink.ddl-generation.output-mode", "database"},
                {"eclipselink.persistencexml", persistenceXmlFile}
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
            for(String currentPrefix : overridablePrefixes) {
                if(currentPropertyName.trim().toLowerCase().startsWith(currentPrefix)) {
                    toReturn.put(currentPropertyName, "" + current.getValue());
                }
            }
        }

        // All done.
        return toReturn;
    }
}
