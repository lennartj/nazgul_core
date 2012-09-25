/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MutableNode implementation harbouring a List of children.
 * Assumes Nodes are Serializable in terms of data and key types.
 *
 * @param <ValueType> The value type of this Node.
 * @param <KeyType>   The key type of this Node.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ListMutableNode<ValueType extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
        implements MutableNode<ValueType, KeyType> {

    // Internal state
    private ValueType data;
    private KeyType key;
    private MutableNode<ValueType, KeyType> parent;
    private final List<Node<ValueType, KeyType>> children;

    /**
     * Compound constructor, creating a new DefaultMutableNode instance from the
     * provided arguments.
     *
     * @param key      The key of this DefaultMutableNode instance.
     * @param data     The data of this DefaultMutableNode instance.
     * @param parent   The parent of this DefaultMutableNode instance.
     * @param children The children of this DefaultMutableNode instance. Can be {@code null}, in which
     *                 case a new ArrayList will be created and used for the children.
     */
    public ListMutableNode(final KeyType key,
                           final ValueType data,
                           final MutableNode<ValueType, KeyType> parent,
                           final List<Node<ValueType, KeyType>> children) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");

        // Assign internal state
        this.data = data;
        this.key = key;
        this.parent = parent;

        List<Node<ValueType, KeyType>> tmp = children == null ? new ArrayList<Node<ValueType, KeyType>>() : children;
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
    public <X extends Node<ValueType, KeyType>> List<X> getChildren() {
        return (List<X>) children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(final MutableNode<ValueType, KeyType> node) throws IllegalArgumentException {

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
    public void removeChildren(final Filter<Node<ValueType, KeyType>> mutableNodeFilter) {

        // Check sanity
        Validate.notNull(mutableNodeFilter, "Cannot handle null mutableNodeFilter argument.");

        // Find all nodes which should be removed.
        List<MutableNode<ValueType, KeyType>> toRemove = new ArrayList<MutableNode<ValueType, KeyType>>();
        for (Object current : children) {

            // This should be either a Node or a MutableNode.
            @SuppressWarnings("unchecked")
            Node<ValueType, KeyType> currentNode = (Node<ValueType, KeyType>) current;
            if (mutableNodeFilter.accept(currentNode) && currentNode instanceof MutableNode) {
                toRemove.add((MutableNode<ValueType, KeyType>) currentNode);
            }
        }

        // Remove all appropriate nodes.
        if (toRemove.size() > 0) {
            synchronized (children) {
                for (MutableNode<ValueType, KeyType> current : toRemove)
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
    public void removeChild(final MutableNode<ValueType, KeyType> node) {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");

        Node<ValueType, KeyType> toRemove = null;
        for (Node<ValueType, KeyType> current : children) {
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

            MutableNode<ValueType, KeyType> mutableNodeToRemove = (MutableNode<ValueType, KeyType>) toRemove;

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
    public <X extends Node<ValueType, KeyType>> X getParent() {
        return (X) parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(final MutableNode<ValueType, KeyType> parent) {

        // Check sanity
        Validate.notNull(parent, "Cannot handle null parent argument.");
        MutableNode<ValueType, KeyType> oldParent = this.parent;

        // Add this MutableNode to the provided parent.
        synchronized (children) {

            if (oldParent != null) {
                // TODO: Send tree-changed event to old parent?
                oldParent.getChildren().remove(this);
            }

            // TODO: Send tree-changed event to new parent?
            // Assign our internal state.
            this.parent = parent;

            final List<Node<ValueType, KeyType>> newParentChildren = parent.getChildren();
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
    public ValueType getData() {
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
    public <X extends Path<KeyType>> X getPath() {

        List<KeyType> pathSegments = new ArrayList<KeyType>();

        for (MutableNode<ValueType, KeyType> current = this; current != null; current = current.getParent()) {
            pathSegments.add(current.getKey());
        }

        Collections.reverse(pathSegments);

        // Create a Path from the reversed pathSegments List.
        Path<KeyType> currentPath = null;
        for (KeyType current : pathSegments) {
            currentPath = currentPath == null ? makePath(current) : currentPath.append(current);
        }

        // All done
        return (X) currentPath;
    }

    /**
     * Override this method to create (and use) a custom Path
     * implementation for this ListMutableNode instance.
     * The default implementation uses {@code ListPath}.
     *
     * @param key The key to convert to a Path.
     * @param <X> The Path type (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path made from the provided key.
     */
    protected <X extends Path<KeyType>> X makePath(final KeyType key) {
        return (X) new ListPath<KeyType>(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Node<ValueType, KeyType> that) {

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
