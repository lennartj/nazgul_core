/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class WellKnownNamespaceTest {

    // Shared state
    private WellKnownNamespace[] allNamespaces = WellKnownNamespace.values();

    @Test
    public void validateNamespaceDataIsNeitherNullNorEmpty() {

        // Assert
        for (WellKnownNamespace current : allNamespaces) {

            // Validate that the namespaceURL is not null or empty
            Assert.assertNotNull(current.getNameSpaceUrl());
            Assert.assertFalse("".equals(current.getNameSpaceUrl()));

            // Validate that the prefix is not null or empty
            Assert.assertNotNull(current.getXsdPrefix());
            Assert.assertFalse("".equals(current.getXsdPrefix()));
        }
    }
}
