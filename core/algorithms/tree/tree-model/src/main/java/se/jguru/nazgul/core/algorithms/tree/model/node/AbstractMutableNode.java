package se.jguru.nazgul.core.algorithms.tree.model.node;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.trees.node.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractMutableNode<K extends Serializable & Comparable<K>, V extends Serializable>
        extends NazgulEntity implements MutableNode<K, V> {

    // Internal state
    private final Object lock = new Object();

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearParent() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(final MutableNode<K, V> parent) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChild(final MutableNode<K, V> node) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(final Filter<Node<K, V>> nodeFilter) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(final MutableNode<K, V> node) throws IllegalArgumentException {
    }

    /**
     * @param <X> The Node subtype.
     * @return An unmodifiable List holding the immediate child nodes of this Node.
     */
    @Override
    public <X extends Node<K, V>> List<X> getChildren() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Node<K, V>> X getParent() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Path<K>> X getPath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Node<K, V> that) {
        return 0;
    }
}
