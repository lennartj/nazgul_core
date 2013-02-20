/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;

import se.jguru.nazgul.core.algorithms.tree.model.MutableNode;
import se.jguru.nazgul.core.algorithms.tree.model.Node;
import se.jguru.nazgul.core.algorithms.tree.model.common.ListMutableNode;

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
