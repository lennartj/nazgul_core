/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api.trees.helpers;

import se.jguru.nazgul.core.algorithms.api.trees.EnumMapPath;
import se.jguru.nazgul.core.algorithms.api.trees.Path;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;

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
