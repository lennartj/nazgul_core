/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.test.persistence.schema;

import liquibase.Liquibase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import se.jguru.nazgul.test.persistence.AbstractLifecycleAware;
import se.jguru.nazgul.test.persistence.AbstractPersistenceTest;
import se.jguru.nazgul.test.persistence.DatabaseType;
import se.jguru.nazgul.test.persistence.SchemaManager;

import java.io.File;
import java.sql.Connection;

/**
 * Schema manager that uses Liquibase to create the test database structure.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LiquibaseSchemaManager<T extends AbstractPersistenceTest>
        extends AbstractLifecycleAware<T> implements SchemaManager<T> {

    // Constants
    private static final String LIQUIBASE_CONFIG_PREFIX = "liquibase_";

    /**
     * The filename of the Liquibase schema setup file.
     */
    public static final String LIQUIBASE_SETUP_CONFIG = LIQUIBASE_CONFIG_PREFIX + "setup_";

    // Internal state
    private Liquibase liquibase;
    private ResourceAccessor resourceAccessor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInitialize(final T testClass, final String testMethodName) {

        // Find the test data directory
        final File testDataDirectory = getTestDataDirectory(testClass.getClass(), testMethodName);

        // Assign internal state
        resourceAccessor = new FileSystemResourceAccessor(testDataDirectory.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onShutdown(final T testClass, final String testMethodName) {

        try {
            // Drop all from the database
            liquibase.dropAll();
        } catch (Exception e) {
            throw new IllegalStateException("Could not drop all from database.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSchema(final String schemaSpecification) throws IllegalStateException {

        final String changeLogFileName = LIQUIBASE_SETUP_CONFIG + schemaSpecification + ".xml";
        final Connection conn = (Connection) getTestClass().getDbConnection();

        // Create the liquibase instance, and update the database according to the specs.
        try {
            liquibase = new Liquibase(changeLogFileName, resourceAccessor, getDatabaseType().getJdbcConnection(conn));
            liquibase.update(schemaSpecification);
        } catch (LiquibaseException e) {
            throw new IllegalStateException("Could not properly set up schema from [" + changeLogFileName + "]", e);
        }
    }
}
