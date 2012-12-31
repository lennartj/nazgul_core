/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.api.transaction;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractTransactedActionTest {

    @Test
    public void validateCorrectExceptionMessage() {

        // Assemble
        final String message = "Test Exception";
        final AbstractTransactedAction unitUnderTest = new AbstractTransactedAction(message) {

            @Override
            public void doInTransaction() throws RuntimeException {
                // Ignored in this test
            }
        };

        // Act
        final String result = unitUnderTest.getRollbackErrorDescription();
        unitUnderTest.doInTransaction();
        unitUnderTest.onRollback();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(message, result);
    }
}
