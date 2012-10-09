/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Non-semantic Path implementation using List as internal storage of KeyType segments.
 * <p/>
 * {@inheritDoc}
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"internalSegments"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class ListPath<SegmentType extends Serializable & Comparable<SegmentType>>
        extends NazgulEntity implements Path<SegmentType> {

    // Internal state
    @XmlTransient
    private List<SegmentType> segments;

    @XmlElementWrapper(name = "pathSegments")
    @XmlElement(name = "pathSegment")
    private List internalSegments;

    /**
     * JPA/JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public ListPath() {
    }

    /**
     * Creates a ListPath holding a single segment (i.e. the provided key).
     *
     * @param segment The only segment within this ListPath instance.
     */
    public ListPath(final SegmentType segment) {

        // Check sanity
        Validate.notNull(segment, "Cannot handle null segment.");

        // Assign internal state
        segments = new ArrayList<SegmentType>();
        segments.add(segment);

        // Handle JAXB requirements for marshalling
        internalSegments = segments;
    }

    /**
     * Creates a ListPath object with the given segment List.
     *
     * @param segments The segments of the ListPath.
     */
    public ListPath(final List<SegmentType> segments) {

        // Check sanity
        Validate.notNull(segments, "Cannot handle null segments argument.");

        // Assign internal state
        this.segments = segments;

        // Handle JAXB requirements for marshalling
        internalSegments = segments;
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
    public <X extends Path<SegmentType>> X append(final SegmentType aKey) {

        // Create a segment copy List.
        List<SegmentType> segments = new ArrayList<SegmentType>(this.segments);

        // Add the provided aKey instance and return.
        segments.add(aKey);
        return (X) new ListPath<SegmentType>(segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Path<SegmentType> that) {

        final Iterator<SegmentType> thisIt = this.iterator();
        final Iterator<SegmentType> thatIt = that.iterator();

        while (true) {

            // Unequal number of segments?
            if (!thisIt.hasNext()) {
                return -1;
            } else if (!thatIt.hasNext()) {
                return 1;
            }

            // Both iterators seem to have a next element
            SegmentType thisKey = thisIt.next();
            SegmentType thatKey = thatIt.next();

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
    public Iterator<SegmentType> iterator() {
        return Collections.unmodifiableList(segments).listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(int index) throws IndexOutOfBoundsException {
        return segments.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("{ ");
        for (SegmentType current : segments) {
            builder.append(current).append("/");

        }
        return builder.delete(builder.length() - "/".length(), builder.length()).append(" }").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(segments, "segments")
                .endExpressionAndValidate();
    }
}
