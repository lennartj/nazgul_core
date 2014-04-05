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
        super.isSeparateConnectionsUsedForDbUnitAndJPA();
        return separateConnectionForDbUnit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanupTestSchema() {
        dropAllDbObjectsInPublicSchema();
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
}
