/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.persistence.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaPersistenceOperationsTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(JpaPersistenceOperationsTest.class);

    private ClassLoader originalClassLoader;
    private EntityManager unitTestEM;
    private EntityTransaction trans;
    private JpaPersistenceOperations unitUnderTest;

    @Before
    public void stashOriginalClassLoader() {
        originalClassLoader = getClass().getClassLoader();

        final String persistenceXmlFile = "testdata/JpaOperationsPersistence.xml";
        final PersistenceRedirectionClassLoader redirectionClassLoader =
                new PersistenceRedirectionClassLoader(getClass().getClassLoader(), persistenceXmlFile);

        Thread.currentThread().setContextClassLoader(redirectionClassLoader);

        // Create EntityManager and Transaction.
        unitTestEM = getEntityManager("InmemoryPU");
        unitUnderTest = new JpaPersistenceOperations(unitTestEM);
        trans = unitTestEM.getTransaction();
        trans.begin();
    }

    @After
    public void cleanupAndRestoreOriginalClassLoader() {

        if (!trans.isActive()) {
            trans.begin();
        }

        try {

            //
            // Be paranoid.
            //
            Query q = unitTestEM.createNativeQuery("DROP TABLE " + MockNazgulEntity.class.getSimpleName());
            int affectedRows = q.executeUpdate();
            log.info("Removed table. Rows affected [" + affectedRows + "]");

            trans.commit();
        } catch (final Exception e) {

            log.info("Could not commit the Transaction.", e);
        }

        try {

            // Close all the JPA resources.
            if (trans.isActive()) {
                trans.commit();
            }
            if (unitTestEM.isOpen()) {
                unitTestEM.close();
            }

        } catch (final Exception e) {

            log.info("Could not close the EntityManager.", e);
        }

        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullEntityManager() {

        // Act & Assert
        new JpaPersistenceOperations(null);
    }

    @Test
    public void validateJpaOperationOnNullEntityManipulation() throws Exception {

        // Act & Assert
        unitUnderTest.create(null);
        unitUnderTest.update(null);
        unitUnderTest.refresh(null);
        unitUnderTest.delete(null);
    }

    @Test(expected = PersistenceOperationException.class)
    public void validateJpaExceptionOnCreatingNonEntity() throws Exception {

        // Act & Assert
        unitUnderTest.create(new IncorrectAnnotationlessEntity());
    }

    @Test(expected = PersistenceOperationException.class)
    public void validateJpaExceptionOnUpdatingNonEntity() throws Exception {

        // Act & Assert
        unitUnderTest.update(new IncorrectAnnotationlessEntity());
    }

    @Test(expected = PersistenceOperationException.class)
    public void validateJpaExceptionOnRefreshingNonEntity() throws Exception {

        // Act & Assert
        unitUnderTest.refresh(new IncorrectAnnotationlessEntity());
    }

    @Test(expected = PersistenceOperationException.class)
    public void validateJpaExceptionOnDeletingNonEntity() throws Exception {

        // Act & Assert
        unitUnderTest.delete(new IncorrectAnnotationlessEntity());
    }

    @Test
    public void validateJpaOperationOnNullListReturnFromQuery() throws Exception {

        // Act
        MockNazgulEntity entity = new MockNazgulEntity("Foo");
        unitUnderTest.create(entity);
        trans.commit();
        trans.begin();

        final List<NazgulEntity> result = unitUnderTest.fireNamedQuery("getAllMockEntities");

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(entity, result.get(0));
    }

    @Test
    public void validateCorrectEncodingOfNonAsciiChars() throws Exception {

        // Assemble
        final String testChars = "åäöÅÄÖ";

        // Act
        unitUnderTest.create(new MockNazgulEntity(testChars));

        trans.commit();
        trans.begin();

        final List<NazgulEntity> result = unitUnderTest.fireNamedQuery("getMockEntityByName", testChars);

        trans.commit();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(testChars, ((MockNazgulEntity) result.get(0)).getValue());
    }

    //
    // Private helpers
    //

    private EntityManager getEntityManager(final String persistenceUnitName) {

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        return emf.createEntityManager();
    }
}
