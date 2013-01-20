/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import org.apache.commons.lang3.Validate;

/**
 * Trivial IdGenerator which always returns a constant string.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ConstantIdGenerator implements IdGenerator {

    // Internal state
    private String id;

    /**
     * Compound constructor creating a ConstantIdGenerator which always returns the provided id String.
     *
     * @param id The constant id.
     */
    public ConstantIdGenerator(final String id) {
        Validate.notEmpty(id, "Cannot handle null or empty id argument.");
        this.id = id;
    }

    /**
     * @return A (cluster-)unique identifier for each call.
     */
    @Override
    public final String getIdentifier() {
        return id;
    }

    /**
     * @return {@code true} if this IdGenerator can deliver an identifier
     *         at the time of this method being called, and {@code false}
     *         otherwise.
     */
    @Override
    public boolean isIdentifierAvailable() {
        return true;
    }
}
