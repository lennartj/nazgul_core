/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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
package se.jguru.nazgul.core.persistence.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Trivial utility class for Entities.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class Entities {

    // Hide the constructor in utility classes.
    private Entities() {
    }

    /**
     * Standard fields to disregard in calculations.
     */
    public static final String[] STANDARD_DISREGARD_FIELDS = {"version", "id",
            "pcVersionInit", "pcStateManager", "pcDetachedState"};

    /**
     * Standard equality calculations between two objects of a given type.
     *
     * @param rhs       The right-hand side comparison.
     * @param lhs       The left-hand side comparison.
     * @param stopClass The topmost class to compare.
     * @param <T>       The type of the comparands.
     * @return {@code true} if the two comparisons were equal.
     */
    public static <T> boolean equals(final T lhs, final T rhs, final Class<? super T> stopClass) {
        return EqualsBuilder.reflectionEquals(rhs, lhs, false, stopClass, STANDARD_DISREGARD_FIELDS);
    }

    /**
     * Builds a hashCode from the supplied data.
     *
     * @param anObject The object for which a HashCode should be constructed.
     * @param stopType The type up to which we should reflect.
     * @param <T>      The type of object to acquire a hashcode for.
     * @return The resulting hash for the supplied instance.
     */
    public static <T> int hashCode(final T anObject, final Class<? super T> stopType) {
        return HashCodeBuilder.reflectionHashCode(17, 37, anObject, false, stopType, STANDARD_DISREGARD_FIELDS);
    }

    /**
     * Performs reflective comparison between the lhs and rhs objects, calculating non-transient properties up to the
     * supplied stopType.
     *
     * @param rhs      The right-hand side comparison object.
     * @param lhs      The left-hand side comparison object.
     * @param stopType The type up to which we should reflect.
     * @param <T>      The type of object to acquire a comparison for.
     * @return The resulting comparison value.
     */
    public static <T> int compare(final T lhs, final T rhs, final Class<? super T> stopType) {
        return CompareToBuilder.reflectionCompare(lhs, rhs, false, stopType, STANDARD_DISREGARD_FIELDS);
    }
}
