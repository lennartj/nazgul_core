/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.predicate;

/**
 * Generic operation wrapper.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Operation<T> {

    /**
     * Performs a no-return operation on the provided target.
     *
     * @param target The target to operate on.
     */
    void operate(T target);
}
