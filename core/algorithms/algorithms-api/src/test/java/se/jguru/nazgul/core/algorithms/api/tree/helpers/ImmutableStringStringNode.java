/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree.helpers;

import se.jguru.nazgul.core.algorithms.api.tree.Node;
import se.jguru.nazgul.core.algorithms.api.tree.Path;
import se.jguru.nazgul.core.algorithms.api.tree.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.tree.factory.PathFactory;

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
    private PathFactory<String> factory;

    public ImmutableStringStringNode(final String key,
                                     final String data,
                                     final Node<String, String> parent,
                                     final List<Node<String, String>> children,
                                     final PathFactory<String> factory) {
        this.children = children;
        this.parent = parent;
        this.key = key;
        this.data = data;
        this.factory = factory;
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
    public Path<String> getPath() {
        List<String> pathSegments = new ArrayList<String>();

        for (Node<String, String> current = this; current.getParent() != null; current = current.getParent()) {
            pathSegments.add(current.getKey());
        }

        Collections.reverse(pathSegments);

        // All done.
        return TreeAlgorithms.getPath(pathSegments, factory);
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
