/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.tree.factory.PathFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MutableNode implementation harbouring a List of children.
 * Assumes Nodes are Serializable in terms of data and key types.
 *
 * @param <T>       The data type of this Node.
 * @param <KeyType> The KeyType of this Node.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ListMutableNode<T extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        implements MutableNode<T, KeyType> {

    // Internal state
    private T data;
    private KeyType key;
    private MutableNode<T, KeyType> parent;
    private final List<Node<T, KeyType>> children;
    private PathFactory<KeyType> factory;

    /**
     * Compound constructor, creating a new DefaultMutableNode instance from the
     * provided arguments.
     *
     * @param key      The key of this DefaultMutableNode instance.
     * @param data     The data of this DefaultMutableNode instance.
     * @param parent   The parent of this DefaultMutableNode instance.
     * @param children The children of this DefaultMutableNode instance. Can be {@code null}, in which
     *                 case a new ArrayList will be created and used for the children.
     * @param factory  The factory which should be used to create Paths from KeyType instances.
     */
    public ListMutableNode(final KeyType key,
                           final T data,
                           final MutableNode<T, KeyType> parent,
                           final List<Node<T, KeyType>> children,
                           final PathFactory<KeyType> factory) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");
        Validate.notNull(factory, "Cannot handle null factory argument.");

        // Assign internal state
        this.data = data;
        this.key = key;
        this.parent = parent;
        this.factory = factory;

        List<Node<T, KeyType>> tmp = children == null ? new ArrayList<Node<T, KeyType>>() : children;
        this.children = Collections.synchronizedList(tmp);
    }

    /**
     * {@inheritDoc}
     *
     * @return a synchronized List holding all children of this ListMutableNode.
     * @see Collections#synchronizedList(java.util.List)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <X extends Node<T, KeyType>> List<X> getChildren() {
        return (List<X>) children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(final MutableNode<T, KeyType> node) throws IllegalArgumentException {

        Validate.notNull(node, "Cannot handle null node argument.");

        // Add only if the given node is not already present.
        if (!children.contains(node)) {

            synchronized (children) {
                node.setParent(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(final Filter<Node<T, KeyType>> mutableNodeFilter) {

        // Check sanity
        Validate.notNull(mutableNodeFilter, "Cannot handle null mutableNodeFilter argument.");

        // Find all nodes which should be removed.
        List<MutableNode<T, KeyType>> toRemove = new ArrayList<MutableNode<T, KeyType>>();
        for (Object current : children) {

            // This should be either a Node or a MutableNode.
            @SuppressWarnings("unchecked")
            Node<T, KeyType> currentNode = (Node<T, KeyType>) current;
            if (mutableNodeFilter.accept(currentNode) && currentNode instanceof MutableNode) {
                toRemove.add((MutableNode<T, KeyType>) currentNode);
            }
        }

        // Remove all appropriate nodes.
        if (toRemove.size() > 0) {
            synchronized (children) {
                for (MutableNode<T, KeyType> current : toRemove)
                    removeChild(current);
            }
        }
    }

    /**
     * Removes the provided child node from this MutableNode, if it exists.
     * If the provided node is not a child node of this MutableNode, this method
     * should return silently.
     *
     * @param node The node to remove.
     */
    @Override
    public void removeChild(final MutableNode<T, KeyType> node) {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");

        Node<T, KeyType> toRemove = null;
        for (Node<T, KeyType> current : children) {
            if (node.equals(current)) {
                toRemove = current;
                break;
            }
        }

        // Should we remove anything?
        if (toRemove != null) {

            // All parent nodes are mutable nowadays?
            /*
            // We cannot re-assign the parent of toRemove unless it is
            // a MutableNode - so throw an exception to indicate this.
            if (!(toRemove instanceof MutableNode)) {
                throw new IllegalArgumentException("Cannot remove parent node from non-MutableNode implementation.");
            }
            */

            MutableNode<T, KeyType> mutableNodeToRemove = (MutableNode<T, KeyType>) toRemove;

            synchronized (children) {

                // TODO: Send tree changed event to parent?
                mutableNodeToRemove.clearParent();
                getChildren().remove(mutableNodeToRemove);
            }
        }
    }

    /**
     * @return The parent of this Node.
     */
    @Override
    public <X extends Node<T, KeyType>> X getParent() {
        return (X) parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(final MutableNode<T, KeyType> parent) {

        // Check sanity
        Validate.notNull(parent, "Cannot handle null parent argument.");
        MutableNode<T, KeyType> oldParent = this.parent;

        // Add this MutableNode to the provided parent.
        synchronized (children) {

            if (oldParent != null) {
                // TODO: Send tree-changed event to old parent?
                oldParent.getChildren().remove(this);
            }

            // TODO: Send tree-changed event to new parent?
            // Assign our internal state.
            this.parent = parent;

            final List<Node<T, KeyType>> newParentChildren = parent.getChildren();
            if (!newParentChildren.contains(this)) {
                newParentChildren.add(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearParent() {

        if (parent != null) {

            // Remove this node from the children list of its parent.
            synchronized (children) {

                // TODO: Send tree-changed event to parent?
                parent.getChildren().remove(this);
                parent = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getData() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyType getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path<KeyType> getPath() {

        List<KeyType> pathSegments = new ArrayList<KeyType>();

        for (MutableNode<T, KeyType> current = this; current != null; current = current.getParent()) {
            pathSegments.add(current.getKey());
        }

        Collections.reverse(pathSegments);

        // All done.
        return TreeAlgorithms.getPath(pathSegments, factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Node<T, KeyType> that) {

        // Delegate to Key comparison.
        return key.compareTo(that.getKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ListMutableNode {" + getKey() + " -> " + getData() + "}, " + children.size() + " children.";
    }
}
