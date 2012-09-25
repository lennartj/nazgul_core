/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.ListMutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.MutableNode;
import se.jguru.nazgul.core.algorithms.api.trees.Node;

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
                            List<Node<String, String>> children) {
        super(key, data, parent, children);
    }
}
