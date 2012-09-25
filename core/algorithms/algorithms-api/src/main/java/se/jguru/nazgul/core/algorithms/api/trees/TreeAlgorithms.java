/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * A suite of frequently used functional algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class TreeAlgorithms {

    /**
     * Retrieves a semi-populated EnumMap with all keys in ordinal order, and all values {@code null}.
     *
     * @param keyType The type of key used within the returned EnumMap.
     * @param <K>     The key class.
     * @param <V>     The value class.
     * @return a semi-populated EnumMap with all keys in ordinal order, and all values {@code null}.
     */
    public static <K extends Enum<K>, V extends Serializable & Comparable<V>> EnumMap<K, V>
    getEmptyEnumMap(final Class<K> keyType) {

        EnumMap<K, V> toReturn = new EnumMap<K, V>(keyType);

        for (K current : keyType.getEnumConstants()) {
            toReturn.put(current, null);
        }

        return toReturn;
    }
}
