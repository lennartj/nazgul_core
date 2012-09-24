/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.predicate.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MutableTree implementation harbouring a root Node instance, capable of containing children.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MapMutableTree<T extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        implements MutableTree<T, KeyType>, Serializable {

    // Internal state
    private Node<T, KeyType> root;
    private final Object lock = new Object();

    /**
     * Creates a new MapMutableTree instance, with the provided Node as tree root.
     *
     * @param root The root node.
     */
    public MapMutableTree(final Node<T, KeyType> root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root Node argument.");

        // Assign internal state
        this.root = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node<T, KeyType> getRoot() {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node<T, KeyType> setRoot(final Node<T, KeyType> root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");
        Validate.isTrue(root.getParent() == null, "New root node must not have a parent Node.");
        Node<T, KeyType> oldRoot = null;

        // Do we need to move the current children?
        final int numOldRootChildren = this.root.getChildren().size();
        if (numOldRootChildren > 0) {

            // If so, the old root must be a MutableNode...
            if (!(this.root instanceof MutableNode)) {
                throw new IllegalStateException("Previous root node [" + this.root
                        + "] was no MutableNode. Cannot move ["
                        + numOldRootChildren + "] children to new root node.");
            }

            // ... and the new root must be a MutableNode.
            if (!(root instanceof MutableNode)) {
                throw new IllegalArgumentException("New root node [" + root
                        + "] was no MutableNode. Cannot move ["
                        + numOldRootChildren + "] children to new root node.");
            }

            // ... and all children of the old root node must be MutableNodes.
            List<MutableNode<T, KeyType>> oldRootChildren = new ArrayList<MutableNode<T, KeyType>>();
            for (Node<T, KeyType> current : root.getChildren()) {

                if (!(current instanceof MutableNode)) {
                    throw new IllegalStateException("Existing child [" + current
                            + "] was not a MutableNode. Cannot move to new parent (root) node.");
                }

                oldRootChildren.add((MutableNode<T, KeyType>) current);
            }

            // Cast appropriately.
            MutableNode<T, KeyType> mutableOldRoot = (MutableNode<T, KeyType>) this.root;
            MutableNode<T, KeyType> mutableNewRoot = (MutableNode<T, KeyType>) root;

            synchronized (lock) {

                // Move all existing children from the old root to the new one.
                for (MutableNode<T, KeyType> current : oldRootChildren) {

                    current.setParent(mutableNewRoot);
                    mutableNewRoot.addChild(current);
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
    public void add(final MutableNode<T, KeyType> node, final Path<KeyType> parentPath)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");
        Validate.notNull(parentPath, "Cannot handle null parentPath argument.");

        // Acquire the parent Node.
        final Node<T, KeyType> parentNode = get(parentPath);
        if (parentNode == null) {
            throw new IllegalArgumentException("No node found at parentPath [" + parentPath + "]");
        }

        if (!(parentNode instanceof MutableNode)) {
            throw new IllegalArgumentException("Parent node [" + parentNode + "] was not a MutableNode. " +
                    "Child Nodes can only be added to MutableNode instances.");
        }

        // Cast, and add.
        final MutableNode<T, KeyType> mutableParentNode = (MutableNode<T, KeyType>) parentNode;

        synchronized (lock) {
            mutableParentNode.addChild(node);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node<T, KeyType> remove(final Path<KeyType> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");

        final Node<T, KeyType> toReturn = get(path);

        if (toReturn != null) {
            final Node<T, KeyType> parent = toReturn.getParent();

            if (parent == null) {
                // Cannot remove the root node in this tree.
                throw new IllegalArgumentException("Cannot remove the root from a Tree.");

            } else if (!(parent instanceof MutableNode)) {
                throw new IllegalArgumentException("Cannot remove Node [" + toReturn + "], "
                        + "since its parent node [" + parent + "] is not a MutableNode.");
            }

            // We should be OK to remove the Node found at the supplied path.
            final MutableNode<T, KeyType> mutableParent = (MutableNode<T, KeyType>) parent;

            synchronized (lock) {
                mutableParent.removeChildren(new Filter<Node<T, KeyType>>() {
                    @Override
                    public boolean accept(final Node<T, KeyType> candidate) {
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
    public Node<T, KeyType> get(final Path<KeyType> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");

        // The first path segment must conform to the key of the root node.
        final Iterator<KeyType> iterator = path.iterator();
        if (!iterator.hasNext() || iterator.next().compareTo(root.getKey()) != 0) {
            return null;
        }

        Node<T, KeyType> currentNode = root;

        outer:
        while (iterator.hasNext()) {

            // Descend one level on the path.
            final KeyType nextPathSegment = iterator.next();

            for (Node<T, KeyType> currentChild : currentNode.getChildren()) {
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
        return currentNode;
    }
}
