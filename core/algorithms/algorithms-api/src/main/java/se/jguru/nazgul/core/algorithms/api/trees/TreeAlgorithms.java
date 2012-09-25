/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.factory.PathFactory;

import java.io.Serializable;
import java.util.Collection;
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

    /**
     * Creates a Path of the provided KeyType from the elements within the given collection,
     * using the factory to create the initial Path element.
     *
     * @param collection The collection used to acquire the elements within the Path.
     * @param factory    The factory used to create the first Path element.
     * @param <KeyType>  The type of key in the returned Path.
     * @param <C>        The type of collection from which the path segments should be acquired.
     * @return The resulting Path.
     */
    public static <KeyType extends Serializable & Comparable<KeyType>, C extends Collection<KeyType>>
    Path<KeyType> getPath(final C collection, final PathFactory<KeyType> factory) {

        // Check sanity
        Validate.notNull(collection, "Cannot handle null collection argument.");

        Path<KeyType> currentPath = null;

        for (KeyType current : collection) {

            if (currentPath == null) {

                // Create the first segment in the path.
                currentPath = factory.create(current);
            } else {

                // Append the current segment to the path.
                currentPath = currentPath.append(current);
            }
        }

        // All done.
        return currentPath;
    }


}
