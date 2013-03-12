/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;

/**
 * Key type for JaxbUtils JAXBContext caching.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class SortedClassNameSetKey implements Comparable<SortedClassNameSetKey> {

    // Internal state
    private String syntheticKey;
    private SortedSet<String> classNames;

    /**
     * Compound constructor, creating a new SortedClassNameSetKey from the provided classNames Set.
     *
     * @param classNames A set of class names.
     */
    public SortedClassNameSetKey(final SortedSet<String> classNames) {

        Validate.notNull(classNames, "Cannot handle null classNames argument.");
        this.classNames = classNames;

        syntheticKey = classNames.toString();
    }

    /**
     * Checks if classNames within this SortedClassNameSetKey contains all classNames provided.
     *
     * @param classNames The class names to check for inclusion.
     * @return {@code true} if all the provided classNames were contained within this SortedClassNameSetKey, and
     *         {@code false} otherwise.
     */
    public boolean containsAll(final Set<String> classNames) {
        for (String current : classNames) {
            if (!this.classNames.contains(current)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return syntheticKey.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof SortedClassNameSetKey) {
            SortedClassNameSetKey that = (SortedClassNameSetKey) obj;
            return syntheticKey.equals(that.syntheticKey);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SortedClassNameSetKey that) {

        if (that == null) {
            return Integer.MIN_VALUE;
        }

        // Delegate comparison to the synthetic key.
        return syntheticKey.compareTo(that.syntheticKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return syntheticKey;
    }
}
