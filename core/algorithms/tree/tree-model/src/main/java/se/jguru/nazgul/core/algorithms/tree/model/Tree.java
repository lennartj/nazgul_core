/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model;

import java.io.Serializable;

/**
 * A generic typed Tree structure holding nodes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Tree<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable> {

    /**
     * @return The root node of this tree.
     */
    MutableNode<KeyType, ValueType> getRoot();

    /**
     * Retrieves the Node at the provided path.
     *
     * @param <X>  The Node (or subtype thereof, such as {@code MutableNode}) returned.
     * @param path The path to the node which should be acquired.
     * @return The Node at the provided path, or {@code null} if no node was found.
     */
    <X extends Node<KeyType, ValueType>> X get(Path<KeyType> path);
}
