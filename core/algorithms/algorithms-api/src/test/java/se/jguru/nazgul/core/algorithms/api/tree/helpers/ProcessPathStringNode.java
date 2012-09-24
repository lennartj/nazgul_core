/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api.tree.helpers;

import se.jguru.nazgul.core.algorithms.api.tree.EnumMapPath;

import java.util.EnumMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ProcessPathStringNode extends EnumMapPath<ProcessPath, String> {

    /**
     * Creates a new EnumMapPath with the provided segment definitions
     * and the given enumType for semantic path segment definitions.
     *
     * @param segments The segments within this path.
     */
    public ProcessPathStringNode(final EnumMap<ProcessPath, String> segments) {
        super(segments, ProcessPath.class);
    }
}
