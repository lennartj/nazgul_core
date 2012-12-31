/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.collections.predicate;

/**
 * Generic type aggregator.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Aggregator<T, R> {

    /**
     * Performs aggregation of the candidate and the current (aggregate) result.
     *
     * @param candidate The candidate to be reduced / aggregated into the current value.
     * @param current   The current aggregate value. Note that this value is null on the first call to aggregate.
     * @return The result of the aggregate operation between the current result and the provided candidate.
     */
    R aggregate(R current, T candidate);
}
