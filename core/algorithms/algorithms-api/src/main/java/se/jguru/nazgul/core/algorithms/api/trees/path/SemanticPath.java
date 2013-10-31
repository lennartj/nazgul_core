/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.trees.path;

import java.io.Serializable;

/**
 * An extension to Path where all segments have a semantic meaning, defined
 * by the values of an Enum. This implies that all levels of the path correspond
 * to some concept, and that the SemanticPath in essence works as a Map relating
 * Enum values to SegmentType values.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SemanticPath<E extends Enum<E>, SegmentType extends Serializable & Comparable<SegmentType>>
        extends Path<SegmentType> {

    /**
     * @return The maximum number of segments in this Path, typically identical to the
     *         number of values in the semantic definition Enum.
     */
    int getMaxSize();

    /**
     * Retrieves the SegmentType segment corresponding to the Enum value providing
     * the semantic definition of this SemanticPath.
     *
     * @param semanticDefinition The semantic definition within this SemanticPath whose value should be retrieved.
     * @return The segment corresponding to the provided semantic definition, or {@code null} if no segment SegmentType
     *         is defined for the provided semanticDefinition level within this SemanticPath.
     */
    SegmentType get(E semanticDefinition);
}
