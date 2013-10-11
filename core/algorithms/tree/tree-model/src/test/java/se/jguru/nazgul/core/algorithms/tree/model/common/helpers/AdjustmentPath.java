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
package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.common.EnumMapPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@Entity
public class AdjustmentPath extends EnumMapPath<Adjustment, String> {

    public AdjustmentPath() {
        // Do nothing
    }

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The segments within this path.
     */
    public AdjustmentPath(final EnumMap<Adjustment, String> segments) {
        super(segments, Adjustment.class);
    }

    /**
     * Standard factory method.
     *
     * @param segments The map entry values in this AdjustmentPath.
     * @return The fully created AdjustmentStringNode.
     */
    public static AdjustmentPath create(final Map<Adjustment, String> segments) {

        final EnumMap<Adjustment, String> segmentMap = TreeAlgorithms.getEmptyEnumMap(Adjustment.class);
        for(Map.Entry<Adjustment, String> current : segments.entrySet()) {
            segmentMap.put(current.getKey(), current.getValue());
        }

        // All done.
        return new AdjustmentPath(segmentMap);
    }

    /**
     * Standard factory method.
     *
     * @param segmentValues The values in the ordinal order.
     * @return The fully created AdjustmentStringNode.
     */
    public static AdjustmentPath create(final List<String> segmentValues) {

        final EnumMap<Adjustment, String> segmentMap = TreeAlgorithms.getEmptyEnumMap(Adjustment.class);

        Iterator<String> it = segmentValues.iterator();
        for (Adjustment current : segmentMap.keySet()) {
            if (!it.hasNext()) {
                break;
            }

            segmentMap.put(current, it.next());
        }

        // All done.
        return new AdjustmentPath(segmentMap);
    }
}
