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

import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
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
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@SuppressWarnings("PMD.UnusedPrivateField")
public abstract class AbstractListPath<SegmentType extends Serializable & Comparable<SegmentType>>
        extends NazgulEntity implements Path<SegmentType> {

    /**
     * @return The List of SegmentType instances which is this Path.
     */
    protected abstract List<SegmentType> getSegments();

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
    @SuppressWarnings("all")
    public boolean equals(final Object obj) {

        // Check sanity; fail fast.
        if(obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        if(obj == this) {
            return true;
        }

        // Delegate to comparing the properties
        final AbstractListPath that = (AbstractListPath) obj;
        if(!(size() == that.size())) {
            return false;
        }

        // Delegate.
        return this.compareTo(that) == 0;
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

        StringBuilder builder = new StringBuilder("{ ");
        for (SegmentType current : getSegments()) {
            builder.append(current).append("/");
        }

        // Remove the trailing '/'
        return builder.delete(builder.length() - "/".length(), builder.length()).append(" }").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(getSegments(), "segments")
                .endExpressionAndValidate();
    }
}
