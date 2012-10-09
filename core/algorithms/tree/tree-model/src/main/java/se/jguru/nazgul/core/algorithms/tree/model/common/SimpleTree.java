/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.MutableTree;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MutableTree implementation which only holds a root node.
 * All mutability operations except the get method are delegated to the Node.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SimpleTree<ValueType extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        implements MutableTree<ValueType, KeyType>, Serializable {

    // Internal state
    private final Object lock = new Object();
    private MutableNode<ValueType, KeyType> root;

    /**
     * Creates a SimpleTree instance with the provided root node.
     *
     * @param root The root node within the tree.
     */
    public SimpleTree(final MutableNode<ValueType, KeyType> root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");

        // Assign internal state
        this.root = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableNode<ValueType, KeyType> setRoot(final MutableNode<ValueType, KeyType> root)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");

        // Assign internal state
        MutableNode<ValueType, KeyType> oldRoot = this.root;

        // Ensure all children can be moved.
        List<MutableNode<ValueType, KeyType>> mutableChildren = new ArrayList<MutableNode<ValueType, KeyType>>();
        for (Node<ValueType, KeyType> current : oldRoot.getChildren()) {
            if (current instanceof MutableNode) {
                mutableChildren.add((MutableNode<ValueType, KeyType>) current);
            } else {
                throw new IllegalStateException("Existing child [" + current + "] was not a MutableNode, " +
                        "so cannot be moved from old root node [" + oldRoot + "]. Aborting setRoot.");
            }
        }

        // Move the children from oldRoot to newRoot
        synchronized (lock) {
            for (MutableNode<ValueType, KeyType> current : mutableChildren) {
                root.addChild(current);
            }

            // Actually switch the root node as well.
            this.root = root;
        }

        // All done.
        return oldRoot;
    }

    /**
     * @return The root node of this tree.
     */
    @Override
    public MutableNode<ValueType, KeyType> getRoot() {
        return root;
    }

    /**
     * Retrieves the Node at the provided path.
     *
     * @param <X>  The Node (or subtype thereof, such as {@code MutableNode}) returned.
     * @param path The path to the node which should be acquired.
     * @return The Node at the provided path, or {@code null} if no node was found.
     */
    @Override
    public <X extends Node<ValueType, KeyType>> X get(final Path<KeyType> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");
        Validate.isTrue(path.size() > 0, "Cannot handle empty path argument.");
        if (!path.get(0).equals(getRoot().getKey())) {
            return null;
        }

        Node<ValueType, KeyType> currentNode = getRoot();
        for (int depth = 1; depth < path.size(); depth++) {

            final Node<ValueType, KeyType> child = findChild(currentNode, path.get(depth));

            if (child == null) {
                currentNode = null;
                break;
            } else {
                currentNode = child;
            }
        }

        // All done.
        return (X) currentNode;
    }

    //
    // Private helpers
    //

    private Node<ValueType, KeyType> findChild(final Node<ValueType, KeyType> node, final KeyType key) {

        for (Node<ValueType, KeyType> current : node.getChildren()) {
            if (current.getKey().equals(key)) {
                return current;
            }
        }

        // No immediate child found.
        return null;
    }
}
