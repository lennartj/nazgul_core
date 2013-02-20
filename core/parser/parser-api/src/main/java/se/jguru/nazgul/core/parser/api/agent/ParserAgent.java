/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.parser.api.agent;

/**
 * Specification for a parser (agent) which handles
 * token substitution with static and/or dynamic tokens.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ParserAgent {

    /**
     * Adds a static replacement, i.e. the token/key to be
     * replaced by the corresponding value.
     *
     * @param key   The key/token to be replaced.
     * @param value The value to use as replacement.
     */
    void addStaticReplacement(String key, String value);

    /**
     * Return true if this ParserAgent can handle the provided token, and false if not.
     *
     * @param token The token to investigate.
     * @return true if this ParserAgent can handle the provided token, and false if not.
     */
    boolean canHandle(String token);

    /**
     * Substitutes the provided token with its corresponding value.
     *
     * @param token The token to replace.
     * @return The replacement value.
     * @throws IllegalArgumentException if the token could not be handled by this ParserAgent.
     */
    String substituteValue(String token) throws IllegalArgumentException;
}
