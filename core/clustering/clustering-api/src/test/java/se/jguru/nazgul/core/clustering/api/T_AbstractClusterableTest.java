/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
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

package se.jguru.nazgul.core.clustering.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractClusterableTest {

    // Shared state
    private final String prefix = "goo";
    private MockIdGenerator idGenerator;

    @Before
    public void setupSharedState() {

        this.idGenerator = new MockIdGenerator(prefix);
        MockIdGenerator.counter.set(0);
    }

    @Test
    public void validateDelegatedIdRetrieval() {

        // Assemble
        idGenerator.idAvailable = false;

        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator, "bar", 42);

        // Act
        idGenerator.idAvailable = true;

        // Assert
        String firstClusterId = null;
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(unitUnderTest.getClusterId().startsWith("goo_"));
            if (firstClusterId == null) {
                firstClusterId = unitUnderTest.getClusterId();
            }

            Assert.assertEquals(firstClusterId, unitUnderTest.getClusterId());
        }
        Assert.assertNotNull(unitUnderTest.getIdGenerator());
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNoIdAvailableInIdGenerator() {

        // Assemble
        final MockIdGenerator idGenerator = new MockIdGenerator("goo");
        idGenerator.idAvailable = false;

        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator, "bar", 42);

        // Act & Assert
        unitUnderTest.getClusterId();
        Assert.assertNotNull(unitUnderTest.getIdGenerator());
    }

    @Test
    public void validateNullIdGeneratorSetWhenSupplyingStringId() {

        // Assemble
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable("constantId", "bar", 42);

        // Act
        final IdGenerator result = unitUnderTest.getIdGenerator();

        // Assert
        Assert.assertNull(result);
    }
}
