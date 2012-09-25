/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import java.io.Serializable;

/**
 * An extension to Path where all segments have a semantic meaning, defined
 * by the values of an Enum. This implies that all levels of the path correspond
 * to some concept, and that the SemanticPath in essence works as a Map relating
 * Enum values to KeyType values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SemanticPath<E extends Enum<E>, KeyType extends Serializable & Comparable<KeyType>>
        extends Path<KeyType> {

    /**
     * @return The maximum number of segments in this Path.
     */
    int getMaxSize();

    /**
     * Retrieves the KeyType segment corresponding to the Enum value providing
     * the semantic definition of this SemanticPath.
     *
     * @param semanticDefinition The semantic definition within this SemanticPath whose value should be retrieved.
     * @return The segment corresponding to the provided semantic definition, or {@code null} if no segment KeyType is
     *         defined for the provided semanticDefinition level within this SemanticPath.
     */
    KeyType get(E semanticDefinition);
}
