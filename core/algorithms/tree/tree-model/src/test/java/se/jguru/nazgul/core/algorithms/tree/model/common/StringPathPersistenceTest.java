/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
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
package se.jguru.nazgul.core.algorithms.tree.model.common;

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
    public void validatePersistingStringPath() {

        // Assemble
        final StringPath unitUnderTest = new StringPath("root");
        final IDataSet expected = performStandardTestDbSetup("validatePersistingStringPath");

        // Act
        jpa.create(unitUnderTest);

        entityManager.flush();
        commitAndStartNewTransaction();

        // final IDataSet dbDataSet = iDatabaseConnection.createDataSet(new String[]{"BIRD", "SEED", "SEED_BIRD"});
        // System.out.println("Got: " + extractFlatXmlDataSet(dbDataSet));
        final Query jpql = entityManager.createQuery("select e from StringPath e");
        final List<StringPath> resultList = jpql.getResultList();

        // Assert
        Assert.assertEquals(1, resultList.size());
    }
}
