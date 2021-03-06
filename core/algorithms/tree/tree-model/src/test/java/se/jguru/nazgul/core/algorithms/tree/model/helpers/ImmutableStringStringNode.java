/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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


package se.jguru.nazgul.core.algorithms.tree.model.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.node.Node;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.algorithms.tree.model.path.StringPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dummy implementation of an immutable Node.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ImmutableStringStringNode implements Node<String, String> {

    // Internal state
    private List<Node<String, String>> children;
    private Node<String, String> parent;
    private String key;
    private String data;

    public ImmutableStringStringNode(final String key,
                                     final String data,
                                     final Node<String, String> parent,
                                     final List<Node<String, String>> children) {
        this.children = children;
        this.parent = parent;
        this.key = key;
        this.data = data;
    }

    /**
     * @return An unmodifiable List holding the immediate child nodes of this Node.
     */
    @Override
    public <X extends Node<String, String>> List<X> getChildren() {
        return (List<X>) children;
    }

    /**
     * @return The parent of this Node.
     */
    @Override
    public Node<String, String> getParent() {
        return parent;
    }

    /**
     * @return The data of this Node.
     */
    @Override
    public String getData() {
        return data;
    }

    /**
     * @return The key of this Node.
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * @return The Path (from the Tree root) of this Node.
     */
    @Override
    public <X extends Path<String>> X getPath() {
        List<String> pathSegments = new ArrayList<String>();

        for (Node<String, String> current = this; current.getParent() != null; current = current.getParent()) {
            pathSegments.add(current.getKey());
        }

        Collections.reverse(pathSegments);

        // Create a Path from the reversed pathSegments List.
        Path<String> currentPath = null;
        for (String current : pathSegments) {
            currentPath = currentPath == null ? makePath(current) : currentPath.append(current);
        }

        // All done
        return (X) currentPath;
    }

    /**
     * Override this method to create (and use) a custom Path
     * implementation for this AbstractMutableNode instance.
     * The default implementation uses {@code AbstractListPath}.
     *
     * @param key The key to convert to a Path.
     * @param <X> The Path type (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path made from the provided key.
     */
    protected <X extends Path<String>> X makePath(final String key) {
        return (X) new StringPath(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Node<String, String> that) {

        // Delegate to Key comparison.
        return key.compareTo(that.getKey());
    }
}
