/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree.helpers;

import se.jguru.nazgul.core.algorithms.api.tree.ListMutableNode;
import se.jguru.nazgul.core.algorithms.api.tree.MutableNode;
import se.jguru.nazgul.core.algorithms.api.tree.Node;
import se.jguru.nazgul.core.algorithms.api.tree.factory.PathFactory;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringStringNode extends ListMutableNode<String, String> {

    /**
     * {@inheritDoc}
     */
    public StringStringNode(String key,
                            String data,
                            MutableNode<String, String> parent,
                            List<Node<String, String>> children,
                            PathFactory<String> factory) {
        super(key, data, parent, children, factory);
    }
}
