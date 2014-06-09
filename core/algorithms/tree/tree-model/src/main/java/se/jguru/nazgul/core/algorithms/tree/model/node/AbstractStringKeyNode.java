/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.algorithms.tree.model.node;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.trees.node.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.algorithms.tree.model.path.StringPath;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract persistable MutableNode implementation, which inherits persistence mechanics from NazgulEntity.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"parent", "key"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractStringKeyNode<V extends Serializable>
        extends NazgulEntity implements MutableNode<String, V> {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(AbstractStringKeyNode.class);

    // Internal state
    private final transient Object lock = new Object();

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    private String key;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private MutableNode<String, V> parent;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public AbstractStringKeyNode() {
    }

    /**
     * Compound constructor, creating an instance wrapping the given data.
     *
     * @param key    The key of this AbstractStringKeyNode.
     * @param parent The parent of this AbstractStringKeyNode, which must be another AbstractStringKeyNode.
     * @param <T>    The concrete parent node subtype.
     */
    public <T extends AbstractStringKeyNode<V>> AbstractStringKeyNode(final String key,
                                                                      final T parent) {
        this.key = key;
        this.parent = parent;

        // Ensure that this node is added as a child to the supplied parent.
        if(parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearParent() {

        if (parent != null) {

            // Remove this node from the children list of its parent.
            synchronized (lock) {

                // TODO: Send tree-changed event to parent?
                parent.getChildren().remove(this);
                parent = null;
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Cannot clear the parent node from a Root node. (It has no parent).");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(final MutableNode<String, V> parent) {

        // Check sanity
        Validate.notNull(parent, "Cannot handle null parent argument.");
        final MutableNode<String, V> oldParent = this.parent;

        // Add this MutableNode to the provided parent.
        synchronized (lock) {

            if (oldParent != null) {
                // TODO: Send tree-changed event to old parent?
                oldParent.getChildren().remove(this);
            }

            // TODO: Send tree-changed event to new parent?
            // Assign our internal state.
            this.parent = parent;

            final List<Node<String, V>> newParentChildren = parent.getChildren();
            if (!newParentChildren.contains(this)) {
                newParentChildren.add(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChild(final MutableNode<String, V> node) {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");

        Node<String, V> toRemove = null;
        for (Node<String, V> current : getChildren()) {
            if (node.equals(current)) {
                toRemove = current;
                break;
            }
        }

        if (toRemove != null) {

            final MutableNode<String, V> mutableNodeToRemove = (MutableNode<String, V>) toRemove;
            synchronized (lock) {

                // TODO: Send tree changed event to parent?
                mutableNodeToRemove.clearParent();
                getChildren().remove(mutableNodeToRemove);
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Node [" + node + "] was not a child of this: " + toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(final Filter<Node<String, V>> nodeFilter) {

        // Check sanity
        Validate.notNull(nodeFilter, "Cannot handle null nodeFilter argument.");

        // Find all nodes which should be removed.
        List<MutableNode<String, V>> toRemove = new ArrayList<>();
        for (Object current : ((AbstractStringKeyNode) this).getChildren()) {

            @SuppressWarnings("unchecked")
            final MutableNode<String, V> currentNode = (MutableNode<String, V>) current;
            if (nodeFilter.accept(currentNode)) {
                toRemove.add(currentNode);
            }
        }

        // Remove all appropriate nodes.
        if (toRemove.size() > 0) {
            synchronized (lock) {
                for (MutableNode<String, V> current : toRemove) {
                    removeChild(current);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(final MutableNode<String, V> node) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(node, "Cannot handle null node argument.");

        // Add only if the given node is not already present.
        if (!getChildren().contains(node)) {

            synchronized (lock) {
                node.setParent(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<String, V>> X getParent() {
        return (X) parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Path<String>> X getPath() {

        final List<String> pathSegments = new ArrayList<>();

        for (MutableNode<String, V> current = this; current != null; current = current.getParent()) {
            pathSegments.add(current.getKey());
        }

        Collections.reverse(pathSegments);

        // Create a Path from the reversed pathSegments List.
        Path<String> currentPath = null;
        for (String current : pathSegments) {
            currentPath = currentPath == null ? new StringPath(current) : currentPath.append(current);
        }

        // All done
        return (X) currentPath;
    }

    /**
     * The compareTo implementation of the AbstractStringKeyNode simply
     * compares the keys of then two nodes.
     *
     * @param that The non-null node sub-type to compare with.
     */
    @Override
    public int compareTo(final Node<String, V> that) {

        // Check sanity as per the spec.
        Validate.notNull(that, "Cannot compare AbstractStringKeyNode to null.");
        if (this == that) {
            return 0;
        }

        // Delegate to Key comparison.
        final String thatKey = that.getKey() == null ? "" : that.getKey();
        return getKey().compareTo(thatKey);
    }

    /**
     * The hashCode implementation of AbstractStringKeyNode uses the key, data and children as
     * basis for the calculation of resulting hashCode value.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int dataHashCode = getData() == null ? 0 : getData().hashCode();
        final int childHashCode = getChildren() == null ? 0 : getChildren().hashCode();
        return key.hashCode() + dataHashCode + childHashCode;
    }

    /**
     * The equals implementation simply delegates to the hashCode to calculate the
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if(!(obj instanceof AbstractStringKeyNode)) {
            return false;
        } else if(obj == this) {
            return true;
        }

        // Delegate, and return.
        return hashCode() == obj.hashCode();
    }

    /**
     * @return A Debug string printout of this AbstractStringKeyNode.
     */
    @Override
    public String toString() {

        final String suffix = getChildren().size() == 0
                ? " [leaf node (0 children)]"
                : " [" + getChildren().size() + " children]";

        return "AbstractStringKeyNode [" + key + "]: " + getData() + suffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(key, "key")
                .endExpressionAndValidate();
    }
}
