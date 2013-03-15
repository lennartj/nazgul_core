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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.Path;
import se.jguru.nazgul.core.algorithms.tree.model.common.EnumMapPath;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProcessStringPath extends EnumMapPath<ProcessPathSegments, String> {

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The segments within this path.
     */
    public ProcessStringPath(final EnumMap<ProcessPathSegments, String> segments) {
        super(segments, ProcessPathSegments.class);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if a new EnumMapPath could not be created
     *                               using this EnumMapPath instance by concatenating
     *                               the provided KeyType instance.
     */
    @Override
    public <X extends Path<String>> X append(String aKey) {
        return super.append(aKey);
    }

    /**
     * Standard factory method.
     *
     * @param segmentValues The values in the ordinal order.
     * @return The fully created ProcessStringPath.
     */
    public static ProcessStringPath create(final List<String> segmentValues) {

        final EnumMap<ProcessPathSegments, String> segmentMap = TreeAlgorithms.getEmptyEnumMap(ProcessPathSegments.class);

        Iterator<String> it = segmentValues.iterator();
        for (ProcessPathSegments current : segmentMap.keySet()) {
            if (!it.hasNext()) {
                break;
            }

            segmentMap.put(current, it.next());
        }

        // All done.
        return new ProcessStringPath(segmentMap);
    }
}
