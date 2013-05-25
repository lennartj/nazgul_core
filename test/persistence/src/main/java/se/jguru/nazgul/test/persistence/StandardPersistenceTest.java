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

import java.util.Map;
import java.util.TreeMap;

/**
 * Concrete implementation of the AbstractDbUnitAndJpaTest specification, providing
 * default behaviour and some utility methods to simplify automated (integration-)test
 * creation using an in-memory HSQL database.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardPersistenceTest extends AbstractDbUnitAndJpaTest {

    /**
     * The name of the standard persistence unit.
     */
    public static final String DEFAULT_PERSISTENCE_UNIT = "InMemoryTestPU";

    /**
     * The name of the standard/default (in-memory) database.
     */
    public static final String DEFAULT_DB_NAME = "inMemoryTestDatabase";

    /**
     * The default userId within the in-memory unit test database.
     */
    public static final String DEFAULT_DB_UID = "sa";

    /**
     * The default password within the in-memory unit test database.
     */
    public static final String DEFAULT_DB_PASSWORD = "";

    /**
     * Retrieves the DatabaseType used by this AbstractJpaPersistenceDbUnitTest.
     *
     * @return The DatabaseType used by this AbstractJpaPersistenceDbUnitTest.
     *         StandardPersistenceTest uses an in-memory HSQL database. Override
     *         in specific implementations/test cases to change the DatabaseType.
     * @see DatabaseType#HSQL
     */
    @Override
    protected DatabaseType getDatabaseType() {
        return DatabaseType.HSQL;
    }

    /**
     * Defines the value {@code "testdata/" + getClass().getSimpleName() + "/persistence.xml"},
     * implying that each concrete test class should define its own persistence.xml file for
     * test-scope usage. This defines a standard behaviour for JPA- and dbUnit-based tests.
     * Override in concrete test classes to use another persistence.xml file.
     *
     * @return the value {@code "testdata/" + getClass().getSimpleName() + "/persistence.xml"}.
     */
    @Override
    protected String getPersistenceXmlFile() {
        return "testdata/" + getClass().getSimpleName() + "/persistence.xml";
    }

    /**
     * Retrieves the standard PersistenceUnit name, {@code DEFAULT_PERSISTENCE_UNIT}.
     * Override in concrete subclasses to use another persistenceUnit.
     *
     * @return the standard PersistenceUnit name, {@code DEFAULT_PERSISTENCE_UNIT}.
     */
    @Override
    protected String getPersistenceUnitName() {
        return DEFAULT_PERSISTENCE_UNIT;
    }

    /**
     * Retrieves the PersistenceProviderType instance used within this AbstractJpaTest.
     *
     * @return The PersistenceProviderType used by this AbstractJpaTest.
     */
    @Override
    protected PersistenceProviderType getPersistenceProviderType() {
        return PersistenceProviderType.OPENJPA_2;
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
     *         need to be declared within the persistence.xml file.
     * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
     */
    @Override
    protected Map<String, String> getEntityManagerFactoryProperties() {

        // Get standard properties
        final String jdbcDriverClass = getDatabaseType().getJdbcDriverClass();
        final String jdbcURL = getDatabaseType().getUnitTestJdbcURL(DEFAULT_DB_NAME, getTargetDirectory());
        final String persistenceProviderClass = getPersistenceProviderType().getPersistenceProviderClass();

        final Map<String, String> toReturn = new TreeMap<String, String>();

        final String[][] persistenceProviderProps = new String[][]{

                // Generic properties
                {"javax.persistence.provider", persistenceProviderClass},
                {"javax.persistence.jdbc.driver", jdbcDriverClass},
                {"javax.persistence.jdbc.url", jdbcURL},
                {"javax.persistence.jdbc.user", DEFAULT_DB_UID},
                {"javax.persistence.jdbc.password", DEFAULT_DB_PASSWORD},

                // OpenJPA properties
                {"openjpa.jdbc.DBDictionary", getDatabaseType().getDatabaseDialectClass()},
                {"openjpa.ConnectionDriverName", jdbcDriverClass},
                {"openjpa.ConnectionURL", jdbcURL},
                {"openjpa.ConnectionUserName", DEFAULT_DB_UID},
                {"openjpa.ConnectionPassword", DEFAULT_DB_PASSWORD},
                {"openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"},
                {"openjpa.InverseManager", "true"},
                {"openjpa.Log", "DefaultLevel=WARN, Tool=INFO, RUNTIME=WARN, SQL=WARN"},
                {"openjpa.jdbc.SchemaFactory", "native(ForeignKeys=true)"},
                {"openjpa.jdbc.TransactionIsolation", "serializable"},

                // Eclipselink properties
                {"eclipselink.target-database", getDatabaseType().getHibernatePlatformClass()},
                {"eclipselink.logging.level", "FINER"},
                {"eclipselink.orm.throw.exceptions", "true"},
                {"eclipselink.ddl-generation", "drop-and-create-tables"},
                {"eclipselink.ddl-generation.output-mode", "database"},
                {"eclipselink.persistencexml", getPersistenceXmlFile()}
        };

        // First, add the default values - and then overwrite them with
        // any given system properties.
        for (String[] current : persistenceProviderProps) {
            toReturn.put(current[0], current[1]);
        }
        for (Object current : System.getProperties().keySet()) {
            toReturn.put("" + current, System.getProperty("" + current));
        }

        // All done.
        return toReturn;
    }

    /**
     * Invoked during setup to prepare the schema used for test.
     */
    @Override
    protected void cleanupTestSchema() {
    }

    /**
     * Retrieves the name of the database used for this AbstractDbUnitAndJpaTest.
     *
     * @return the name of the database used for this AbstractDbUnitAndJpaTest.
     */
    @Override
    protected String getDatabaseName() {
        return DEFAULT_DB_NAME;
    }
}
