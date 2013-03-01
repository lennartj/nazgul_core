/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import java.util.UUID;

/**
 * An IdGenerator, supplying a new UUID value for each call to {@code getIdentifier}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class UUIDGenerator implements IdGenerator {

    /**
     * @return {@code UUID.randomUUID().toString()}, implying a new/unique UUID for each call
     *         to this method.
     */
    @Override
    public final String getIdentifier() {
        return UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isIdentifierAvailable() {
        return true;
    }
}
