/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
        for(int i = 0; i < 10; i++) {
            Assert.assertEquals("goo_1", unitUnderTest.getClusterId());
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
