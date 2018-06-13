/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.tree.model.path;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.persistence.StandardPersistenceTest;

import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringPathPersistenceTest extends StandardPersistenceTest {

    @Test
    public void validatePersistingStringPath() throws Exception {

        // Assemble
        StringPath unitUnderTest = new StringPath("root");
        unitUnderTest = unitUnderTest.append("intermediate");
        unitUnderTest = unitUnderTest.append("leaf");
        final IDataSet expected = performStandardTestDbSetup();

        // Act
        entityManager.persist(unitUnderTest);
        entityManager.flush();
        commitAndStartNewTransaction();

        final Query jpql = entityManager.createQuery("select e from StringPath e");
        final List<StringPath> resultList = jpql.getResultList();

        // Assert
        logIDataSets(expected, iDatabaseConnection.createDataSet());
        Assert.assertEquals(1, resultList.size());

        final StringPath readFromDb = resultList.get(0);
        Assert.assertEquals("root", readFromDb.get(0));
        Assert.assertEquals("intermediate", readFromDb.get(1));
        Assert.assertEquals("leaf", readFromDb.get(2));

        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet(new String[]{"STRINGPATH"}));
    }

    @Test
    public void validateDeleteStringPath() throws Exception {

        // Assemble
        final String pathToDelete = "top/middle/cellar";
        final IDataSet expected = performStandardTestDbSetup();

        // ### 1) Act & Assert: Ensure that the database contains the Entity to remove.
        final Query getStringPathByCompoundPath = entityManager.createNamedQuery("getStringPathByCompoundPath");
        getStringPathByCompoundPath.setParameter(1, pathToDelete);

        final List<StringPath> resultList = getStringPathByCompoundPath.getResultList();
        Assert.assertEquals(1, resultList.size());
        entityManager.flush();

        final StringPath cellarPath = resultList.get(0);
        Assert.assertNotNull(cellarPath);
        Assert.assertEquals(pathToDelete, cellarPath.toString());

        // ### 2) Act & Assert: Delete the retrieved StringPath.
        entityManager.remove(cellarPath);
        entityManager.flush();
        commitAndStartNewTransaction();

        // Assert
        logIDataSets(expected, iDatabaseConnection.createDataSet(new String[]{"STRINGPATH"}));
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet(new String[]{"STRINGPATH"}));
    }

    @Test
    public void validateFindingStringPath() throws Exception {

        // Assemble
        final String compoundPath = "top#middle#cellar";
        performStandardTestDbSetup();

        // Act
        final List<StringPath> resultList = jpa.fireNamedQuery("getStringPathByCompoundPath", compoundPath);

        // Assert
        Assert.assertEquals(1, resultList.size());

        final StringPath cellarPath = resultList.get(0);
        Assert.assertNotNull(cellarPath);
        Assert.assertEquals(compoundPath, cellarPath.toString());
    }

    //
    // Private helpers
    //

    private void logIDataSets(final IDataSet expected, final IDataSet actual) {
        System.out.println(" ===== expected: \n" + extractFlatXmlDataSet(expected)
                + "\n ===== actual: \n" + extractFlatXmlDataSet(actual)
                + "\n =====");
    }
}
