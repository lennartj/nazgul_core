/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.tree.model.common.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.tree.model.common.EnumMapPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
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
