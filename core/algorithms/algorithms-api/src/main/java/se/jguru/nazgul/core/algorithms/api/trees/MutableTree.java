/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

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
     * Adds the provided Node to this Tree, rooted at the provided parentPath.
     *
     * @param aNode      The Node to add.
     * @param parentPath The Path where the Node should be added.
     * @throws IllegalArgumentException if the provided parentPath contained no [parent] Node.
     */
    void add(MutableNode<ValueType, KeyType> aNode, Path<KeyType> parentPath) throws IllegalArgumentException;

    /**
     * Removes the Node at the provided path.
     *
     * @param <X>  The Node (or subtype thereof, such as {@code MutableNode}) returned.
     * @param path The path to the node which should be removed.
     * @return The Node just removed, or {@code null} if no node was found at / removed from the provided path.
     */
    <X extends Node<ValueType, KeyType>> X remove(Path<KeyType> path);

    /**
     * Reassigns the root node of this MutableTree, and moves all existing (immediate)
     * children of the old root node to the new one. Following the completion of the
     * move, the old root node is returned.
     * <p/>
     * Both the new and old root node (as well as any immediate children to the old root node)
     * must be Mutable, unless no immediate children to the old root node exists.
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
