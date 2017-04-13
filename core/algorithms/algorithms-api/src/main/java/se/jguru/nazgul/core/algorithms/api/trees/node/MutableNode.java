/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.api.trees.node;

import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import javax.validation.constraints.NotNull;

/**
 * Specification for a Node whose internal state can be manipulated, in terms
 * of moving it within trees, and adding/removing children.
 *
 * @param <V> The value type of this MutableNode.
 * @param <K> The key type of this MutableNode.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MutableNode<K extends Comparable<K>, V> extends Node<K, V> {

    /**
     * Adds the provided node as a child to this MutableNode.
     *
     * @param node The node to add.
     * @throws IllegalArgumentException if the given node could not be added properly.
     */
    void addChild(@NotNull MutableNode<K, V> node) throws IllegalArgumentException;

    /**
     * Removes all immediate children of this MutableNode matching the provided Filter.
     *
     * @param nodeFilter a Filter defining which children should be removed.
     */
    void removeChildren(@NotNull Filter<Node<K, V>> nodeFilter);

    /**
     * Removes the provided child node from this MutableNode, if it exists.
     * If the provided node is not a child node of this MutableNode, this method
     * should return silently.
     *
     * @param node The node to remove.
     */
    void removeChild(@NotNull MutableNode<K, V> node);

    /**
     * Assigns the parent of this MutableNode.
     *
     * @param parent The parent of this node.
     */
    void setParent(@NotNull MutableNode<K, V> parent);

    /**
     * Removes this MutableNode from its parent, by removing both the
     * reference to the parent from this MutableNode and the reference
     * from the parent MutableNode to this MutableNode (in its list of children).
     */
    void clearParent();
}
