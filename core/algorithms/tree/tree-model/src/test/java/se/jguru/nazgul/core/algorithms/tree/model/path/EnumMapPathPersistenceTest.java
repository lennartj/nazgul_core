/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
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
package se.jguru.nazgul.core.algorithms.tree.model.path;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.helpers.CityIdentifier;
import se.jguru.nazgul.core.algorithms.tree.model.path.EnumMapPath;
import se.jguru.nazgul.test.persistence.StandardPersistenceTest;

import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPathPersistenceTest extends StandardPersistenceTest {

    @Test
    public void validatePersistingEnumMapPath() throws Exception {

        // Assemble
        EnumMapPath<CityIdentifier> unitUnderTest = new EnumMapPath<CityIdentifier>("Sweden", CityIdentifier.class);
        unitUnderTest = unitUnderTest.append("Västra Götaland");
        unitUnderTest = unitUnderTest.append("Göteborg");
        final IDataSet expected = performStandardTestDbSetup("validatePersistingEnumMapPath");

        // Act
        entityManager.persist(unitUnderTest);
        entityManager.flush();
        commitAndStartNewTransaction();

        final Query jpql = entityManager.createNamedQuery("getEnumMapPathByCompoundPath");
        jpql.setParameter(1, "Sweden/Västra Götaland/Göteborg");
        final List<EnumMapPath<CityIdentifier>> resultList = jpql.getResultList();

        // Assert
        final IDataSet dbDataSet = iDatabaseConnection.createDataSet();
        // System.out.println("Got: " + extractFlatXmlDataSet(dbDataSet));
        // Assert.assertEquals(1, resultList.size());

        final EnumMapPath<CityIdentifier> readFromDb = resultList.get(0);
        Assert.assertEquals("Sweden", readFromDb.get(CityIdentifier.COUNTRY));
        Assert.assertEquals("Västra Götaland", readFromDb.get(CityIdentifier.REGION));
        Assert.assertEquals("Göteborg", readFromDb.get(CityIdentifier.CITY));

        Assertion.assertEquals(expected, dbDataSet);
    }
}
