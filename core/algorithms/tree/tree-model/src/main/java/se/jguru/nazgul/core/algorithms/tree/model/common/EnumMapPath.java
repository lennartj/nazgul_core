/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.SemanticPath;
import se.jguru.nazgul.core.algorithms.tree.model.common.converter.EnumMapTypeConverter;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;

/**
 * Semantic Path implementation using an Enum to define the semantic meaning of
 * each segment within the Path. Uses an EnumMap to relate semantics to mapPathSegments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"enumType", "mapPathSegments"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class EnumMapPath<E extends Enum<E>, SegmentType extends Serializable & Comparable<SegmentType>>
        extends ListPath<SegmentType> implements SemanticPath<E, SegmentType> {

    // Internal state
    @XmlTransient
    private E[] enumConstants;

    @XmlAttribute(required = true)
    private Class<E> enumType;

    @XmlJavaTypeAdapter(EnumMapTypeConverter.class)
    private EnumMap<E, SegmentType> mapPathSegments;

    /**
     * JAXB/JPA-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public EnumMapPath() {
    }

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The mapPathSegments within this path.
     * @param enumType The type of enumeration defining the semantics of the path mapPathSegments.
     */
    public EnumMapPath(final EnumMap<E, SegmentType> segments, final Class<E> enumType) {

        // Check sanity
        Validate.notNull(enumType, "Cannot handle null enumType argument.");
        Validate.notEmpty(segments, "Cannot handle null or empty mapPathSegments argument.");

        final E[] enumConstants = enumType.getEnumConstants();
        Validate.isTrue(enumConstants.length > 0, "Cannot create an EnumMapPath from an Enum with no constants.");

        // Assign internal state
        this.enumType = enumType;
        this.mapPathSegments = TreeAlgorithms.getEmptyEnumMap(enumType);
        this.enumConstants = enumType.getEnumConstants();

        for (E current : segments.keySet()) {
            this.mapPathSegments.put(current, segments.get(current));
        }

        // Check sanity
        boolean noNullsFound = true;
        for (E current : this.mapPathSegments.keySet()) {
            if (this.mapPathSegments.get(current) == null) {
                noNullsFound = false;
            } else {
                Validate.isTrue(noNullsFound, "Paths cannot contain null elements. Got: " + this.mapPathSegments);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxSize() {
        return mapPathSegments.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(final E semanticDefinition) {
        return mapPathSegments.get(semanticDefinition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {

        int toReturn = 0;

        for (E current : mapPathSegments.keySet()) {
            if (mapPathSegments.get(current) == null) {
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
    public <X extends Path<SegmentType>> X append(final SegmentType aSegment) {
        if (size() >= getMaxSize()) {
            throw new IndexOutOfBoundsException("Cannot append key [" + aSegment + "] to path [" + this
                    + "]. Maximum depth [" + getMaxSize() + "] reached.");
        }

        // Perform a deep clone on the mapPathSegments map.
        final EnumMap<E, SegmentType> clone = TreeAlgorithms.getEmptyEnumMap(enumType);
        for (E current : mapPathSegments.keySet()) {

            final SegmentType segmentType = mapPathSegments.get(current);
            final SegmentType toPut = segmentType == null ? null : (SegmentType) deepClone(segmentType);
            clone.put(current, toPut);
        }

        // Add the new element and create a new EnumMapPath instance to return.
        clone.put(enumConstants[size()], aSegment);
        return (X) new EnumMapPath<E, SegmentType>(clone, enumType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Path<SegmentType> that) {

        final Iterator<SegmentType> thisIt = this.iterator();
        final Iterator<SegmentType> thatIt = that.iterator();

        while (true) {

            // Unequal number of mapPathSegments?
            if (!thisIt.hasNext()) {
                return -1;
            } else if (!thatIt.hasNext()) {
                return 1;
            }

            // Both iterators seem to have a next element
            SegmentType thisSegment = thisIt.next();
            SegmentType thatSegment = thatIt.next();

            // Compare this next segment, and return a non-0 result.
            int result = thisSegment.compareTo(thatSegment);
            if (result != 0) {
                return result;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SegmentType> iterator() {
        return new SemanticPathSegmentIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(final int index) throws IndexOutOfBoundsException {

        // Check sanity
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index [" + index
                    + "] must be smaller than path size [" + size() + "].");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Cannot handle negative index argument.");
        }
        if(mapPathSegments == null) {

            // Re-build the mapPathSegments?
            throw new IllegalStateException("mapPathSegments: "  + mapPathSegments);
        }

        // All done.
        final E key = enumConstants[index];
        return key == null ? null : mapPathSegments.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String separator = "\n";
        StringBuilder builder = new StringBuilder("EnumMapPath { \n");
        for (E current : mapPathSegments.keySet()) {
            if (mapPathSegments.get(current) != null) {
                builder.append("  ").append(current).append("=").append(mapPathSegments.get(current)).append(separator);
            }
        }
        return builder.delete(builder.length() - separator.length(), builder.length()).append(" }").toString();
    }

    //
    // Private helpers
    //

    /**
     * Performs a deep clone of the contained mapPathSegments EnumMap.
     *
     * @return a deep clone of the mapPathSegments EnumMap.
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
     * Iterator usable for EnumMapPath instances.
     * Not a thread-safe implementation.
     */
    class SemanticPathSegmentIterator implements Iterator<SegmentType> {

        // Internal state
        private final E[] constants = EnumMapPath.this.enumConstants;
        private E cursor;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {

            // Don't run out of constants
            boolean withinBounds = cursor != constants[constants.length - 1];

            // ... and paths should be continuous.
            final int currentOrdinal = cursor == null ? -1 : cursor.ordinal();
            return withinBounds && mapPathSegments.get(constants[currentOrdinal + 1]) != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SegmentType next() {

            // Step the cursor
            final int currentOrdinal = cursor == null ? -1 : cursor.ordinal();
            cursor = constants[currentOrdinal + 1];

            // All done.
            return mapPathSegments.get(cursor);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove data from mapPathSegments.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
        // Do nothing
    }
}
