/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.io.Serializable;
import java.util.List;

/**
 * Specification for a Node whose internal state can be manipulated, in terms
 * of moving it within trees, and adding/removing children.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MutableNode<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable>
        extends Node<KeyType, ValueType> {

    /**
     * @return A List holding the immediate child nodes of this Node.
     *         The returned List should be modifiable.
     */
    <X extends Node<KeyType, ValueType>> List<X> getChildren();

    /**
     * Adds the provided node as a child to this MutableNode.
     *
     * @param node The node to add.
     * @throws IllegalArgumentException if the given node could not be added properly.
     */
    void addChild(MutableNode<KeyType, ValueType> node) throws IllegalArgumentException;

    /**
     * Removes all immediate children of this MutableNode matching the provided Filter.
     *
     * @param nodeFilter a Filter defining which children should be removed.
     */
    void removeChildren(Filter<Node<KeyType, ValueType>> nodeFilter);

    /**
     * Removes the provided child node from this MutableNode, if it exists.
     * If the provided node is not a child node of this MutableNode, this method
     * should return silently.
     *
     * @param node The node to remove.
     */
    void removeChild(MutableNode<KeyType, ValueType> node);

    /**
     * Assigns the parent of this MutableNode.
     *
     * @param parent The parent of this node.
     */
    void setParent(MutableNode<KeyType, ValueType> parent);

    /**
     * Removes this MutableNode from its parent, by removing both the
     * reference to the parent from this MutableNode and the reference
     * from the parent MutableNode to this MutableNode (in its list of children).
     */
    void clearParent();
}
