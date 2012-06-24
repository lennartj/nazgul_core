/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.predicate;

/**
 * Trivial holder type for a Tuple with a Key and a Value.
 * This is basically a public version of Map.Entry.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Tuple<K, V> {

    // Internal state
    private K key;
    private V value;

    /**
     * Compound constructor wrapping the key and value within this Tuple.
     *
     * @param key   The key of this Tuple.
     * @param value The value of this Tuple.
     */
    public Tuple(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return The tuple key.
     */
    public K getKey() {
        return key;
    }

    /**
     * @return The tuple value.
     */
    public V getValue() {
        return value;
    }
}
