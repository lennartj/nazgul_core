/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model;

import java.io.Serializable;

/**
 * Specification for a Tree whose internal state can be manipulated, in terms
 * of adding/removing Nodes and re-assigning the root Node.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MutableTree<ValueType extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        extends Tree<ValueType, KeyType> {

    /**
     * Reassigns the root node of this MutableTree, and moves all existing (immediate)
     * children of the old root node to the new one. Following the completion of the
     * move, the old root node is returned.
     *
     * @param root The new root node.
     * @return The previous root node.
     * @throws IllegalArgumentException if the existing root Node has children and either
     *                                  the old root Node, the supplied (new) root Node or
     *                                  any existing children are not MutableNode instances.
     *                                  Also thrown if {@code root} has a parent node assigned.
     */
    MutableNode<ValueType, KeyType> setRoot(MutableNode<ValueType, KeyType> root) throws IllegalArgumentException;
}
