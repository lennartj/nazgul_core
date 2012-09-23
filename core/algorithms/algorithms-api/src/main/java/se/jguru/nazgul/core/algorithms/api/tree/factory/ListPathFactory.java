/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree.factory;


import se.jguru.nazgul.core.algorithms.api.tree.ListPath;
import se.jguru.nazgul.core.algorithms.api.tree.Path;

import java.io.Serializable;

/**
 * PathFactory implementation returning ListPath instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ListPathFactory<KeyType extends Serializable & Comparable<KeyType>> implements PathFactory<KeyType> {

    /**
     * Creates a Path holding a single segment being the provided key.
     *
     * @param key The key to make a Path from.
     * @return A Path holding the provided key.
     */
    @Override
    public Path<KeyType> create(final KeyType key) {
        return new ListPath<KeyType>(key);
    }
}
