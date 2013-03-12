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

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.MutableTree;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MutableTree implementation which only holds a root node.
 * All mutability operations except the get method are delegated to the Node.
 *
 * @param <KeyType>   The type of key used within all MutableNode instances that constructs this SimpleTree.
 * @param <ValueType> The type of value used within all MutableNode instances that constitutes this SimpleTree.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SimpleTree<KeyType extends Serializable & Comparable<KeyType>, ValueType extends Serializable>
        implements MutableTree<KeyType, ValueType>, Serializable {

    // Internal state
    private final Object lock = new Object();
    private MutableNode<KeyType, ValueType> root;

    /**
     * Creates a SimpleTree instance with the provided root node.
     *
     * @param root The root node within the tree.
     */
    public SimpleTree(final MutableNode<KeyType, ValueType> root) {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");

        // Assign internal state
        this.root = root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableNode<KeyType, ValueType> setRoot(final MutableNode<KeyType, ValueType> root)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(root, "Cannot handle null root argument.");

        // Assign internal state
        MutableNode<KeyType, ValueType> oldRoot = this.root;

        // Ensure all children can be moved.
        List<MutableNode<KeyType, ValueType>> mutableChildren = new ArrayList<MutableNode<KeyType, ValueType>>();
        for (Node<KeyType, ValueType> current : oldRoot.getChildren()) {
            if (current instanceof MutableNode) {
                mutableChildren.add((MutableNode<KeyType, ValueType>) current);
            } else {
                throw new IllegalStateException("Existing child [" + current + "] was not a MutableNode, "
                        + "so cannot be moved from old root node [" + oldRoot + "]. Aborting setRoot.");
            }
        }

        // Move the children from oldRoot to newRoot
        synchronized (lock) {
            for (MutableNode<KeyType, ValueType> current : mutableChildren) {
                root.addChild(current);
            }

            // Actually switch the root node as well.
            this.root = root;
        }

        // All done.
        return oldRoot;
    }

    /**
     * @return The root node of this tree.
     */
    @Override
    public MutableNode<KeyType, ValueType> getRoot() {
        return root;
    }

    /**
     * Retrieves the Node at the provided path.
     *
     * @param <X>  The Node (or subtype thereof, such as {@code MutableNode}) returned.
     * @param path The path to the node which should be acquired.
     * @return The Node at the provided path, or {@code null} if no node was found.
     */
    @Override
    public <X extends Node<KeyType, ValueType>> X get(final Path<KeyType> path) {

        // Check sanity
        Validate.notNull(path, "Cannot handle null path argument.");
        Validate.isTrue(path.size() > 0, "Cannot handle empty path argument.");
        if (!path.get(0).equals(getRoot().getKey())) {
            return null;
        }

        Node<KeyType, ValueType> currentNode = getRoot();
        for (int depth = 1; depth < path.size(); depth++) {

            final Node<KeyType, ValueType> child = findChild(currentNode, path.get(depth));

            if (child == null) {
                currentNode = null;
                break;
            } else {
                currentNode = child;
            }
        }

        // All done.
        return (X) currentNode;
    }

    //
    // Private helpers
    //

    private Node<KeyType, ValueType> findChild(final Node<KeyType, ValueType> node, final KeyType key) {

        for (Node<KeyType, ValueType> current : node.getChildren()) {
            if (current.getKey().equals(key)) {
                return current;
            }
        }

        // No immediate child found.
        return null;
    }
}
