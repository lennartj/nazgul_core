/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api.tree.helpers;

import se.jguru.nazgul.core.algorithms.api.tree.EnumMapPath;
import se.jguru.nazgul.core.algorithms.api.tree.TreeAlgorithms;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AdjustmentStringNode extends EnumMapPath<Adjustment, String> {

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The segments within this path.
     */
    public AdjustmentStringNode(final EnumMap<Adjustment, String> segments) {
        super(segments, Adjustment.class);
    }

    /**
     * Standard factory method.
     *
     * @param segmentValues The values in the ordinal order.
     * @return The fully created AdjustmentStringNode.
     */
    public static AdjustmentStringNode create(final List<String> segmentValues) {

        final EnumMap<Adjustment, String> segmentMap = TreeAlgorithms.getEmptyEnumMap(Adjustment.class);

        Iterator<String> it = segmentValues.iterator();
        for (Adjustment current : segmentMap.keySet()) {
            if (!it.hasNext()) {
                break;
            }

            segmentMap.put(current, it.next());
        }

        // All done.
        return new AdjustmentStringNode(segmentMap);
    }
}
