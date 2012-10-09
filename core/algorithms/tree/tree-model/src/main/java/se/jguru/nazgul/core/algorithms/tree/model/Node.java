/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model;

import java.io.Serializable;
import java.util.List;

/**
 * Generic and typesafe Node definition, for Tree structures. A Node is similar to a Map.Entry,
 * in that it relates a Key (with a defined KeyType) to a Value (with a defined ValueType).
 * <p/>
 * To simplify Nodes and their corresponding features being read from and written to streams,
 * both the Node type itself and its KeyType/ValueType are {@code Serializable}. Moreover,
 * since some Tree algorithms require ordering of keys (and, thereby, nodes), the KeyType must also implement
 * {@code Comparable<KeyType>}.
 *
 * @param <ValueType> The type for the values of this Node.
 * @param <KeyType>   The type for the keys of this Node.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Node<ValueType extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        extends Comparable<Node<ValueType, KeyType>>, Serializable {

    /**
     * @return An unmodifiable List holding the immediate child nodes of this Node.
     */
    <X extends Node<ValueType, KeyType>> List<X> getChildren();

    /**
     * @return The parent of this Node.
     */
    <X extends Node<ValueType, KeyType>> X getParent();

    /**
     * @return The data of this Node.
     */
    ValueType getData();

    /**
     * @return The key of this Node.
     */
    KeyType getKey();

    /**
     * @param <X> The Path (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path (from the Tree root) of this Node.
     */
    <X extends Path<KeyType>> X getPath();
}
