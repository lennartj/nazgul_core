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

import liquibase.database.core.H2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.supplier.H2ConnSupplier;
import liquibase.database.jvm.HsqlConnection;
import liquibase.database.jvm.JdbcConnection;
import org.apache.commons.lang3.Validate;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Database properties enumeration, indicating dbUnit DatatypeFactory, JDBC driver and JDBC URI.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum DatabaseType {

    /**
     * HSQL database definitions.
     */
    HSQL(new HsqldbDataTypeFactory(),
            "org.hsqldb.jdbc.JDBCDriver",
            -1,
            "jdbc:hsqldb:mem:{0};hsqldb.tx_level=serializable",
            0,
            "org.apache.openjpa.jdbc.sql.HSQLDictionary",
            "org.eclipse.persistence.platform.database.HSQLPlatform",
            "PUBLIC",
            HsqlConnection.class.getName()),

    /**
     * H2 database definitions.
     */
    H2(new H2DataTypeFactory(),
            "org.h2.Driver",
            0,
            "jdbc:h2:{0}",
            -1,
            "org.apache.openjpa.jdbc.sql.H2Dictionary",
            "org.eclipse.persistence.platform.database.H2Platform",
            "public",
            JdbcConnection.class.getName()),

    /**
     * PostgreSQL database definitions for a postgresql database running on localhost and port 5432.
     */
    POSTGRESQL_LOCALHOST(new PostgresqlDataTypeFactory(),
            "org.postgresql.Driver",
            -1,
            "jdbc:postgresql:{0}",
            0,
            "org.apache.openjpa.jdbc.sql.PostgresDictionary",
            "org.eclipse.persistence.platform.database.PostgreSQLPlatform",
            "public",
            JdbcConnection.class.getName());

    // Internal state
    private int uniqueDirectoryParameterIndex;
    private int dbNameParameterIndex;
    private IDataTypeFactory dataTypeFactory;
    private String openJpaDatabaseDictionaryClass;
    private String jdbcDriverClass;
    private String unitTestJdbcUrlPattern;
    private String hibernatePlatformClass;
    private String publicSchemaName;
    private String liquibaseDatabaseConnectionClass;

    /**
     * Creates a new DatabaseType wrapping the given properties assisting
     * in setting up an automated testcase using dbUnit, JPA and/or JDBC.
     *
     * @param dataTypeFactory                  The dbUnit IDataTypeFactory used for the supplied database.
     * @param jdbcDriverClass                  The JDBC driver class for the supplied database.
     * @param uniqueDirectoryParameterIndex    If {@code true}, implies that the supplied database requires
     *                                         a unique directory to write its state to.
     * @param unitTestJdbcUrlPattern           A pattern for defining a unit-test (in-memory,
     *                                         if possible) JDBC URL for the supplied database.
     * @param openJpaDatabaseDictionaryClass   The class name realizing the OpenJPA database dictionary for the
     *                                         active DatabaseType.
     * @param publicSchemaName                 The name of the public schema for the active DatabaseType.
     * @param liquibaseDatabaseConnectionClass The name of the Liquibase JdbcConnection class for
     *                                         the active DatabaseType.
     */
    private DatabaseType(final IDataTypeFactory dataTypeFactory,
                         final String jdbcDriverClass,
                         final int uniqueDirectoryParameterIndex,
                         final String unitTestJdbcUrlPattern,
                         final int dbNameParameterIndex,
                         final String openJpaDatabaseDictionaryClass,
                         final String hibernatePlatformClass,
                         final String publicSchemaName,
                         final String liquibaseDatabaseConnectionClass) {

        // Assign internal state
        this.dataTypeFactory = dataTypeFactory;
        this.jdbcDriverClass = jdbcDriverClass;
        this.unitTestJdbcUrlPattern = unitTestJdbcUrlPattern;
        this.uniqueDirectoryParameterIndex = uniqueDirectoryParameterIndex;
        this.dbNameParameterIndex = dbNameParameterIndex;
        this.openJpaDatabaseDictionaryClass = openJpaDatabaseDictionaryClass;
        this.hibernatePlatformClass = hibernatePlatformClass;
        this.publicSchemaName = publicSchemaName;
        this.liquibaseDatabaseConnectionClass = liquibaseDatabaseConnectionClass;
    }

    /**
     * Retrieves the dbUnit IDataTypeFactory used for the supplied database.
     *
     * @return The dbUnit IDataTypeFactory used for the supplied database.
     */
    public IDataTypeFactory getDataTypeFactory() {
        return dataTypeFactory;
    }

    /**
     * Retrieves the hibernate platform class, such as
     * {@code org.eclipse.persistence.platform.database.HSQLPlatform}.
     *
     * @return the hibernate platform class, such as
     * {@code org.eclipse.persistence.platform.database.HSQLPlatform}.
     */
    public String getHibernatePlatformClass() {
        return hibernatePlatformClass;
    }

    /**
     * Retrieves the Database dialect class for the supplied database.
     * This is a property used by the OpenJPA persistence provider as
     * a persistence.xml property ("").
     *
     * @return the OpenJPA Database dialect class for the supplied database.
     */
    public String getDatabaseDialectClass() {
        return openJpaDatabaseDictionaryClass;
    }

    /**
     * Retrieves the JDBC driver class for the supplied database.
     *
     * @return The JDBC driver class for the supplied database.
     */
    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    /**
     * Retrieves a JDBC URL for a unit-test database.
     *
     * @param databaseName    The name of the database, which should be used as a parameter for the JDBC URL.
     * @param targetDirectory The (maven) target directory of the current project.
     * @return A JDBC URL appropriate for a unit-test database of the given type.
     */
    public String getUnitTestJdbcURL(final String databaseName, final File targetDirectory) {

        String toReturn = null;

        // Compile the parameters used for the JDBC URL.
        final SortedMap<Integer, String> parameterMap = new TreeMap<Integer, String>();
        addParameter(parameterMap, dbNameParameterIndex, databaseName);

        if (uniqueDirectoryParameterIndex != -1) {

            File uniqueDirectory = null;
            for (int i = 0; true; i++) {

                // Create a new unique directory candidate.
                uniqueDirectory = new File(targetDirectory, "datafiles_" + (i++));

                // Already created?
                if (!uniqueDirectory.exists()) {
                    uniqueDirectory.mkdirs();

                    // Add the absolute path of the uniqueDirectory to the parameterMap
                    addParameter(parameterMap, uniqueDirectoryParameterIndex, uniqueDirectory.getAbsolutePath());
                    break;
                }
            }
        }

        // Format the JDBC URL.
        if (parameterMap.size() == 0) {
            toReturn = unitTestJdbcUrlPattern;
        } else {
            toReturn = MessageFormat.format(unitTestJdbcUrlPattern, parameterMap.values().toArray());
        }

        // All done.
        return toReturn;
    }

    /**
     * Acquires the [default] name of the public schema for this DatabaseType.
     *
     * @return the [default] name of the public schema for this DatabaseType.
     */
    public String getPublicSchemaName() {
        return publicSchemaName;
    }

    /**
     * Retrieves a Liquibase JdbcConnection subclass for this DatabaseType.
     *
     * @param dbConnection An SQL Connection to the unit test database.
     * @param <T> The concrete type of JdbcConnection
     * @return the class name for the Liquibase concrete JdbcConnection subclass corresponding to this DatabaseType.
     */
    public <T extends JdbcConnection> T getJdbcConnection(final Connection dbConnection) {

        // Check sanity
        Validate.notNull(dbConnection, "Cannot handle null dbConnection argument.");

        try {
            // Load the Liquibase JdbcConnection class.
            final Class<T> connClass = (Class<T>) getClass()
                    .getClassLoader()
                    .loadClass(liquibaseDatabaseConnectionClass);

            // Find the correct constructor
            final Constructor<? extends JdbcConnection> constructor = connClass.getDeclaredConstructor(Connection.class);
            Validate.notNull(constructor, "Class [" + liquibaseDatabaseConnectionClass
                    + "] did not contain a constructor with a single java.sql.Connection argument.");
            final Object[] arguments = new Object[] {dbConnection};

            // All done.
            return (T) constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new IllegalStateException("Could not acquire JdbcConnection.", e);
        }
    }


    //
    // Private helpers
    //

    private void addParameter(final SortedMap<Integer, String> target, final int index, final String value) {

        if (index >= 0) {

            // Check sanity
            Validate.isTrue(!target.containsKey(index),
                    "Key [" + index + "] already present in target map [" + target + "]");

            // Add the value.
            target.put(index, value);
        }
    }
}
