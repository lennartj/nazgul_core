/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.predicate;

/**
 * Generic type transformer.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Transformer<T, R> {

    /**
     * Transforms an element of type T to type R.
     *
     * @param input The input object to be transformed.
     * @return the result instance of type R, as a result of this function applied on the given input.
     */
    R transform(T input);
}
