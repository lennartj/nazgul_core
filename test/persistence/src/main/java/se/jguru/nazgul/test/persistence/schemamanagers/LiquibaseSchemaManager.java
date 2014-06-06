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
package se.jguru.nazgul.test.persistence.schemamanagers;

import liquibase.Liquibase;
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
    public static final String LIQUIBASE_SETUP_CONFIG = LIQUIBASE_CONFIG_PREFIX + "setup.xml";

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

        // Do nothing?
        testClass.getDbConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSchema(final String schemaSpecification, final DatabaseType databaseType)
            throws IllegalStateException {

        final String changeLogFileName = "setup_" + schemaSpecification + ".xml";
        final Connection conn = (Connection) getTestClass().getDbConnection();

        // Create the liquibase, and update the database according to the specs.
        liquibase = new Liquibase(changeLogFileName, resourceAccessor, databaseType.getJdbcConnection(conn));
        liquibase.update(schemaSpecification);
    }
}
