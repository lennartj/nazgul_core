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

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.persistence.pets.Bird;

import javax.persistence.EntityTransaction;
import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractDbUnitAndJpaTestTest {

    // Shared state
    private ClassLoader originalClassLoader;

    @Before
    public void setupSharedState() {

        // Stash the original ClassLoader
        final Thread activeThread = Thread.currentThread();
        originalClassLoader = activeThread.getContextClassLoader();
    }

    @After
    public void teardownSharedState() {

        // Restore the original classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void validateStandardLifecycle() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockdbunitpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractDbUnitAndJpaTest unitUnderTest = new MockAbstractDbUnitAndJpaTest(
                persistenceUnit,
                persistenceXmlFile,
                true,
                "unittestDatabaseID",
                false);

        final Bird bird = new Bird("birdName", "cool birds");

        // Act & Assert #1: Validate the entitymanager properties
        unitUnderTest.setUp();
        Assert.assertNotNull(unitUnderTest.entityManager);
        Assert.assertNotNull(unitUnderTest.jpa);
        Assert.assertNotNull(unitUnderTest.getJpaUnitTestConnection(true));

        EntityTransaction userTransaction = unitUnderTest.transaction;
        Assert.assertNotNull(userTransaction);
        Assert.assertFalse(userTransaction.isActive());

        // Act & Assert #2: Begin the user transaction
        final int firstTransactionHashCode = unitUnderTest.hashCode();
        userTransaction.begin();
        Assert.assertTrue(userTransaction.isActive());

        // Act & Assert #3: Persist a Bird instance, and commit without starting a new transaction.
        unitUnderTest.entityManager.persist(bird);
        unitUnderTest.commitAndStartNewTransaction();
        userTransaction = unitUnderTest.transaction;
        Assert.assertNotSame(firstTransactionHashCode, userTransaction.hashCode());

        // Act & Assert #4: Dig out the database using dbUnit methods.
        final IDataSet dataSet = unitUnderTest.iDatabaseConnection.createDataSet();
        final String flatXmlDataSet = unitUnderTest.extractFlatXmlDataSet(dataSet);
        Assert.assertTrue(
                flatXmlDataSet.contains("<BIRD ID=\"0\" CATEGORY=\"cool birds\" NAME=\"birdName\" VERSION=\"1\"/>")
        );

        // Act & Assert #5: Find the expected FlatXmlDataSet - as written to a file.
        final IDataSet expected = unitUnderTest.getDataSet("testdata/mockjpa/expected_validateStandardLifecycle.xml");
        Assertion.assertEquals(expected, dataSet);

        // Act & Assert #6: Validate same connection for dbUnit and JPA EntityManager
        Assert.assertEquals(unitUnderTest.iDatabaseConnection.getConnection().hashCode(),
                unitUnderTest.getJpaUnitTestConnection(true).hashCode());

        // Act & Assert #7: Teardown
        unitUnderTest.tearDown();
        Assert.assertNull(unitUnderTest.transaction);
    }

    @Test
    public void validateCorrectlyAcquiringTargetDirectory() {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockdbunitpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractDbUnitAndJpaTest unitUnderTest = new MockAbstractDbUnitAndJpaTest(
                persistenceUnit,
                persistenceXmlFile,
                true,
                "unittestDatabaseID",
                false);

        // Act
        final File targetDirectory = unitUnderTest.getTargetDirectory();

        // Assert
        Assert.assertNotNull(targetDirectory);
        Assert.assertTrue(targetDirectory.isDirectory());
        Assert.assertEquals("target", targetDirectory.getName().toLowerCase());
    }

    @Test
    public void validateCreatingSeparateConnectionForDbUnit() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockdbunitpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractDbUnitAndJpaTest unitUnderTest = new MockAbstractDbUnitAndJpaTest(
                persistenceUnit,
                persistenceXmlFile,
                true,
                "unittestDatabaseID",
                true);

        final Bird bird = new Bird("birdName", "cool birds");

        // Act & Assert #1: Validate the entitymanager properties
        unitUnderTest.setUp();
        Assert.assertNotNull(unitUnderTest.entityManager);
        Assert.assertNotNull(unitUnderTest.jpa);
        Assert.assertNotNull(unitUnderTest.getJpaUnitTestConnection(true));

        EntityTransaction userTransaction = unitUnderTest.transaction;
        Assert.assertNotNull(userTransaction);
        Assert.assertFalse(userTransaction.isActive());

        // Act & Assert #2: Begin the user transaction
        final int firstTransactionHashCode = unitUnderTest.hashCode();
        userTransaction.begin();
        Assert.assertTrue(userTransaction.isActive());

        // Act & Assert #3: Persist a Bird instance, and commit without starting a new transaction.
        unitUnderTest.entityManager.persist(bird);
        unitUnderTest.entityManager.flush();
        unitUnderTest.commitAndStartNewTransaction();
        userTransaction = unitUnderTest.transaction;
        Assert.assertNotSame(firstTransactionHashCode, userTransaction.hashCode());

        // Act & Assert #4: Dig out the database using dbUnit methods.
        final IDataSet dataSet = unitUnderTest.iDatabaseConnection.createDataSet();
        final String flatXmlDataSet = unitUnderTest.extractFlatXmlDataSet(dataSet);

        Assert.assertTrue(
                flatXmlDataSet.contains("<BIRD ID=\"0\" CATEGORY=\"cool birds\" NAME=\"birdName\" VERSION=\"1\"/>")
                         );

        // Act & Assert #5: Find the expected FlatXmlDataSet - as written to a file.
        final IDataSet expected = unitUnderTest.getDataSet("testdata/mockjpa/expected_validateStandardLifecycle.xml");
        Assertion.assertEquals(expected, dataSet);

        // Act & Assert #6: Validate not same connection for dbUnit and JPA EntityManager
        Assert.assertNotSame(unitUnderTest.iDatabaseConnection.getConnection(),
                unitUnderTest.getJpaUnitTestConnection(true));

        // Act & Assert #7: Teardown
        unitUnderTest.tearDown();
        Assert.assertNull(unitUnderTest.transaction);
    }
}
