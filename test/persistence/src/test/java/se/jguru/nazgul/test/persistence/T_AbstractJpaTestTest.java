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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.persistence.classloader.PersistenceRedirectionClassLoader;
import se.jguru.nazgul.test.persistence.pets.Bird;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractJpaTestTest {

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
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit,
                PersistenceProviderType.OPENJPA_2);

        final Bird bird = new Bird("birdName", "cool birds");

        // Act & Assert #1: Validate the entitymanager properties
        unitUnderTest.setUp();
        Assert.assertNotNull(unitUnderTest.entityManager);
        Assert.assertNotNull(unitUnderTest.jpa);
        Assert.assertNotNull(unitUnderTest.jpaUnitTestConnection);

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

        // Act & Assert #4: Teardown
        unitUnderTest.tearDown();
        Assert.assertNull(unitUnderTest.transaction);
    }

    @Test
    public void validateTransactionlessReadDoesNotYieldException() throws Exception {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit,
                PersistenceProviderType.OPENJPA_2);

        // Act & Assert #1: Close transaction, and check state
        unitUnderTest.setUp();
        unitUnderTest.transaction.begin();
        unitUnderTest.commit(false);
        Assert.assertFalse(unitUnderTest.transaction.isActive());

        // Act & Assert #2:
        final Query query = unitUnderTest.entityManager.createQuery("select b from Bird b order by b.name");
        final List<Bird> birds = query.getResultList();
        Assert.assertNotNull(birds);
        Assert.assertEquals(0, birds.size());

        Assert.assertFalse(unitUnderTest.entityManager.getTransaction().isActive());
        Assert.assertSame(unitUnderTest.transaction, unitUnderTest.entityManager.getTransaction());
    }

    @Test
    public void validateNoExceptionOnTeardownWithoutSetup() {

        // Assemble
        final String persistenceXmlFile = "testdata/mockjpa/mockpersistence.xml";
        final String persistenceUnit = "birdPU";
        final MockAbstractJpaTest unitUnderTest = new MockAbstractJpaTest(
                persistenceXmlFile,
                persistenceUnit,
                PersistenceProviderType.OPENJPA_2);
        unitUnderTest.cleanupSchemaInTeardown = false;

        // Act & Assert
        unitUnderTest.tearDown();
    }
}
