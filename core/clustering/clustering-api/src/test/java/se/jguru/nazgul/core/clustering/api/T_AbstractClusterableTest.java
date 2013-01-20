/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.clustering.api;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractClusterableTest {

    @Test
    public void validateDelegatedIdRetrieval() {

        // Assemble
        final MockIdGenerator idGenerator = new MockIdGenerator("goo");
        idGenerator.idAvailable = false;

        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator, "bar", 42);

        // Act
        idGenerator.idAvailable = true;

        // Assert
        for(int i = 0; i < 10; i++) {
            Assert.assertEquals("goo_1", unitUnderTest.getId());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNoIdAvailableInIdGenerator() {

        // Assemble
        final MockIdGenerator idGenerator = new MockIdGenerator("goo");
        idGenerator.idAvailable = false;

        final TestAbstractSwiftClusterable unitUnderTest = new TestAbstractSwiftClusterable(idGenerator, "bar", 42);

        // Act & Assert
        unitUnderTest.getId();
    }
}
