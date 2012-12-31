/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TreeAlgorithmsTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClassArgument() {

        // Assemble
        Class<Adjustment> clazz = null;

        // Act & Assert
        EnumMap<Adjustment, String> dummy = TreeAlgorithms.getEmptyEnumMap(clazz);
    }

    @Test
    public void validateCorrectlyCreatedEmptyEnumMap() {

        // Act
        final EnumMap<Adjustment, String> emptyEnumMap = TreeAlgorithms.getEmptyEnumMap(Adjustment.class);
        final Iterator<Adjustment> it = emptyEnumMap.keySet().iterator();

        // Assert
        for (Adjustment current : Adjustment.values()) {
            Assert.assertEquals(current, it.next());
        }
    }
}
