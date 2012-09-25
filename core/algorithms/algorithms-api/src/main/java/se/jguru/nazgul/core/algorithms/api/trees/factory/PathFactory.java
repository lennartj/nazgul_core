/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees.factory;

import se.jguru.nazgul.core.algorithms.api.trees.Path;

import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PathFactory<KeyType extends Serializable & Comparable<KeyType>> {

    /**
     * Creates a Path holding a single segment being the provided key.
     *
     * @param key The key to make a Path from.
     * @return A Path holding the provided key.
     */
    Path<KeyType> create(KeyType key);
}
