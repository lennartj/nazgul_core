/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;

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
public class ListMutableNode<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable>
        implements MutableNode<KeyType, ValueType> {

    // Internal state
    private ValueType data;
    private KeyType key;
    private MutableNode<KeyType, ValueType> parent;
    private final List<Node<KeyType, ValueType>> children;

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
                           final MutableNode<KeyType, ValueType> parent,
                           final List<Node<KeyType, ValueType>> children) {

        // Check sanity
        Validate.notNull(key, "Cannot handle null key argument.");

        // Assign internal state
        this.data = data;
        this.key = key;
        this.parent = parent;

        List<Node<KeyType, ValueType>> tmp = children == null ? new ArrayList<Node<KeyType, ValueType>>() : children;
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
    public <X extends Node<KeyType, ValueType>> List<X> getChildren() {
        return (List<X>) children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(final MutableNode<KeyType, ValueType> node) throws IllegalArgumentException {

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
    public void removeChildren(final Filter<Node<KeyType, ValueType>> mutableNodeFilter) {

        // Check sanity
        Validate.notNull(mutableNodeFilter, "Cannot handle null mutableNodeFilter argument.");

        // Find all nodes which should be removed.
        List<MutableNode<KeyType, ValueType>> toRemove = new ArrayList<MutableNode<KeyType, ValueType>>();
        for (Object current : children) {

            // This should be either a Node or a MutableNode.
            @SuppressWarnings("unchecked")
            Node<KeyType, ValueType> currentNode = (Node<KeyType, ValueType>) current;
            if (mutableNodeFilter.accept(currentNode) && currentNode instanceof MutableNode) {
                toRemove.add((MutableNode<KeyType, ValueType>) currentNode);
            }
        }

        // Remove all appropriate nodes.
        if (toRemove.size() > 0) {
            synchronized (children) {
                for (MutableNode<KeyType, ValueType> current : toRemove)
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
    public void removeChild(final MutableNode<KeyType, ValueType> node) {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");

        Node<KeyType, ValueType> toRemove = null;
        for (Node<KeyType, ValueType> current : children) {
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

            MutableNode<KeyType, ValueType> mutableNodeToRemove = (MutableNode<KeyType, ValueType>) toRemove;

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
    public <X extends Node<KeyType, ValueType>> X getParent() {
        return (X) parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(final MutableNode<KeyType, ValueType> parent) {

        // Check sanity
        Validate.notNull(parent, "Cannot handle null parent argument.");
        MutableNode<KeyType, ValueType> oldParent = this.parent;

        // Add this MutableNode to the provided parent.
        synchronized (children) {

            if (oldParent != null) {
                // TODO: Send tree-changed event to old parent?
                oldParent.getChildren().remove(this);
            }

            // TODO: Send tree-changed event to new parent?
            // Assign our internal state.
            this.parent = parent;

            final List<Node<KeyType, ValueType>> newParentChildren = parent.getChildren();
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

        for (MutableNode<KeyType, ValueType> current = this; current != null; current = current.getParent()) {
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
    public int compareTo(final Node<KeyType, ValueType> that) {

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
