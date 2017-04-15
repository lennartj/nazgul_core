/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */

package se.jguru.nazgul.core.algorithms.api.trees.path;

import java.io.Serializable;

/**
 * Generic immutable Path, defining a single continuous list of segments.
 * This is applicable - for example - as a Path within a Tree.
 *
 * @param <S> The type of segment from which this Path is constructed.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Path<S extends Comparable<S>> extends Iterable<S>, Comparable<Path<S>>, Serializable {

    /**
     * @return The number of segments in this Path.
     */
    int size();

    /**
     * Retrieves the path segment with the provided index.
     *
     * @param index the index (which must be smaller than {@code size}).
     * @return The KeyType instance found on the provided position.
     * @throws IndexOutOfBoundsException if {@code index} was smaller than 0 or
     *                                   greater than {@code size}.
     */
    S get(int index) throws IndexOutOfBoundsException;

    /**
     * Appends the provided KeyType to this Path, returning the resulting Path.
     * Note that the returned value could/should be a new Path subtype instance, given
     * that the implementation's internal state is immutable.
     *
     * @param aKey The KeyType to append to this Path.
     * @param <X>  The explicit Path subtype.
     * @return The Path result from appending the provided KeyType instance to this Path.
     * Note that the returned value could/should be a new Path subtype, given that
     * the implementation's internal state is immutable.
     */
    <X extends Path<S>> X append(S aKey);
}
