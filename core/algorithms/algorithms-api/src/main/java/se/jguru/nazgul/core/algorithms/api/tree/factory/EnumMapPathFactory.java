/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree.factory;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.tree.EnumMapPath;
import se.jguru.nazgul.core.algorithms.api.tree.Path;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * PathFactory implementation returning EnumMapPath instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPathFactory<E extends Enum<E>, KeyType extends Serializable & Comparable<KeyType>>
        implements PathFactory<KeyType> {

    // Internal state
    private Class<E> enumType;
    private EnumMap<E, KeyType> enumMap;

    /**
     * Creates a new EnumMapPathFactory instance, using the provided
     * Enum class to create the Path instance.
     *
     * @param enumType The Enum type used by this EnumMapPathFactory.
     */
    public EnumMapPathFactory(final Class<E> enumType) {

        // Check sanity
        Validate.notNull(enumType, "Cannot handle null enumType instance.");
        Validate.notEmpty(enumType.getEnumConstants(), "Cannot handle empty enumType; "
                + "class [" + enumType.getName() +  "] must contain defined enum elements.");

        // Assign internal state
        this.enumType = enumType;
        this.enumMap = new EnumMap<E, KeyType>(enumType);
        for(E current : enumType.getEnumConstants()) {
            enumMap.put(current, null);
        }
    }

    /**
     * Creates a Path holding a single segment being the provided key.
     *
     * @param key The key to make a Path from.
     * @return A Path holding the provided key.
     */
    @Override
    public Path<KeyType> create(final KeyType key) {

        // Clone the template enumMap, and put the provided key as the
        // first element within the Path returned.
        final EnumMap<E, KeyType> templateClone = enumMap.clone();
        final E firstSemanticType = templateClone.keySet().iterator().next();
        templateClone.put(firstSemanticType, key);

        // Package and return.
        return new EnumMapPath<E, KeyType>(templateClone, enumType);
    }
}
