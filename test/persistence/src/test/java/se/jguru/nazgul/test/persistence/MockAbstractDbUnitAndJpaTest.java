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

import se.jguru.nazgul.test.persistence.jpa.PersistenceProviderType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockAbstractDbUnitAndJpaTest extends AbstractDbUnitAndJpaTest {

    // Internal state
    public boolean cleanupSchemaInTeardown;
    private String persistenceXmlFile;
    private String persistenceUnit;
    private String databaseName;
    private boolean separateConnectionForDbUnit;

    public MockAbstractDbUnitAndJpaTest(final String persistenceUnit,
                                        final String persistenceXmlFile,
                                        final boolean cleanupSchemaInTeardown,
                                        final String databaseName,
                                        final boolean separateConnectionForDbUnit) {
        this.persistenceUnit = persistenceUnit;
        this.persistenceXmlFile = persistenceXmlFile;
        this.cleanupSchemaInTeardown = cleanupSchemaInTeardown;
        this.databaseName = databaseName;
        this.separateConnectionForDbUnit = separateConnectionForDbUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DatabaseType getDatabaseType() {
        return DatabaseType.HSQL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isSeparateConnectionsUsedForDbUnitAndJPA() {

        // Just a coverage stunt...
        super.isSeparateConnectionsUsedForDbUnitAndJPA();

        // Override the result.
        return separateConnectionForDbUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {
        if (cleanupSchemaInTeardown) {
            dropAllDbObjectsInPublicSchema(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPersistenceXmlFile() {
        return persistenceXmlFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPersistenceUnitName() {
        return persistenceUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
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
    @Override
    protected SortedMap<String, String> getEntityManagerFactoryProperties() {

        // Get standard properties
        final String jdbcDriverClass = getDatabaseType().getJdbcDriverClass();
        final String jdbcURL = getDatabaseType().getUnitTestJdbcURL(getDatabaseName(), getTargetDirectory());
        final String persistenceProviderClass = System.getProperty("jpa_provider_class",
                PersistenceProviderType.ECLIPSELINK_2.getPersistenceProviderClass());

        final SortedMap<String, String> toReturn = new TreeMap<String, String>();

        final String[][] persistenceProviderProps = new String[][]{

                // Generic properties
                {"javax.persistence.provider", persistenceProviderClass},
                {"javax.persistence.jdbc.driver", jdbcDriverClass},
                {"javax.persistence.jdbc.url", jdbcURL},
                {"javax.persistence.jdbc.user", "sa"},
                {"javax.persistence.jdbc.password", ""},

                // OpenJPA properties
                {"openjpa.jdbc.DBDictionary", getDatabaseType().getDatabaseDialectClass()},

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
                {"eclipselink.deploy-on-startup", "true"},
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
