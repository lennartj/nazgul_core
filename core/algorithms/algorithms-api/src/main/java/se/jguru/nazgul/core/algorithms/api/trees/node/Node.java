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

import se.jguru.nazgul.core.algorithms.api.trees.path.Path;

import java.io.Serializable;
import java.util.List;

/**
 * Generic and typesafe Node definition, for Tree structures. A Node is similar to a Map.Entry,
 * in that it relates a Key (with a defined KeyType) to a Value (with a defined ValueType).
 * <p/>
 * To simplify Nodes and their corresponding features being read from and written to streams,
 * both the Node type itself and its KeyType/ValueType are {@code Serializable}. Moreover,
 * since some Tree algorithms require ordering of keys (and, thereby, nodes), the KeyType must also implement
 * {@code Comparable<KeyType>}.
 *
 * @param <ValueType> The type for the values of this Node.
 * @param <KeyType>   The type for the keys of this Node.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Node<KeyType extends Comparable<KeyType>, ValueType>
        extends Comparable<Node<KeyType, ValueType>>, Serializable {

    /**
     * @param <X> The Node subtype.
     * @return An unmodifiable List holding the immediate child nodes of this Node.
     */
    <X extends Node<KeyType, ValueType>> List<X> getChildren();

    /**
     * Retrieves the parent of this Node.
     *
     * @param <X> The Node subtype.
     * @return The parent of this Node.
     */
    <X extends Node<KeyType, ValueType>> X getParent();

    /**
     * @return The data of this Node.
     */
    ValueType getData();

    /**
     * @return The key of this Node.
     */
    KeyType getKey();

    /**
     * @param <X> The Path (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path (from the Tree root) of this Node.
     */
    <X extends Path<KeyType>> X getPath();
}
