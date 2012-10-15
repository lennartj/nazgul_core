/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ConstantIdGeneratorTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyID() {

        // Act & Assert
        new ConstantIdGenerator("");
    }

    @Test
    public void validateIdenticalIdReturned() {

        // Act & Assert
        Assert.assertEquals("foo", new ConstantIdGenerator("foo").getIdentifier());
    }
}
