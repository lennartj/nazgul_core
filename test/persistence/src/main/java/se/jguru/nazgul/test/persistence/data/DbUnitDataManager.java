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
package se.jguru.nazgul.test.persistence.data;

import org.apache.commons.lang3.Validate;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import se.jguru.nazgul.test.persistence.AbstractLifecycleAware;
import se.jguru.nazgul.test.persistence.AbstractPersistenceTest;
import se.jguru.nazgul.test.persistence.DataManager;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DbUnitDataManager<T extends AbstractPersistenceTest> extends AbstractLifecycleAware<T>
        implements DataManager<T> {

    // Constants
    private static final String DBUNIT_CONFIG_PREFIX = "dbunit_";

    /**
     * The filename of the DbUnit schema setup file.
     */
    public static final String DBUNIT_DATA_DEFINITION = DBUNIT_CONFIG_PREFIX + "primeDb.xml";

    // Internal state
    private File setupDataFile;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInitialize(final T testClass, final String testMethodName) {

        // Ensure that the data insert file exists
        setupDataFile = new File(getTestDataDirectory(testClass.getClass(), testMethodName), DBUNIT_DATA_DEFINITION);

        final boolean okDataFile = setupDataFile.exists() && setupDataFile.isFile() && setupDataFile.canRead();
        if (!okDataFile) {
            throw new IllegalArgumentException("Could not find DbUnit setup data file ["
                    + setupDataFile.getAbsolutePath() + "]");
        }
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
    public void insertData(final String dataIdentifier) throws IllegalStateException {

        try (Connection dbConnection = (Connection) getTestClass().getDbConnection()) {

            // Create dbUnit connection and assign the DataTypeFactory
            final DatabaseConnection dbUnitConnection = new DatabaseConnection(dbConnection);
            dbUnitConnection.getConfig().setProperty(
                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    getDatabaseType().getDataTypeFactory());

            // Read the dataset from the DbUnit setup data file
            final IDataSet dataSet = getDataSet(setupDataFile);

            // ... and insert the data into the test DB.
            DatabaseOperation.INSERT.execute(dbUnitConnection, dataSet);

        } catch (Exception e) {
            throw new IllegalStateException("Could not properly insert data from ["
                    + setupDataFile.getAbsolutePath() + "]", e);
        }
    }

    /**
     * Load a DataSet from XML file.
     *
     * @param aFile data file xml.
     * @return an IDataSet instance of the entities loaded from xml.
     * @throws org.dbunit.dataset.DataSetException if an error occurred while reading XML definition.
     */
    public static IDataSet getDataSet(final File aFile) throws DataSetException {

        // Check sanity
        Validate.notNull(aFile, "Cannot handle null aFile argument.");

        final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);

        try {
            // All done.
            return flatXmlDataSetBuilder.build(aFile);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not read data from file [" + aFile.getAbsolutePath() + "]", e);
        }
    }
}
