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
