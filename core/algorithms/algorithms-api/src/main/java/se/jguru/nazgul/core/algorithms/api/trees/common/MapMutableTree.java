/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.trees.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.MutableTree;
import se.jguru.nazgul.core.algorithms.api.trees.Node;
import se.jguru.nazgul.core.algorithms.api.trees.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MutableTree implementation harbouring a root Node instance, capable of containing children.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MapMutableTree<V extends Serializable, K extends Serializable & Comparable<K>>
        implements MutableTree<V, K>, Serializable {

    // Internal state
    private Node<V, K> root;
    private final Object lock = new Object();

    /**
     * Creates a new MapMutableTree instance, with the provided Node as tree root.
     *
     * @param root The root node.
     */
    public <X extends Node<V, K>> MapMutableTree(final X root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root Node argument.");

        // Assign internal state
        this.root = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<V, K>> X getRoot() {
        return (X) root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableNode<V, K> setRoot(final MutableNode<V, K> root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");
        Validate.isTrue(root.getParent() == null, "New root node must not have a parent Node.");
        MutableNode<V, K> oldRoot = null;

        // Do we need to move the current children?
        final int numOldRootChildren = this.root.getChildren().size();
        if (numOldRootChildren > 0) {

            // If so, the old root must be a MutableNode...
            if (!(this.root instanceof MutableNode)) {
                throw new IllegalStateException("Previous root node [" + this.root
                        + "] was no MutableNode. Cannot move ["
                        + numOldRootChildren + "] children to new root node.");
            }

            // ... and all children of the old root node must be MutableNodes.
            List<MutableNode<V, K>> oldRootChildren = new ArrayList<MutableNode<V, K>>();
            for (Node<V, K> current : root.getChildren()) {

                if (!(current instanceof MutableNode)) {
                    throw new IllegalStateException("Existing child [" + current
                            + "] was not a MutableNode. Cannot move to new parent (root) node.");
                }

                oldRootChildren.add((MutableNode<V, K>) current);
            }

            // Cast appropriately.
            MutableNode<V, K> mutableOldRoot = (MutableNode<V, K>) this.root;

            synchronized (lock) {

                // Move all existing children from the old root to the new one.
                for (MutableNode<V, K> current : oldRootChildren) {

                    current.setParent(root);
                    root.addChild(current);
                    mutableOldRoot.removeChild(current);
                }

                oldRoot = mutableOldRoot;
            }
        }

        // All Done.
        return oldRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final MutableNode<V, K> node, final Path<K> parentPath)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");
        Validate.notNull(parentPath, "Cannot handle null parentPath argument.");

        // Acquire the parent Node.
        final Node<V, K> parentNode = get(parentPath);
        if (parentNode == null) {
            throw new IllegalArgumentException("No node found at parentPath [" + parentPath + "]");
        }

        if (!(parentNode instanceof MutableNode)) {
            throw new IllegalArgumentException("Parent node [" + parentNode + "] was not a MutableNode. " +
                    "Child Nodes can only be added to MutableNode instances.");
        }

        // Cast, and add.
        final MutableNode<V, K> mutableParentNode = (MutableNode<V, K>) parentNode;

        synchronized (lock) {
            mutableParentNode.addChild(node);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<V, K>> X remove(final Path<K> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");

        final X toReturn = get(path);

        if (toReturn != null) {
            final Node<V, K> parent = toReturn.getParent();

            if (parent == null) {
                // Cannot remove the root node in this tree.
                throw new IllegalArgumentException("Cannot remove the root from a Tree.");

            } else if (!(parent instanceof MutableNode)) {
                throw new IllegalArgumentException("Cannot remove Node [" + toReturn + "], "
                        + "since its parent node [" + parent + "] is not a MutableNode.");
            }

            // We should be OK to remove the Node found at the supplied path.
            final MutableNode<V, K> mutableParent = (MutableNode<V, K>) parent;

            synchronized (lock) {
                mutableParent.removeChildren(new Filter<Node<V, K>>() {
                    @Override
                    public boolean accept(final Node<V, K> candidate) {
                        return candidate.getKey().compareTo(toReturn.getKey()) == 0;
                    }
                });
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<V, K>> X get(final Path<K> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");

        // The first path segment must conform to the key of the root node.
        final Iterator<K> iterator = path.iterator();
        if (!iterator.hasNext() || iterator.next().compareTo(root.getKey()) != 0) {
            return null;
        }

        Node<V, K> currentNode = root;

        outer:
        while (iterator.hasNext()) {

            // Descend one level on the path.
            final K nextPathSegment = iterator.next();

            for (Node<V, K> currentChild : currentNode.getChildren()) {
                if (currentChild.getKey().compareTo(nextPathSegment) == 0) {

                    // Found a matching Node.
                    currentNode = currentChild;
                    continue outer;
                }
            }

            // Found no match
            return null;
        }

        // All done.
        return (X) currentNode;
    }
}
