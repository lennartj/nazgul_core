/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree.factory;

import se.jguru.nazgul.core.algorithms.api.tree.MutableNode;
import se.jguru.nazgul.core.algorithms.api.tree.MutableTree;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TreeFactory<E extends Enum<E>> {

    public <T extends Serializable, KeyType extends Serializable & Comparable<KeyType>>
    MutableTree<T, KeyType> makeTree(EnumMap<E, MutableNode<T, KeyType>> enumToNodeMap);
}
