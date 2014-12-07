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
package se.jguru.nazgul.test.persistence.pets;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.persistence.api.PersistenceOperationException;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.test.persistence.StandardPersistenceTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaRelationsTest extends StandardPersistenceTest {

    private static final String[] birdTableName = new String[] {"BIRD"};

    @Test
    public void validateCreateJpaRelationEntities() throws Exception {

        // Assemble
        final Bird eagle = new Bird("Eagle", "Predator");
        final Bird hawk = new Bird("Hawk", "Predator");

        final List<Bird> oatsEatenBy = new ArrayList<Bird>();
        oatsEatenBy.add(eagle);
        oatsEatenBy.add(hawk);

        final List<Bird> barleyEatenBy = new ArrayList<Bird>();
        barleyEatenBy.add(hawk);

        final Seed oats = new Seed("Oats", "Grain", oatsEatenBy);
        final Seed barley = new Seed("Barley", "Grain", barleyEatenBy);

        final IDataSet expected = performStandardTestDbSetup("validateCreateJpaRelationEntities");

        // Act
        jpa.create(eagle);
        jpa.create(hawk);

        jpa.create(oats);
        jpa.create(barley);

        entityManager.flush();
        commitAndStartNewTransaction();

        // final IDataSet dbDataSet = iDatabaseConnection.createDataSet(new String[]{"BIRD", "SEED", "SEED_BIRD"});
        // System.out.println("Got: " + extractFlatXmlDataSet(dbDataSet));

        final List<Seed> seeds = jpa.fireNamedQuery(Seed.GET_SEEDS_BY_BIRD_NAME, "Hawk");

        // Barley --> Hawk
        // Oats   --> Eagle, Hawk

        // Assert
        Assert.assertEquals(2, seeds.size());
        final Map<String, Seed> namedSeedsMap = new TreeMap<String, Seed>();
        for(Seed current : seeds) {
            namedSeedsMap.put(current.getName(), current);
        }

        final Seed oatsSeed = namedSeedsMap.get("Oats");
        final Seed barleySeed = namedSeedsMap.get("Barley");

        Assert.assertNotNull(oatsSeed);
        Assert.assertNotNull(barleySeed);

        final List<Bird> oatsEatenByList = oatsSeed.getEatenBy();
        Assert.assertEquals(2, oatsEatenByList.size());
        final List<String> birdNames = Arrays.asList("Eagle", "Hawk");
        for(Bird current : oatsEatenByList) {
            Assert.assertTrue(birdNames.contains(current.getName()));
        }
    }

    @Test
    public void validateDeleteEntity() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup("validateDeleteEntity");

        // Act & Assert #1: Readout one of the Bird records from the DB.
        final List<Bird> result = jpa.fireNamedQuery(Bird.GET_BIRDS_BY_CATEGORY, "Preda%");
        Assert.assertEquals(2, result.size());

        final Bird toDelete = result.get(0);
        Assert.assertEquals("Eagle", toDelete.getName());

        final List<Seed> seedsEatenByBird = jpa.fireNamedQuery(Seed.GET_SEEDS_BY_BIRD_NAME, toDelete.getName());
        Assert.assertEquals(1, seedsEatenByBird.size());
        seedsEatenByBird.iterator().next().getEatenBy().remove(toDelete);

        // Act & Assert #2: Delete the entity from the database
        jpa.delete(toDelete);
        commitAndStartNewTransaction();

        // Assert
        final IDataSet dbDataSet2 = iDatabaseConnection.createDataSet(); // birdTableName
        final ITable birdTable = dbDataSet2.getTable(birdTableName[0]);
        // System.out.println("Got: " + extractFlatXmlDataSet(dbDataSet2));

        Assert.assertEquals(1, birdTable.getRowCount());
        Assert.assertEquals("Hawk", "" + birdTable.getValue(0, "NAME"));
        Assertion.assertEquals(expected, dbDataSet2);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnDeletingEntityWithForeignKeys() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup("validateDeleteEntity");

        // Act & Assert #1: Readout one of the Bird records from the DB.
        final List<Bird> result = jpa.fireNamedQuery(Bird.GET_BIRDS_BY_CATEGORY, "Preda%");
        Assert.assertEquals(2, result.size());

        final Bird toDelete = result.get(0);
        Assert.assertEquals("Eagle", toDelete.getName());

        // Act & Assert #2: Delete the entity from the database
        jpa.delete(toDelete);
        commitAndStartNewTransaction();

        /*
        Exception [EclipseLink-4002] (Eclipse Persistence Services - 2.5.1.v20130918-f2b9fc5):
        org.eclipse.persistence.exceptions.DatabaseException
        Internal Exception: java.sql.SQLIntegrityConstraintViolationException:
        integrity constraint violation: foreign key no action; FK_SEED_BIRD_EATENBY_ID table: SEED_BIRD
         */
    }

    @Test
    public void validateUpdateEntity() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup("validateUpdateEntity");

        // Act & Assert #1: Readout some Bird records from the DB.
        final List<Bird> result = jpa.fireNamedQuery(Bird.GET_BIRDS_BY_CATEGORY, "Pred%");
        Assert.assertEquals(2, result.size());

        // Since the NamedQuery is sorted by name, the first bird should be the Eagle.
        final Bird toUpdate = result.get(0);
        Assert.assertEquals("Eagle", toUpdate.getName());

        // Act #2: Update the entity, then merge it into the new DB transaction and sync to the DB.
        commitAndStartNewTransaction();
        toUpdate.setName("Falcon");
        final Bird updatedEntity = jpa.update(toUpdate);
        entityManager.flush();

        // Assert
        Assert.assertEquals("Falcon", updatedEntity.getName());
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet());
    }
}
