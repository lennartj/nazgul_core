/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.algorithms.tree.model;

import java.io.Serializable;

/**
 * Specification for a Tree whose internal state can be manipulated, in terms
 * of adding/removing Nodes and re-assigning the root Node.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MutableTree<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable>
        extends Tree<KeyType, ValueType> {

    /**
     * Reassigns the root node of this MutableTree, and moves all existing (immediate)
     * children of the old root node to the new one. Following the completion of the
     * move, the old root node is returned.
     *
     * @param root The new root node.
     * @return The previous root node.
     * @throws IllegalArgumentException if the existing root Node has children and either
     *                                  the old root Node, the supplied (new) root Node or
     *                                  any existing children are not MutableNode instances.
     *                                  Also thrown if {@code root} has a parent node assigned.
     */
    MutableNode<KeyType, ValueType> setRoot(MutableNode<KeyType, ValueType> root) throws IllegalArgumentException;
}
