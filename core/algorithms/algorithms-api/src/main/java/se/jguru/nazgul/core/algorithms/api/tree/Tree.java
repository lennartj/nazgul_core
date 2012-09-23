/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import java.io.Serializable;

/**
 * A generic typed Tree structure holding nodes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Tree<T extends Serializable, KeyType extends Serializable & Comparable<KeyType>> {

    /**
     * @return The root node of this tree.
     */
    Node<T, KeyType> getRoot();

    /**
     * Retrieves the Node at the provided path.
     *
     * @param path The path to the node which should be acquired.
     * @return The Node at the provided path, or {@code null} if no node was found.
     */
    Node<T, KeyType> get(Path<KeyType> path);
}
