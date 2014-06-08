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

import liquibase.resource.ResourceAccessor;
import se.jguru.nazgul.test.persistence.AbstractLifecycleAware;
import se.jguru.nazgul.test.persistence.AbstractPersistenceTest;
import se.jguru.nazgul.test.persistence.SchemaManager;

import java.io.File;

/**
 * Schema manager that uses DbUnit to create the test database structure.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DbUnitSchemaManager <T extends AbstractPersistenceTest>
        extends AbstractLifecycleAware<T> implements SchemaManager<T> {

    // Constants
    private static final String DBUNIT_CONFIG_PREFIX = "dbunit_";

    /**
     * The filename of the DbUnit schema setup file.
     */
    public static final String DBUNIT_SETUP_CONFIG = DBUNIT_CONFIG_PREFIX + "setup_";

    // Internal state
    private ResourceAccessor resourceAccessor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInitialize(final T testClass, final String testMethodName) {

        // Find the test data directory
        final File testDataDirectory = getTestDataDirectory(testClass.getClass(), testMethodName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onShutdown(final T testClass, final String testMethodName) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSchema(final String schemaSpecification) throws IllegalStateException {
    }
}
