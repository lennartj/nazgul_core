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
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Abstract superclass for JPA based tests, that uses dbUnit to define
 * criteria to setup and evaluate database state
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractDbUnitAndJpaTest extends AbstractJpaTest {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractDbUnitAndJpaTest.class);

    /**
     * The OpenJpa username property.
     */
    public static final String OPENJPA_CONNECTION_USERNAME_KEY = "openjpa.ConnectionUserName";

    /**
     * The OpenJpa password property.
     */
    public static final String OPENJPA_CONNECTION_PASSWORD_KEY = "openjpa.ConnectionPassword";

    /**
     * The dbUnit IDatabaseConnection, hooked up to the same
     * database as the JPA EntityManager, either using a connection of its own
     * or piggybacking on
     */
    protected IDatabaseConnection iDatabaseConnection;

    /**
     * Retrieves the name of the database used for this AbstractDbUnitAndJpaTest.
     *
     * @return the name of the database used for this AbstractDbUnitAndJpaTest.
     */
    protected abstract String getDatabaseName();

    /**
     * @return The DatabaseType used by this AbstractJpaPersistenceDbUnitTest.
     */
    protected abstract DatabaseType getDatabaseType();

    // Internal state

    /**
     * Retrieves {@code true} if dbUnit and JPA should use separate Connections
     * to the test database, and {@code false} otherwise. Defaults to {@code false},
     * but can be overridden in subclasses to use other behaviour.
     *
     * @return {@code true} if dbUnit and JPA should use separate Connections
     *         to the test database, and {@code false} otherwise.
     */
    protected boolean isSeparateConnectionsUsedForDbUnitAndJPA() {
        return false;
    }

    /**
     * Setting up the DbUnit framework which setup the inherited Persistence Entity Manager.
     * DbUnit will use the already initiated SQL connection from the Persistence Entity Manager.
     *
     * @throws Exception if an error occurred.
     */
    @Before
    public void setUp() throws Exception {

        // Delegate
        super.setUp();

        // Acquire the Connection to the database
        final Connection dbConnection = isSeparateConnectionsUsedForDbUnitAndJPA()
                ? getStandaloneDbConnection(getDatabaseName(), getTargetDirectory())
                : jpaUnitTestConnection;

        // Create dbUnit connection and assign the DataTypeFactory
        iDatabaseConnection = new DatabaseConnection(dbConnection);
        iDatabaseConnection.getConfig().setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                getDatabaseType().getDataTypeFactory());
    }

    /**
     * Retrieves a File to the target directory.
     *
     * @return the project target directory path, wrapped in a File object.
     */
    protected File getTargetDirectory() {

        // Use CodeSource
        final URL location = getClass().getProtectionDomain().getCodeSource().getLocation();

        // Check sanity
        Validate.notNull(location, "CodeSource location not found for class [" + getClass().getSimpleName() + "]");

        // All done.
        return new File(location.getPath()).getParentFile();
    }

    /**
     * Load a DataSet from XML file.
     *
     * @param filename data file xml.
     * @return an IDataSet instance of the entities loaded from xml.
     * @throws DataSetException if an error occurred while reading XML definition.
     */
    protected IDataSet getDataSet(final String filename) throws DataSetException {

        // Check sanity
        Validate.notEmpty(filename, "Cannot handle null or empty filename argument.");

        final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final InputStream flatXmlFile = contextClassLoader.getResourceAsStream(filename);
        Validate.notNull(flatXmlFile, "Could not find a file from filename [" + filename + "]");

        // All done.
        return flatXmlDataSetBuilder.build(flatXmlFile);
    }

    /**
     * Converts the supplied dataSet to a FlatXmlDataSet which, in turn, is written to a String.
     *
     * @param dataSet The IDataSet to convert.
     * @return The string representation of the supplied IDataSet.
     */
    protected final String extractFlatXmlDataSet(final IDataSet dataSet) {

        // Check sanity
        Validate.notNull(dataSet, "Cannot handle null dataset argument.");

        // Convert and return.
        final StringWriter out = new StringWriter();
        try {
            FlatXmlDataSet.write(dataSet, out);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not write FlatXmlDataSet.", e);
        }

        // All done.
        return out.toString();
    }

    /**
     * Drops all the Database Objects in the public schema of the active database.
     */
    protected final void dropAllDbObjectsInPublicSchema() {

        try {

            // Revert to plain-old JDBC to drop all DB objects in the public schema.
            final DatabaseMetaData metaData = jpaUnitTestConnection.getMetaData();
            final ResultSet dbObjects = metaData.getTables(null, getDatabaseType().getPublicSchemaName(), "%", null);
            final Statement dropStatement = jpaUnitTestConnection.createStatement();

            while (dbObjects.next()) {
                final String schemaAndTableName = dbObjects.getString(2) + "." + dbObjects.getString(3);
                final String dbObjectType = dbObjects.getString(4);
                log.debug(" Dropping [" + schemaAndTableName + "] ... ");

                // Add the drop statement to the batch.
                dropStatement.addBatch("DROP " + dbObjectType + " " + schemaAndTableName);
            }
            final int[] results = dropStatement.executeBatch();

            log.debug(" ... Done dropping [" + results.length + "] table(s).");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //
    // Private helpers
    //

    private Connection getStandaloneDbConnection(final String databaseName, final File targetDirectory) {

        final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader localClassLoader = getClass().getClassLoader();
        final String jdbcDriverClass = getDatabaseType().getJdbcDriverClass();

        // 0) Load the DB Driver
        try {
            threadClassLoader.loadClass(jdbcDriverClass);
        } catch (ClassNotFoundException e) {

            // Attempt to load the class using the local class classloader
            try {
                localClassLoader.loadClass(jdbcDriverClass);
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Could not load JDBC driver class '" + jdbcDriverClass + "'", e1);
            }
        }

        // 1) Use the standard DriverManager-based Connection
        final Map<String, String> properties = getEntityManagerFactoryProperties();

        final String userName = properties.containsKey(OPENJPA_CONNECTION_USERNAME_KEY)
                ? properties.get(OPENJPA_CONNECTION_USERNAME_KEY)
                : "sa";
        final String password = properties.containsKey(OPENJPA_CONNECTION_PASSWORD_KEY)
                ? properties.get(OPENJPA_CONNECTION_PASSWORD_KEY)
                : "";

        try {

            final String unitTestJdbcURL = getDatabaseType().getUnitTestJdbcURL(databaseName, targetDirectory);

            // All done.
            return DriverManager.getConnection(
                    unitTestJdbcURL,
                    userName,
                    password);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open JDBC Connection using [" + jdbcDriverClass + "]", e);
        }
    }
}
