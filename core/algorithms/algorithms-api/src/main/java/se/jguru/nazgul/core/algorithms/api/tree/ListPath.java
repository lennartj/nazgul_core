/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Non-semantic Path implementation using List as internal storage of KeyType segments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ListPath<KeyType extends Serializable & Comparable<KeyType>> implements Path<KeyType> {

    // Internal state
    private List<KeyType> segments;

    /**
     * Creates a ListPath holding a single segment (i.e. the provided key).
     *
     * @param segment The only segment within this ListPath instance.
     */
    public ListPath(final KeyType segment) {

        // Check sanity
        Validate.notNull(segment, "Cannot handle null segment.");

        // Assign internal state
        segments = new ArrayList<KeyType>();
        segments.add(segment);
    }

    /**
     * Creates a ListPath object with the given segment List.
     *
     * @param segments The segments of the ListPath.
     */
    public ListPath(final List<KeyType> segments) {

        // Check sanity
        Validate.notNull(segments, "Cannot handle null segments argument.");

        // Assign internal state
        this.segments = segments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return segments.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X extends Path<KeyType>> X append(final KeyType aKey) {

        // Create a segment copy List.
        List<KeyType> segments = new ArrayList<KeyType>(this.segments);

        // Add the provided aKey instance and return.
        segments.add(aKey);
        return (X) new ListPath<KeyType>(segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Path<KeyType> that) {

        final Iterator<KeyType> thisIt = this.iterator();
        final Iterator<KeyType> thatIt = that.iterator();

        while (true) {

            // Unequal number of segments?
            if (!thisIt.hasNext()) {
                return -1;
            } else if (!thatIt.hasNext()) {
                return 1;
            }

            // Both iterators seem to have a next element
            KeyType thisKey = thisIt.next();
            KeyType thatKey = thatIt.next();

            // Compare this next segment, and return a non-0 result.
            int result = thisKey.compareTo(thatKey);
            if (result != 0) {
                return result;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<KeyType> iterator() {
        return Collections.unmodifiableList(segments).listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyType get(int index) throws IndexOutOfBoundsException {
        return segments.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("{ ");
        for (KeyType current : segments) {
            builder.append(current).append("/");

        }
        return builder.delete(builder.length() - "/".length(), builder.length()).append(" }").toString();
    }
}
