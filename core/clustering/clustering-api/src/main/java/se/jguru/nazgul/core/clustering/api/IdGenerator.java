/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.clustering.api;

import java.io.Serializable;

/**
 * Specification for how to generate identifiers.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface IdGenerator extends Serializable {

    /**
     * @return A (cluster-)unique identifier for each call.
     */
    String getIdentifier();

    /**
     * @return {@code true} if this IdGenerator can deliver an identifier
     *         at the time of this method being called, and {@code false}
     *         otherwise.
     */
    boolean isIdentifierAvailable();
}
