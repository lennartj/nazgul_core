/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.algorithms.api.trees.path.Path;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * AbstractPath implementation using Strings for path elements.
 * <strong>Note!</strong> This implementation is immutable, implying that the append method returns a new
 * instance, rather than modifying this one.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({
        @NamedQuery(
                name = "getStringPathByCompoundPath",
                query = "select s from StringPath s where s.compoundPath like ?1 order by s.compoundPath")
})
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class StringPath extends AbstractPath<String> {

    // Internal state
    @Transient
    @XmlTransient
    private List<String> segments;

    /**
     * JPA/JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public StringPath() {
        super();
    }

    /**
     * Creates a StringPath with the supplied compoundPath.
     *
     * @param compoundPath The compoundPath of this StringPath instance.
     */
    public StringPath(final String compoundPath) {

        // Delegate
        this(compoundPath, DEFAULT_SEGMENT_SEPARATOR);
    }

    /**
     * Compound constructor, creating a new AbstractPath instance from the supplied data.
     *
     * @param compoundPath     The compound path representation of this AbstractPath.
     *                         Each segment string must be convertible to a {@code SegmentType} instance.
     * @param segmentSeparator The string separating SegmentType representations from each other.
     */
    public StringPath(final String compoundPath, final String segmentSeparator) {

        // Delegate
        super(compoundPath, segmentSeparator);

        // Parse and assign internal state
        segments = parseCompoundPath(compoundPath, segmentSeparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public <X extends Path<String>> X append(final String aKey) {

        // Wrap and return
        final String newCompoundPath = getCompoundPath() + getSegmentSeparator() + aKey;
        return (X) new StringPath(newCompoundPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSegments() {

        // Check sanity
        if (segments == null && getCompoundPath() != null) {
            segments = parseCompoundPath(getCompoundPath(), getSegmentSeparator());
        }

        // All done.
        return segments;
    }

    /**
     * Parses the supplied compoundPath using the supplied pathSeparator, returning the resulting
     * List of path segments.
     *
     * @param compoundPath  The compound path to parse into a List of segments.
     * @param pathSeparator The pathSeparator where the given compoundPath should be split.
     * @return The resulting List of path segments.
     */
    public static List<String> parseCompoundPath(final String compoundPath, final String pathSeparator) {

        // Check sanity
        Validate.notEmpty(pathSeparator, "pathSeparator");
        Validate.notNull(compoundPath, "compoundPath");

        // Parse and return
        final List<String> toReturn = new ArrayList<String>();
        final StringTokenizer tok = new StringTokenizer(compoundPath, pathSeparator, false);
        if (tok.countTokens() == 0) {
            toReturn.add(compoundPath);
        } else {
            while (tok.hasMoreTokens()) {
                toReturn.add(tok.nextToken());
            }
        }

        // All done.
        return toReturn;
    }
}
