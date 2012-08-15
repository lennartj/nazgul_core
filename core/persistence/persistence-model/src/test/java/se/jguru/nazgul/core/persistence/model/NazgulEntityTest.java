/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.persistence.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulEntityTest {

    @Test
    public void validateCreation() {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);

        // Act
        // ... do nothing...

        // Assert
        final ArrayList<String> callTrace = mockEntity.callTrace;
        Assert.assertTrue("callTrace size: " + callTrace.size(), callTrace.size() > 0);
    }

    @Test
    public void validateEqualityAndIdentity() {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);
        final MockEntity mockEntity2 = new MockEntity(name, value);

        // Act
        final boolean equalityByComparison = mockEntity.equals(mockEntity2);
        final boolean equalityByNull = mockEntity.equals(null);
        final boolean hashCodeEqual = mockEntity.hashCode() == mockEntity2.hashCode();

        // Assert
        Assert.assertTrue(equalityByComparison);
        Assert.assertFalse(equalityByNull);
        Assert.assertTrue(hashCodeEqual);
    }

    @Test
    public void validateCloningAndCopying() throws Exception {

        // Assemble
        final String name = "foobar";
        final Long value = 42L;
        final MockEntity mockEntity = new MockEntity(name, value);

        // Act

        // Assert
        Assert.assertEquals(mockEntity, mockEntity.copy());
        Assert.assertEquals(mockEntity, mockEntity.clone());
        Assert.assertNotSame(mockEntity, mockEntity.copy());
    }
}
