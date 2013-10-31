/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.trees;

import se.jguru.nazgul.core.algorithms.api.trees.path.Path;

import java.io.Serializable;

/**
 * A generic typed Tree structure holding nodes.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Tree<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable> {

    /**
     * @return The root node of this tree.
     */
    MutableNode<KeyType, ValueType> getRoot();

    /**
     * Retrieves the Node at the provided path.
     *
     * @param <X>  The Node (or subtype thereof, such as {@code MutableNode}) returned.
     * @param path The path to the node which should be acquired.
     * @return The Node at the provided path, or {@code null} if no node was found.
     */
    <X extends Node<KeyType, ValueType>> X get(Path<KeyType> path);
}
