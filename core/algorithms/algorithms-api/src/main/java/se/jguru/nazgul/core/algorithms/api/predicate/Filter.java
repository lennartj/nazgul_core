/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.predicate;

/**
 * Generic type filter.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Filter<T> {

    /**
     * Tests whether or not the specified candidate should be
     * included in this filter's selection.
     *
     * @param candidate The candidate to be tested.
     * @return <code>true</code> if the <code>candidate</code>
     *         should be included.
     */
    boolean accept(T candidate);
}
