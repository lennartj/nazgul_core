/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees.common;

import junit.framework.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SemanticPathTest {

    enum SegmentDefinition {
        TOP, MIDDLE, BOTTOM;

        /**
         * @return the lower-case name of this enum constant
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Test
    public void validateSemanticPathConcept() {

        // Assemble
        final EnumMap<SegmentDefinition, String> pathDefinition =
                getEnumMap(SegmentDefinition.class, String.class);
        for(SegmentDefinition current : SegmentDefinition.values()) {
            pathDefinition.put(current, "key_" + current);
        }

        // Act
        final String key = pathDefinition.get(SegmentDefinition.BOTTOM);

        // Assert
        Assert.assertEquals("key_bottom", key);
    }

    //
    // Private helpers
    //

    private <E extends Enum<E>, KeyType extends Serializable> EnumMap<E, KeyType> getEnumMap(
            Class<E> enumType, Class<KeyType> keyTypeClass) {
        return new EnumMap<E, KeyType>(enumType);
    }

    // Path: List<KeyType> [non-semantic], alt. EnumMap<E, KeyType> [semantic]
    // Node: KeyType --> ValueType
    // Tree: Set<Node>, i.e.
    // private <E extends Enum<E>, KeyType extends Serializable, ValueType extends Serializable, Comparable>
}
