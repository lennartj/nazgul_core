/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.event.api.consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractEventConsumerTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullId() {

        // Assemble
        final String incorrectNull = null;

        // Act
        new MockEventConsumer(incorrectNull);
    }

    @Test
    public void validateIdentityHandling() {

        // Assemble
        final String id = "FooBar!";
        final MockEventConsumer unitUnderTest = new MockEventConsumer(id);

        // Act & Assert
        Assert.assertEquals(id, unitUnderTest.getClusterId());
    }
}
