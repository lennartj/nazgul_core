/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Non-semantic Path implementation using List as internal storage of KeyType segments.
 * <strong>Note!</strong> This implementation is immutable, implying that the append method returns a new
 * instance, rather than modifying this one.
 * <p/>
 * {@inheritDoc}
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"compoundPath", "segmentSeparator"})
@SuppressWarnings("PMD.UnusedPrivateField")
public abstract class AbstractPath<SegmentType extends Serializable & Comparable<SegmentType>>
        extends NazgulEntity implements Path<SegmentType> {

    /**
     * The separator between segments of this AbstractPath.
     * This DEFAULT_SEGMENT_SEPARATOR is used in the default {@code toString} implementation,
     * as glue between segments.
     */
    public static final String DEFAULT_SEGMENT_SEPARATOR = "/";

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String compoundPath;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String segmentSeparator;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public AbstractPath() {
    }

    /**
     * Convenience constructor, creating a new AbstractPath instance with the supplied compoundPath
     * and using the {@inheritDoc DEFAULT_SEGMENT_SEPARATOR} for separator between segments.
     *
     * @param compoundPath The compound path representation of this AbstractPath.
     *                     Each segment string must be convertible to a {@code SegmentType} instance.
     */
    public AbstractPath(final String compoundPath) {
        this(compoundPath, DEFAULT_SEGMENT_SEPARATOR);
    }

    /**
     * Compound constructor, creating a new AbstractPath instance from the supplied data.
     *
     * @param compoundPath     The compound path representation of this AbstractPath.
     *                         Each segment string must be convertible to a {@code SegmentType} instance.
     * @param segmentSeparator The string separating SegmentType representations from each other.
     */
    public AbstractPath(final String compoundPath, final String segmentSeparator) {

        // Check sanity
        Validate.notNull(compoundPath, "Cannot handle null compoundPath argument.");
        Validate.notEmpty(segmentSeparator, "Cannot handle nulll or empty segmentSeparator argument.");

        // Assign internal state
        this.compoundPath = compoundPath;
        this.segmentSeparator = segmentSeparator;
    }

    /**
     * @return The List of SegmentType instances which is this Path.
     */
    protected abstract List<SegmentType> getSegments();


    /**
     * @return A compound string representation of this AbstractPath used for JPA storage and searches.
     *         The compoundPath is represented as the segments, separated by the {@code segmentSeparator} string,
     *         or the {@code DEFAULT_SEGMENT_SEPARATOR} if no explicit segmentSeparator is given.
     */
    public String getCompoundPath() {
        return compoundPath;
    }

    /**
     * @return The segment separator string, or the {@code DEFAULT_SEGMENT_SEPARATOR} if no
     *         explicit segmentSeparator string is given.
     */
    public String getSegmentSeparator() {
        return segmentSeparator == null ? DEFAULT_SEGMENT_SEPARATOR : segmentSeparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return getSegments().size();
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
            boolean thisHasNext = thisIt.hasNext();
            boolean thatHasNext = thatIt.hasNext();
            if (!thisHasNext && !thatHasNext) {
                return 0;
            }
            if (!thisHasNext) {
                return -1;
            } else if (!thatHasNext) {
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
    @SuppressWarnings("all")
    public boolean equals(final Object obj) {

        // Check sanity; fail fast.
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        // Check sizes and types
        final AbstractPath that = (AbstractPath) obj;
        if(this.size() != that.size()) {
            return false;
        }

        // Delegate
        return this.toString().equals(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SegmentType> iterator() {
        return Collections.unmodifiableList(getSegments()).listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(final int index) throws IndexOutOfBoundsException {
        return getSegments().get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        for (SegmentType current : getSegments()) {
            builder.append(current).append(getSegmentSeparator());
        }

        // Remove trailing separator
        return builder.delete(builder.length() - getSegmentSeparator().length(), builder.length()).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(getSegments(), "segments")
                .notNull(compoundPath, "compoundPath")
                .endExpressionAndValidate();
    }
}
