/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.SemanticPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Semantic Path implementation using an Enum to define the semantic meaning of
 * each segment within the Path. Uses an EnumMap to relate semantics to mapPathSegments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"enumTypeClass", "jaxbState"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class EnumMapPath<E extends Enum<E>, SegmentType extends Serializable & Comparable<SegmentType>>
        extends AbstractListPath<SegmentType> implements SemanticPath<E, SegmentType> {

    // Internal state
    @XmlTransient
    @Transient
    private E[] enumConstants;

    @XmlTransient
    @Transient
    private Class<E> enumType;

    @Transient
    @XmlTransient
    private final Object lock = new Object();

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private String enumTypeClass;

    @XmlTransient
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<E, SegmentType> jpaState;

    @Transient
    @XmlElementWrapper(name = "segments")
    @XmlElement(name = "segment")
    private TreeMap<E, SegmentType> jaxbState;

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

        enumTypeClass = enumType.getName();
        resurrectInternalHelperStateIfRequired();

        Validate.isTrue(enumConstants.length > 0, "Cannot create an EnumMapPath from an Enum with no constants.");

        // Assign internal state
        this.enumType = enumType;
        this.enumConstants = enumType.getEnumConstants();
        this.jpaState = new TreeMap<E, SegmentType>();
        for(E current : enumConstants) {
            jpaState.put(current, null);
        }

        for (Map.Entry<E, SegmentType> current : segments.entrySet()) {
            this.jpaState.put(current.getKey(), current.getValue());
        }

        this.jaxbState = new TreeMap<E, SegmentType>(jpaState);

        // Check sanity
        boolean noNullsFound = true;
        for (Map.Entry<E, SegmentType> current : this.jpaState.entrySet()) {
            if (current.getValue() == null) {
                noNullsFound = false;
            } else {
                Validate.isTrue(noNullsFound, "Paths cannot contain null elements. Got: " + this.jpaState);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxSize() {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done.
        return jpaState.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(final E semanticDefinition) {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done.
        return jpaState.get(semanticDefinition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<SegmentType> getSegments() {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done.
        return new ArrayList<SegmentType>(jpaState.values());
    }

    /**
     * Adds the supplied key/value pair to the internal representation of mapPathSegment.
     *
     * @param key   The key in the assigned pair.
     * @param value The pathSegment value.
     * @return The former value, or {@code null} if no previous value was found for the supplied key.
     */
    public SegmentType put(final E key, final SegmentType value) {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done.
        synchronized (lock) {
            jaxbState.put(key, value);
            return jpaState.put(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {

        resurrectInternalHelperStateIfRequired();
        int toReturn = 0;

        for (Map.Entry<E, SegmentType> current : jpaState.entrySet()) {
            if (current.getValue() == null) {
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

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        if (size() >= getMaxSize()) {
            throw new IndexOutOfBoundsException("Cannot append key [" + aSegment + "] to path [" + this
                    + "]. Maximum depth [" + getMaxSize() + "] reached.");
        }

        // Perform a deep clone on the mapPathSegments map.
        final EnumMap<E, SegmentType> clone = TreeAlgorithms.getEmptyEnumMap(enumType);
        synchronized (lock) {
            for (Map.Entry<E, SegmentType> current : jpaState.entrySet()) {

                final SegmentType segmentType = current.getValue();
                final SegmentType toPut = segmentType == null ? null : (SegmentType) deepClone(segmentType);
                clone.put(current.getKey(), toPut);
            }
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

        // Check sanity
        resurrectInternalHelperStateIfRequired();

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
     * @return Retrieves the class of the EnumType used by this EnumMapPath.
     */
    public final Class<E> getEnumType() {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done.
        return enumType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SegmentType> iterator() {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // All done
        return new SemanticPathSegmentIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentType get(final int index) throws IndexOutOfBoundsException {

        // Check sanity
        resurrectInternalHelperStateIfRequired();
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index [" + index
                    + "] must be smaller than path size [" + size() + "].");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Cannot handle negative index argument.");
        }
        if (jpaState == null) {

            // Re-build the mapPathSegments?
            throw new IllegalStateException("mapPathSegments: " + jpaState);
        }

        // All done.
        final E key = enumConstants[index];
        return key == null ? null : jpaState.get(key);
    }

    /**
     * Equality definition, including the values of each path segment in the equality expression.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
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

        // Delegate; check that each path segment value is equal.
        resurrectInternalHelperStateIfRequired();
        final EnumMapPath that = (EnumMapPath) obj;
        if (!getEnumType().equals(that.getEnumType())) {
            return false;
        }

        final EqualsBuilder builder = new EqualsBuilder().append(getMaxSize(), that.getMaxSize());
        for (E current : getEnumType().getEnumConstants()) {
            builder.append(get(current), that.get(current));
        }

        // All done.
        return builder.isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        // Check sanity
        resurrectInternalHelperStateIfRequired();

        // Delegate
        final String separator = "\n";
        StringBuilder builder = new StringBuilder("EnumMapPath { \n");
        for (Map.Entry<E, SegmentType> current : jpaState.entrySet()) {
            if (current.getValue() != null) {
                builder.append("  ").append(current.getKey()).append("=").append(current.getValue()).append(separator);
            }
        }
        return builder.delete(builder.length() - separator.length(), builder.length()).append(" }").toString();
    }

    //
    // Private helpers
    //

    private void resurrectInternalHelperStateIfRequired() {

        // Already done?
        if (enumConstants == null) {

            // Load the enum class
            for (ClassLoader loader : Arrays.asList(Thread.currentThread().getContextClassLoader(),
                    getClass().getClassLoader())) {
                try {

                    // Attempt to load the Enum subclass.
                    enumType = (Class<E>) loader.loadClass(enumTypeClass);
                    if (enumType != null) {

                        // Restore internal state
                        enumConstants = enumType.getEnumConstants();
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore this.
                }
            }
        }

        // Check sanity
        Validate.notNull(enumType, "Could not resurrect enumType from enumTypeClass [" + enumTypeClass + "]");
    }

    /**
     * Creates an EnumMap with all keys in the provided enumeration type in enumeration
     * value order, and all values {@code null}.
     *
     * @param enumType The type of Enum which should constitute keys in the returned EnumMap.
     * @param <K>      The Enum key type used in the returned EnumMap.
     * @param <V>      The value type used in the returned EnumMap .... noting that all values in the
     *                 returned EnumMap are {@code null}.
     * @return an EnumMap with all keys in the provided enumeration type in enumeration
     *         value order, and all values {@code null}.
     */
    public static <K extends Enum<K>, V> EnumMap<K, V> getEmptyEnumMap(final Class<K> enumType) {

        // Check sanity
        Validate.notNull(enumType, "Cannot handle null enumType argument.");

        // Create an EnumMap with null values.
        EnumMap<K, V> toReturn = new EnumMap<K, V>(enumType);
        for (K current : enumType.getEnumConstants()) {
            toReturn.put(current, null);
        }

        // All Done.
        return toReturn;
    }

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
            return withinBounds && jpaState.get(constants[currentOrdinal + 1]) != null;
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
            return jpaState.get(cursor);
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

        // Delegate
        super.validateEntityState();

        InternalStateValidationException.create()
                .notNullOrEmpty(enumTypeClass, "enumTypeClass")
                .notNull(jaxbState, "jaxbState")
                .notNull(jpaState, "jpaState")
                .endExpressionAndValidate();
    }
}
