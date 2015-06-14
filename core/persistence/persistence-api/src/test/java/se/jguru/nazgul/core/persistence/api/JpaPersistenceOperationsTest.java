/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.api.helpers.MockNazgulEntity;
import se.jguru.nazgul.core.persistence.api.helpers.NamedParametersPerson;
import se.jguru.nazgul.core.persistence.api.helpers.ParameterMapBuilder;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaPersistenceOperationsTest extends AbstractInMemoryJpaTest {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(JpaPersistenceOperationsTest.class);

    @Override
    protected String getPersistenceFileName() {
        return "JpaOperationsPersistence";
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

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnJpaMergeOperationWithNullEntity() throws Exception {

        // Act & Assert
        unitUnderTest.update(null);
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
    public void validateJpaExceptionOnFindingNonEntityByPK() throws Exception {

        // Assemble
        final Class<MockNazgulEntity> mockNazgulEntityClass = MockNazgulEntity.class;
        final Integer mockPrimaryKey = 42;
        final EntityManager mockEntityManager = EasyMock.createMock(EntityManager.class);
        final MockNazgulEntity mockNazgulEntity = new MockNazgulEntity("foobar!");
        mockNazgulEntity.throwValidationException = true;

        EasyMock.expect(mockEntityManager.find(mockNazgulEntityClass, mockPrimaryKey)).andReturn(mockNazgulEntity);
        EasyMock.replay(mockEntityManager);

        final JpaPersistenceOperations jpa = new JpaPersistenceOperations(mockEntityManager);

        // Act & Assert
        jpa.findByPrimaryKey(mockNazgulEntityClass, mockPrimaryKey);
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

        final List<NazgulEntity> result = unitUnderTest.fireNamedQueryWithResultLimit("getMockEntityByName",
                5, testChars);

        final List<NazgulEntity> result2 = unitUnderTest.fireNamedQuery("getMockEntityByName", testChars);

        trans.commit();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        final String value = ((MockNazgulEntity) result.get(0)).getValue();
        Assert.assertEquals("Expected [" + testChars + "], Actual [" + value + "]",
                testChars, value);

        Assert.assertEquals(1, result2.size());
        final String value2 = ((MockNazgulEntity) result2.get(0)).getValue();
        Assert.assertEquals("Expected [" + testChars + "], Actual [" + value2 + "]",
                testChars, value);
    }

    @Test
    public void validateNamedParametersInJpqlNamedQueries() throws Exception {

        // Assemble
        final String expected = "Lennart";
        final Map<String, Object> namedParameters = ParameterMapBuilder.with("firstName", "Lenn%").build();

        // Act
        unitUnderTest.create(new NamedParametersPerson(expected, 45));
        unitUnderTest.create(new NamedParametersPerson("Malin", 25));
        unitUnderTest.create(new NamedParametersPerson("Ida", 16));

        trans.commit();
        trans.begin();

        final List<NamedParametersPerson> result = unitUnderTest.fireNamedQueryWithResultLimit("getPersonsByFirstName",
                5, namedParameters);

        final NamedParametersPerson found = unitUnderTest.findByPrimaryKey(NamedParametersPerson.class, 1l);
        trans.commit();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        final String value = result.get(0).getFirstName();
        Assert.assertEquals("Expected [" + expected + "], Actual [" + value + "]",
                expected, value);

        Assert.assertNotNull("found null NPP by primary key.", found);
    }

    //
    // Private helpers
    //

    private EntityManager getEntityManager(final String persistenceUnitName) {

        // Add Eclipselink special settings
        final Map<String, String> extraProperties = new TreeMap<String, String>();
        extraProperties.put("eclipselink.persistencexml", persistenceXmlFile);
        // System.out.println("extraProperties [" + extraProperties + "]");

        // Create an EntityManager factory.
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, extraProperties);
        return emf.createEntityManager();
    }
}
