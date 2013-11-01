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
package se.jguru.nazgul.core.algorithms.tree.model.path;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.algorithms.api.trees.path.SemanticPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/**
 * Refined StringPath which maps all segments to keys within an Enumeration.
 * This implies that all segments have a natural semantic meaning, as given
 * by the current-value Enumeration element.
 * <p/>
 * <strong>Note!</strong> This implementation is immutable, implying that the append method returns a new
 * instance, rather than modifying this one.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@NamedQueries({
        @NamedQuery(
                name = "getEnumMapPathsByEnumType",
                query = "select s from EnumMapPath s where s.enumClassName = ?1 order by s.compoundPath"),
        @NamedQuery(
                name = "getEnumMapPathByCompoundPath",
                query = "select s from EnumMapPath s where s.compoundPath like ?1 order by s.compoundPath")
})
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"enumClassName"})
@XmlAccessorType(XmlAccessType.FIELD)
public class EnumMapPath<E extends Enum<E>> extends StringPath implements SemanticPath<E, String> {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private String enumClassName;

    @Transient
    private transient EnumMap<E, String> shortcut;

    /**
     * JPA/JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public EnumMapPath() {
        super();
    }

    /**
     * Compound constructor, creating a new EnumMapPath instance from the supplied data,
     * and using the {@code AbstractPath.DEFAULT_SEGMENT_SEPARATOR} as segment separator.
     *
     * @param compoundPath The compound path representation of this AbstractPath.
     *                     Each segment string must be convertible to a {@code SegmentType} instance.
     * @param enumType     The type of Enum used for keys within this EnumMapPath.
     *                     Cannot be null or an Enum without any defined constants (since this would
     *                     result in an EnumMapPath which cannot hold any segments).
     */
    public EnumMapPath(final String compoundPath, final Class<E> enumType) {
        this(compoundPath, AbstractPath.DEFAULT_SEGMENT_SEPARATOR, enumType);
    }

    /**
     * Compound constructor, creating a new EnumMapPath instance from the supplied data.
     *
     * @param compoundPath     The compound path representation of this AbstractPath.
     *                         Each segment string must be convertible to a {@code SegmentType} instance.
     * @param segmentSeparator The string separating SegmentType representations from each other.
     * @param enumType         The type of Enum used for keys within this EnumMapPath.
     *                         Cannot be null or an Enum without any defined constants (since this would
     *                         result in an EnumMapPath which cannot hold any segments).
     * @throws IndexOutOfBoundsException if the supplied compoundPath had more segments than the supplied enumType
     *                                   has constants.
     */
    public EnumMapPath(final String compoundPath,
                       final String segmentSeparator,
                       final Class<E> enumType) throws IndexOutOfBoundsException {

        // Delegate
        super(compoundPath, segmentSeparator);

        // Check sanity
        Validate.notNull(enumType, "Cannot handle null enumType argument.");
        Validate.isTrue(enumType.getEnumConstants().length > 0,
                "Cannot handle enumType without any defined enum constants.");

        // Assign internal state
        this.enumClassName = enumType.getName();
        populateShortcutIfRequired(enumType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxSize() {

        // Check sanity
        populateShortcutIfRequired(null);

        // All done.
        return shortcut.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(final E semanticDefinition) {

        // Check sanity
        Validate.notNull(semanticDefinition, "Cannot handle null semanticDefinition argument.");
        populateShortcutIfRequired(null);

        // All done.
        return shortcut.get(semanticDefinition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public <X extends Path<String>> X append(final String aKey) {

        // Check sanity
        populateShortcutIfRequired(null);

        // All done.
        final String newCompoundPath = getCompoundPath() + getSegmentSeparator() + aKey;
        return (X) new EnumMapPath<E>(newCompoundPath, getSegmentSeparator(), getEnumType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Delegate to superclass first
        super.validateEntityState();

        // If JAXB har resurrected this object, the default constructor
        // has been called, but the populateShortcutIfRequired method has
        // not yet been invoked. In that case, do so manually
        if(shortcut == null && enumClassName != null) {
            populateShortcutIfRequired(null);
        }

        // Now, validate the local internal state
        InternalStateValidationException.create()
                .notNullOrEmpty(enumClassName, "enumClassName")
                .notNull(shortcut, "shortcut")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

    private void populateShortcutIfRequired(final Class<E> enumType) throws IndexOutOfBoundsException {

        // Check sanity
        final List<String> segments = getSegments();
        final boolean alreadyPopulated = shortcut != null && shortcut.size() == segments.size();
        final boolean notEnoughInformation = enumType == null && enumClassName == null;
        if (alreadyPopulated || notEnoughInformation) {
            return;
        }

        // Populate the shortcut
        final Class<E> eType = enumType != null ? enumType : getEnumType();
        final boolean enumTooSmallForSegments = eType.getEnumConstants().length < segments.size();
        if (enumTooSmallForSegments) {

            // Compose a sensible error message.
            final StringBuilder builder = new StringBuilder();
            builder.append("EnumMapPath using KeySet of type [")
                    .append(eType.getSimpleName())
                    .append("] cannot accept path with [")
                    .append(segments.size())
                    .append("] elements since it only has [")
                    .append(eType.getEnumConstants().length)
                    .append("] constants defined.");

            throw new IndexOutOfBoundsException(builder.toString());
        }

        // We should be fine; populate the shortcut
        // Since the internal state of this class is immutable, we need to synchronization.
        this.shortcut = new EnumMap<E, String>(eType);
        for(E current : eType.getEnumConstants()) {
            shortcut.put(current, null);
        }

        final Iterator<E> it = shortcut.keySet().iterator();
        for (String current : segments) {
            E currentKey = it.next();
            shortcut.put(currentKey, current);
        }
    }

    private Class<E> getEnumType() {
        try {
            return (Class<E>) getClass().getClassLoader().loadClass(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not acquire effective enumType.", e);
        }
    }
}
