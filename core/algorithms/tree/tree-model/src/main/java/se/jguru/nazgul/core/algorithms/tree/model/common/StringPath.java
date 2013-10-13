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
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractListPath implementation using Strings for path elements.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"segments"})
@XmlAccessorType(XmlAccessType.FIELD)
public class StringPath extends AbstractListPath<String> {

    // Internal state
    @ElementCollection(fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "segments")
    @XmlElement(name = "segment")
    private List<String> segments;

    /**
     * JPA/JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public StringPath() {
        super();
    }

    /**
     * Creates a StringPath holding a single segment (i.e. the root node in the path).
     *
     * @param rootSegment The only/root segment within this StringPath instance.
     */
    public StringPath(final String rootSegment) {

        // Check sanity
        Validate.notNull(rootSegment, "Cannot handle null rootSegment argument.");

        // Assign internal state
        this.segments = new ArrayList<String>();
        this.segments.add(rootSegment);
    }

    /**
     * Creates a StringPath object with the given segment List.
     *
     * @param segments The segments of the StringPath.
     */
    public StringPath(final List<String> segments) {

        // Check sanity
        Validate.notNull(segments, "Cannot handle null segments argument.");

        // Assign internal state
        this.segments = new ArrayList<String>(segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public <X extends Path<String>> X append(final String aKey) {

        // Copy the internal segments List
        final List<String> newSegments = new ArrayList<String>(segments);
        newSegments.add(aKey);

        // Wrap and return.
        return (X) new StringPath(newSegments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getSegments() {
        return segments;
    }
}
