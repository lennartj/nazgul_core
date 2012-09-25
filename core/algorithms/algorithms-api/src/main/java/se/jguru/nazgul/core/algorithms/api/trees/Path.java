/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees;

import java.io.Serializable;

/**
 * Generic immutable Path, defining a single continuous list of segments.
 * This is applicable - for example - as a Path within a Tree.
 *
 * @param <SegmentType> The type of segment from which this Path is constructed.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Path<SegmentType extends Serializable & Comparable<SegmentType>>
        extends Iterable<SegmentType>, Comparable<Path<SegmentType>>, Serializable {

    /**
     * @return The number of segments in this Path.
     */
    int size();

    /**
     * Retrieves the path segment with the provided index.
     *
     * @param index the index (which must be smaller than {@code size}).
     * @return The KeyType instance found on the provided position.
     * @throws IndexOutOfBoundsException if {@code index} was smaller than 0 or
     *                                   greater than {@code size}.
     */
    SegmentType get(int index) throws IndexOutOfBoundsException;

    /**
     * Appends the provided KeyType to this Path, returning the resulting Path.
     *
     * @param aKey The KeyType to append to this Path.
     * @return The Path result from appending the provided KeyType instance to this Path.
     */
    <X extends Path<SegmentType>> X append(SegmentType aKey);
}
