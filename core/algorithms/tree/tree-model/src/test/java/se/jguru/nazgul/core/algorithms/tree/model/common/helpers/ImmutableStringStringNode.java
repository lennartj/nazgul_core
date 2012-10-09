/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;

import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.common.ListPath;

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
     * implementation for this ListMutableNode instance.
     * The default implementation uses {@code ListPath}.
     *
     * @param key The key to convert to a Path.
     * @param <X> The Path type (or subtype thereof, such as {@code SemanticPath}) returned.
     * @return The Path made from the provided key.
     */
    protected <X extends Path<String>> X makePath(final String key) {
        return (X) new ListPath<String>(key);
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
