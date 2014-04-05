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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockAbstractJpaTest extends AbstractJpaTest {

    public boolean cleanupSchemaInTeardown = true;

    // Internal state
    private String persistenceXmlFile;
    private String persistenceUnit;

    public MockAbstractJpaTest(final String persistenceXmlFile,
                               final String persistenceUnit) {
        this.persistenceXmlFile = persistenceXmlFile;
        this.persistenceUnit = persistenceUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() {

        // Proceed with normal testcase teardown.
        super.tearDown();
    }

    /**
     * Invoked during setup to prepare the schema used for test.
     */
    @Override
    protected void cleanupTestSchema() {

        if (cleanupSchemaInTeardown) {

            try {
                final DatabaseMetaData metaData = getJpaUnitTestConnection().getMetaData();
                final ResultSet tables = metaData.getTables(null, DatabaseType.HSQL.getPublicSchemaName(), "%", null);
                final Statement dropStatement = getJpaUnitTestConnection().createStatement();
                while (tables.next()) {
                    final String schemaAndTableName = tables.getString(2) + "." + tables.getString(3);
                    System.out.println(" Dropping [" + schemaAndTableName + "] ... ");

                    dropStatement.addBatch("drop table " + schemaAndTableName);
                }
                final int[] results = dropStatement.executeBatch();

                System.out.println(" ... Done dropping [" + results.length + "] table(s).");

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
}
