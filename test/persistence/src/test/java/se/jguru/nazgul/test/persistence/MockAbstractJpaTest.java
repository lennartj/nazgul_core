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

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockAbstractJpaTest extends AbstractJpaTest {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(MockAbstractJpaTest.class);

    public boolean cleanupSchemaInTeardown = true;

    // Internal state
    private boolean useDbUnitToCleanDatabaseStateInTeardown;
    private String persistenceXmlFile;
    private String persistenceUnit;
    private Map<String, String> emProperties;

    public MockAbstractJpaTest(final String persistenceXmlFile,
                               final String persistenceUnit,
                               final boolean useDbUnitToCleanDatabaseStateInTeardown,
                               final Map<String, String> entityManagerProperties) {

        this.persistenceXmlFile = persistenceXmlFile;
        this.persistenceUnit = persistenceUnit;
        this.useDbUnitToCleanDatabaseStateInTeardown = useDbUnitToCleanDatabaseStateInTeardown;
        if(entityManagerProperties != null) {
            emProperties = entityManagerProperties;
        }
    }

    /*
    @;Override
    public void setUp() throws Exception {

        // Delegate
        super.setUp();

        // Ensure that the database contains a sensible structure.
        final Connection jpaUnitTestConnection = getJpaUnitTestConnection(true);
        final ResultSet tables = jpaUnitTestConnection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            knownTableNames.add(tables.getString(3));
        }

        System.out.println("\n\nMockAbstractJpaTest (setup) - existing tablenames: " + knownTableNames + "\n\n");

        // Cleanup this operation.
        this.commitAndStartNewTransaction();
    }
    */

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() {

        if (useDbUnitToCleanDatabaseStateInTeardown) {

            // Be somewhat rigorous.
            try {
                final Connection jpaUnitTestConnection = getJpaUnitTestConnection(true);
                final DatabaseConnection dbUnitConn = new DatabaseConnection(jpaUnitTestConnection);
                final IDataSet allDatabaseDataSet = dbUnitConn.createDataSet();
                DatabaseOperation.DELETE_ALL.execute(dbUnitConn, allDatabaseDataSet);
            } catch (Exception e) {
                throw new IllegalStateException("Could not wipe all database state.", e);
            }
        }

        // Proceed with normal testcase teardown.
        super.tearDown();
    }

    /**
     * Invoked during setup to prepare the schema used for test.
     *
     * @param shutdownDatabase if {@code true}, the database should be shutdown after cleaning the schema.
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {

        if (cleanupSchemaInTeardown) {

            try {
                final DatabaseMetaData metaData = getJpaUnitTestConnection(true).getMetaData();
                final ResultSet tables = metaData.getTables(null, DatabaseType.HSQL.getPublicSchemaName(), "%", null);
                final Statement dropStatement = getJpaUnitTestConnection(true).createStatement();
                while (tables.next()) {
                    final String schemaAndTableName = tables.getString(2) + "." + tables.getString(3);

                    if(log.isInfoEnabled()) {
                        log.info(" Dropping [" + schemaAndTableName + "] ... ");
                    }

                    dropStatement.addBatch("drop table " + schemaAndTableName);
                }
                final int[] results = dropStatement.executeBatch();

                log.info(" ... Done dropping [" + results.length + "] table(s).");

            } catch (SQLException e) {
                e.printStackTrace();
            }
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
    protected SortedMap<String, String> getEntityManagerFactoryProperties() {

        final SortedMap<String, String> toReturn = super.getEntityManagerFactoryProperties();
        if(emProperties != null) {
            toReturn.putAll(emProperties);
        }

        // All Done.
        return toReturn;
    }
}
