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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.test.persistence;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.persistence.pets.Bird;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockStandardPersistenceTest extends StandardPersistenceTest {

    @Test
    public void validateCreateEntity() throws Exception {

        // Assemble
        final Bird eagle = new Bird("Eagle", "Predator");

        final IDataSet expected = performStandardTestDbSetup("validateCreateEntity");

        // Act
        jpa.create(eagle);

        commitAndStartNewTransaction();
        final List<Bird> birds = jpa.fireNamedQuery(Bird.GET_ALL_BIRDS);

        final IDataSet dbDataSet = iDatabaseConnection.createDataSet(new String[]{"BIRD"});

        // Assert
        Assert.assertEquals(2, birds.size());
        Assertion.assertEquals(expected, dbDataSet);
    }

    /*
    @Test
    public void validateDeleteEntity() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup("validateDeleteEntity");

        // Act & Assert #1: Readout one of the Person instances from the DB.
        final List<Person> result = jpa.fireNamedQuery(Person.GET_PEOPLE_BY_LAST_NAME, "Old%");
        Assert.assertEquals(1, result.size());

        final Person toDelete = result.get(0);
        Assert.assertEquals("Ove", toDelete.getFirstName());

        // Act & Assert #2: Delete the entity from the database
        jpa.delete(toDelete);
        commitAndStartNewTransaction();

        // Assert
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet(new String[]{"PERSON"}));
    }

    @Test
    public void validateUpdateEntity() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup("validateUpdateEntity");

        // Act & Assert #1: Readout one of the Person instances from the DB.
        final List<Person> result = jpa.fireNamedQuery(Person.GET_PEOPLE_BY_LAST_NAME, "Jöre%");
        Assert.assertEquals(1, result.size());

        final Person toUpdate = result.get(0);
        Assert.assertEquals("Lennart", toUpdate.getFirstName());

        // Act & Assert #2: Update the entity within the database
        toUpdate.setShoeSize(52);
        final Person updatedEntity = jpa.update(toUpdate);
        commitAndStartNewTransaction();

        // Assert
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet(new String[]{"PERSON"}));
        Assert.assertEquals(52, updatedEntity.getShoeSize());
    }
    */
}
