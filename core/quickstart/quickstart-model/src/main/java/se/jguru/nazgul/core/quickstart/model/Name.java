/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-model
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.quickstart.model;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.StringTokenizer;

/**
 * Specification for how to build names for use within project structures.
 * A name normally consists of up to three parts, on the form {@code prefix-name-type}
 * (i.e. {@code nazgul-foo-api-parent} or similar). The order of the parts are as follows:
 * <ol>
 * <li><strong>prefix</strong>. An optional prefix for all names within a given project.</li>
 * <li><strong>name</strong>. A mandatory name.</li>
 * <li><strong>type</strong>. The type of name which may be used to indicate the functionality
 * of the name, such as "api", "impl-jpa", "assembly" etc.</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"prefix", "name", "type", "separator"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Name extends NazgulEntity implements Comparable<Name> {

    /**
     * Comparison value constant for indeterminate comparisons.
     */
    public static final int UNKNOWN = Integer.MIN_VALUE;

    /**
     * Default separator used between
     */
    public static final String DEFAULT_SEPARATOR = "-";

    // Internal state
    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String prefix;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String type;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String separator;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Name() {
    }

    /**
     * Compound constructor creating a new Name object wrapping the supplied data, and using
     * {@code DEFAULT_SEPARATOR} for separator.
     *
     * @param prefix The prefix part of this Name. Optional, i.e. may be null or empty.
     * @param name   The name part of this Name. Mandatory.
     * @param type   The type part of this Name. Mandatory
     */
    public Name(final String prefix, final String name, final String type) {
        this(prefix, name, type, DEFAULT_SEPARATOR);
    }

    /**
     * Compound constructor creating a new Name object wrapping the supplied data.
     *
     * @param prefix    The prefix part of this Name. Optional, i.e. may be null or empty.
     * @param name      The name part of this Name. Mandatory.
     * @param type      The type part of this Name. Mandatory.
     * @param separator The separator part of this Name. Mandatory.
     */
    public Name(final String prefix, final String name, final String type, final String separator) {
        this.prefix = prefix;
        this.name = name;
        this.type = type;
        this.separator = separator;
    }

    /**
     * @return The prefix part of this Name. Optional, i.e. may be null or empty.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return The non-empty name part of this Name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The non-empty type of this Name.
     */
    public String getType() {
        return type;
    }

    /**
     * @return The non-empty separator of this Name.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Name that) {
        int toReturn = getStandardNullComparisonValue(this, that);
        if (toReturn == UNKNOWN) {

            // Do a detailed comparison.
            final String thisPrefix = this.getPrefix() == null ? "" : this.getPrefix();
            final String thatPrefix = that.getPrefix() == null ? "" : that.getPrefix();
            toReturn = thisPrefix.compareTo(thatPrefix);

            if (toReturn == 0) {
                toReturn = this.getName().compareTo(that.getName());
            }
            if (toReturn == 0) {
                toReturn = this.getType().compareTo(that.getType());
            }
            if (toReturn == 0) {
                toReturn = this.getSeparator().compareTo(that.getSeparator());
            }
        }

        // All done
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (!(obj instanceof Name)) {
            return false;
        }

        // All done.
        return hashCode() == obj.hashCode();
    }

    /**
     * Joins the parts of this Name to a String, on the form {@code prefix-name-type},
     * where the "-" represents the separator.
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String prefixPart = prefix == null ? "" : prefix + getSeparator();
        return prefixPart + getName() + getSeparator() + getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prefixHash = prefix == null ? 0 : prefix.hashCode();
        return prefixHash + name.hashCode() + type.hashCode() + getSeparator().hashCode();
    }

    /**
     * Compares the two supplied objects, managing null and identity checks.
     * The return value is found as follows:
     * <ol>
     * <li><strong>If both objects are null</strong>: 0</li>
     * <li><strong>If both objects are identical (o1 == o2)</strong>: 0</li>
     * <li><strong>If o1 == null and o2 != null</strong>: -1</li>
     * <li><strong>If o1 != null and o2 == null</strong>: 1</li>
     * <li><strong>If neither o1 nor o2 is null</strong>: {@code UNKNOWN}</li>
     * </ol>
     * {@code 0} if both objects are {@code null} or identical. {@code 1} if o1 is not null and o2 is
     * {@code null}, and {@code -1} if o1 is null and o2 is not null. If both objects are non-null,
     * {@code UNKNOWN} is returned.
     *
     * @param o1 The left hand object.
     * @param o2 The right hand object.
     * @return {@code 0} if both objects are {@code null} or identical. {@code 1} if o1 is not null and o2 is
     * {@code null}, and {@code -1} if o1 is null and o2 is not null. If both objects are non-null,
     * {@code UNKNOWN} is returned.
     */
    public static int getStandardNullComparisonValue(final Object o1, final Object o2) {

        int toReturn = UNKNOWN;

        // Handle null and identity checks.
        if (o1 == null) {
            toReturn = o2 == null ? 0 : -1;
        } else if (o2 == null) {
            toReturn = 1;
        } else if (o1 == o2) {
            toReturn = 0;
        }

        return toReturn;
    }

    /**
     * Standard parsing method which creates a Name from the supplied toParse string, using DEFAULT_SEPARATOR
     * to identify the Name parts.
     *
     * @param toParse A non-empty string which should be parsed into a Name.
     * @return A Name created from the parts of the toParse string.
     */
    public static Name parse(final String toParse) {
        return parse(toParse, DEFAULT_SEPARATOR);
    }

    /**
     * Standard parsing method which creates a Name from the supplied toParse string, using the given
     * separator to identify the Name parts.
     *
     * @param toParse   A non-empty string which should be parsed into a Name.
     * @param separator A non-empty separator used to separate the parts of the toParse String.
     * @return A Name created from the parts of the toParse string.
     */
    public static Name parse(final String toParse, final String separator) {

        // Check sanity
        Validate.notEmpty(toParse, "Cannot handle null or empty toParse argument.");
        Validate.notEmpty(separator, "Cannot handle null or empty separator argument.");

        // The name should be on the form [prefix][separator][name][separator][type],
        // where the [prefix][separator] part is optional.
        final StringTokenizer tok = new StringTokenizer(toParse, separator, false);
        final int numTokens = tok.countTokens();
        Validate.isTrue(numTokens >= 2, "A Name must contain at least 2 parts (name and type). Found ["
                + numTokens + "] parts in [" + toParse + "] using separator [" + separator + "].");

        String prefix = null;
        if (numTokens > 2) {
            // In this case, we have a prefix.
            prefix = tok.nextToken();
        }

        final String name = tok.nextToken();

        // In case we have more separators, simply join the rest of the tokens
        // with the separator to re-create the type.
        final StringBuilder typeBuilder = new StringBuilder();
        while (tok.hasMoreTokens()) {
            typeBuilder.append(tok.nextToken());
            if (tok.hasMoreTokens()) {
                typeBuilder.append(separator);
            }
        }

        // All done.
        return new Name(prefix, name, typeBuilder.toString(), separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNullOrEmpty(type, "type")
                .notNullOrEmpty(separator, "separator")
                .endExpressionAndValidate();
    }
}
