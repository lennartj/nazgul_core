/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import java.io.Serializable;
import java.util.List;

/**
 * Generic and typesafe Node definition, for Tree structures.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Node<T extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        extends Comparable<Node<T, KeyType>>, Serializable {

    /**
     * @return An unmodifiable List holding the immediate child nodes of this Node.
     */
    <X extends Node<T, KeyType>> List<X> getChildren();

    /**
     * @return The parent of this Node.
     */
    <X extends Node<T, KeyType>> X getParent();

    /**
     * @return The data of this Node.
     */
    T getData();

    /**
     * @return The key of this Node.
     */
    KeyType getKey();

    /**
     * @return The Path (from the Tree root) of this Node.
     */
    Path<KeyType> getPath();
}
