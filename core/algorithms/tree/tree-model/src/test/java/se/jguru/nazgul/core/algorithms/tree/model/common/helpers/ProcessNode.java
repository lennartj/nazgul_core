/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;


import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.common.ListMutableNode;

import java.util.Arrays;
import java.util.List;

/**
 * A node relating String names to String process values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProcessNode extends ListMutableNode<String, String> {

    /**
     * {@inheritDoc}
     */
    public ProcessNode(final String key,
                       final String data,
                       final MutableNode<String, String> parent) {

        // Delegate
        this(key, data, parent, null);
    }


    /**
     * {@inheritDoc}
     */
    public ProcessNode(final String key,
                       final String data,
                       final MutableNode<String, String> parent,
                       final List<Node<String, String>> children) {

        // Delegate
        super(key, data, parent, children);
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
    @Override
    protected <X extends Path<String>> X makePath(final String key) {
        return (X) ProcessStringPath.create(Arrays.asList(key));
    }
}
