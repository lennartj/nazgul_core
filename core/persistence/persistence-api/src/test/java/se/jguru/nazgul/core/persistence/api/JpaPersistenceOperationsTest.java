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
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaPersistenceOperationsTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(JpaPersistenceOperationsTest.class);

    private static final String JPA_SPECIFICATION_PROPERTY = "jpaSpec";
    private ClassLoader originalClassLoader;
    private EntityManager unitTestEM;
    private EntityTransaction trans;
    private JpaPersistenceOperations unitUnderTest;
    private String persistenceXmlFile;
    private String jpaSpecification;

    @Before
    public void stashOriginalClassLoader() {

        originalClassLoader = getClass().getClassLoader();

        // Find the JPA specification used, and use openjpa2 as the default.
        jpaSpecification = System.getProperty(JPA_SPECIFICATION_PROPERTY, "openjpa2");
        persistenceXmlFile = "testdata/JpaOperationsPersistence_" + jpaSpecification + ".xml";
        System.out.println("jpaSpec: [" + jpaSpecification + "]  ==> persistenceXmlFile: [" + persistenceXmlFile + "]");

        final PersistenceRedirectionClassLoader redirectionClassLoader =
                new PersistenceRedirectionClassLoader(getClass().getClassLoader(), persistenceXmlFile);

        Thread.currentThread().setContextClassLoader(redirectionClassLoader);

        /*
        try {
            for (URL current : Collections.list(redirectionClassLoader.getResources("META-INF/MANIFEST.MF"))) {
                System.out.println("  [" + current + "]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not acquire ClassLoader resource list.", e);
        }
        */

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

            Query infoQ = unitTestEM.createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_NAME = 'MOCKNAZGULENTITY'");
            final List resultList = infoQ.getResultList();
            if (resultList.size() == 1) {

                Query q = unitTestEM.createNativeQuery("DROP TABLE MOCKNAZGULENTITY");
                int affectedRows = q.executeUpdate();
                log.info("Removed table. Rows affected [" + affectedRows + "]");

                if (trans.getRollbackOnly()) {
                    trans.rollback();
                } else {
                    trans.commit();
                }
            }
        } catch (final Exception e) {

            log.info("Could not commit the Transaction.", e);
        }

        try {

            // Close all the JPA resources.
            if (trans.isActive() && !trans.getRollbackOnly()) {
                trans.commit();
            }
            if (unitTestEM.isOpen()) {
                unitTestEM.flush();
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
        unitUnderTest.refresh(null);
        unitUnderTest.delete(null);
    }

    @Test
    public void validateJpaMergeOperationOnNullEntity() throws Exception {

        // This case is handled differently by the different JPA providers...
        if (jpaSpecification.equals("eclipselink2")) {
            try {
                unitUnderTest.update(null);
                Assert.fail("The EclipseLink2 throws a IllegalArgumentException when "
                        + "attempting to update (i.e. JPA merge) a null value.");
            } catch (PersistenceOperationException e) {
                Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
            }
        } else {
            unitUnderTest.update(null);
        }
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
        final String value = ((MockNazgulEntity) result.get(0)).getValue();
        Assert.assertEquals("Expected [" + testChars + "], Actual [" + value + "]",
                testChars, value);
    }

    //
    // Private helpers
    //

    private EntityManager getEntityManager(final String persistenceUnitName) {

        // Add Eclipselink special settings
        final Map<String, String> extraProperties = new TreeMap<String, String>();
        extraProperties.put("eclipselink.persistencexml", persistenceXmlFile);
        System.out.println("extraProperties [" + extraProperties + "]");

        // Create an EntityManager factory.
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, extraProperties);
        return emf.createEntityManager();
    }
}
