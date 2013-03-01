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
public final class ConstantIdGenerator implements IdGenerator {

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
     * {@inheritDoc}
     */
    @Override
    public final String getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isIdentifierAvailable() {
        return true;
    }
}
