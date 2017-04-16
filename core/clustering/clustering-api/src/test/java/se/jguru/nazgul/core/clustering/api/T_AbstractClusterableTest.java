/*
 * #%L
 * Nazgul Project: nazgul-core-clustering-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
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
    private static final String PREFIX = "goo";
    private MockIdGenerator idGenerator;

    @Before
    public void setupSharedState() {

        this.idGenerator = new MockIdGenerator(PREFIX);
        MockIdGenerator.counter.set(0);
    }

    @Test
    public void validateIdGeneratorIsNullAfterDeliveringID() {

        // Assemble
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator,
                true,
                "bar",
                42);

        // Act
        final String clusterId = unitUnderTest.getClusterId();
        final String clusterId2 = unitUnderTest.getClusterId();

        // Assert
        Assert.assertNotNull(clusterId);
        Assert.assertNotNull(clusterId2);
        Assert.assertEquals(clusterId, clusterId2);
        Assert.assertNull(unitUnderTest.idGenerator);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNoIdAvailableInIdGenerator() {

        // Assemble
        final MockIdGenerator idGenerator = new MockIdGenerator("goo");
        idGenerator.idAvailable = false;

        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator,
                false,
                "bar",
                42);

        // Act & Assert
        unitUnderTest.getClusterId();
        Assert.assertNotNull(unitUnderTest.idGenerator);
    }

    @Test
    public void validateNullIdGeneratorSetWhenSupplyingStringId() {

        // Assemble
        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable("constantId",
                true,
                "bar",
                42);

        // Act
        final IdGenerator result = unitUnderTest.idGenerator;

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateEquality() {

        // Assemble
        final TestAbstractSwiftClusterable c1 = new TestAbstractSwiftClusterable("id1",
                true,
                "bar",
                42);
        final TestAbstractSwiftClusterable c2 = new TestAbstractSwiftClusterable("id2",
                true,
                "bar",
                42);

        // Act
        final int c1HashCode = c1.hashCode();
        final int c2HashCode = c2.hashCode();
        final boolean equality = c1.equals(c2);

        // Assert
        Assert.assertNotEquals(c1HashCode, c2HashCode);
        Assert.assertFalse(equality);
    }
}
