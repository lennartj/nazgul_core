/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;

/**
 * Semantic Path implementation using an Enum to define the semantic meaning of
 * each segment within the Path. Uses an EnumMap to relate semantics to segments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPath<E extends Enum<E>, KeyType extends Serializable & Comparable<KeyType>>
        implements SemanticPath<E, KeyType> {

    // Internal state
    private Class<E> enumType;
    private E[] enumConstants;
    private EnumMap<E, KeyType> segments;

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The segments within this path.
     * @param enumType The type of enumeration defining the semantics of the path segments.
     */
    public EnumMapPath(final EnumMap<E, KeyType> segments, final Class<E> enumType) {

        // Check sanity
        Validate.notNull(enumType, "Cannot handle null enumType argument.");
        Validate.notNull(segments, "Cannot handle null segments argument.");

        final E[] enumConstants = enumType.getEnumConstants();
        Validate.isTrue(enumConstants.length > 0, "Cannot create an EnumMapPath from an Enum with no constants.");

        // Create a full internal enum map.
        this.enumType = enumType;
        this.segments = new EnumMap<E, KeyType>(enumType);
        for (E current : enumConstants) {
            this.segments.put(current, null);
        }

        // Assign our internal state
        this.enumConstants = enumType.getEnumConstants();
        for (E current : segments.keySet()) {
            this.segments.put(current, segments.get(current));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxSize() {
        return segments.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyType get(final E semanticDefinition) {
        return segments.get(semanticDefinition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {

        int toReturn = 0;

        for (E current : segments.keySet()) {
            if (segments.get(current) == null) {
                return toReturn;
            }

            toReturn++;
        }

        return toReturn;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if a new EnumMapPath could not be created
     *                               using this EnumMapPath instance by concatenating
     *                               the provided KeyType instance.
     */
    @Override
    public <X extends Path<KeyType>> X append(final KeyType aKey) {
        if (size() >= getMaxSize()) {
            throw new IndexOutOfBoundsException("Cannot append key [" + aKey + "] to path [" + this
                    + "]. Maximum depth [" + getMaxSize() + "] reached.");
        }

        // Perform a deep clone on the segments map.
        final EnumMap<E, KeyType> clone = new EnumMap<E, KeyType>(enumType);
        for(E current : segments.keySet()) {

            final KeyType keyType = segments.get(current);
            final KeyType toPut = keyType == null ? null : (KeyType) deepClone(keyType);
            clone.put(current, toPut);
        }

        // Add the new element and create a new EnumMapPath instance to return.
        clone.put(enumConstants[size()], aKey);
        return (X) new EnumMapPath<E, KeyType>(clone, enumType);
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
        return segments.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyType get(final int index) throws IndexOutOfBoundsException {

        // Check sanity
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index [" + index
                    + "] must be smaller than path size [" + size() + "].");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Cannot handle negative index argument.");
        }

        // All done.
        return segments.get(enumConstants[index]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String separator = "\n";
        StringBuilder builder = new StringBuilder("EnumMapPath { \n");
        for (E current : segments.keySet()) {
            if (segments.get(current) != null) {
                builder.append("  ").append(current).append("=").append(segments.get(current)).append(separator);
            }
        }
        return builder.delete(builder.length() - separator.length(), builder.length()).append(" }").toString();
    }

    //
    // Private helpers
    //

    /**
     * Performs a deep clone of the contained segments EnumMap.
     *
     * @return a deep clone of the segments EnumMap.
     */
    private Object deepClone(final Object toClone) {

        ByteArrayOutputStream bits = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(bits).writeObject(toClone);
            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bits.toByteArray()));
            return in.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deepClone object of type ["
                    + toClone.getClass().getName() + "]", e);
        }
    }

    /**
     *
     */
    class SemanticPathSegmentIterator implements Iterator<KeyType> {

        // Internal state

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KeyType next() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove data from segments.");
        }
    }
}
