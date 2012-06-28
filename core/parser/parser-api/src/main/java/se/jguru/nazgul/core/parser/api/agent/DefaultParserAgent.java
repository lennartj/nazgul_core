/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.parser.api.agent;

import java.util.Map;

/**
 * Standard ParserAgent which performs dynamic substitutions from System.getEnv
 * and System.getProperty maps.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultParserAgent extends AbstractParserAgent {

    /**
     * Prefix indicating that a token replacement value should
     * be acquired from environment settings using <code>System.getenv("someKey")</code>,
     * given that the token is <code>${env:someKey}</code>
     */
    public static final String ENVIRONMENT_PREFIX = "env:";

    /**
     * Prefix indicating that a token replacement value should
     * be acquired from Java System Properties using
     * <code>System.getProperty("someKey")</code>,
     * given that the token is <code>${sysprop:someKey}</code>
     */
    public static final String SYSPROP_PREFIX = "sysprop:";

    /**
     * Convenience constructor to create a DefaultParserAgent without any static substitution tokens.
     */
    public DefaultParserAgent() {
        this(null);
    }

    /**
     * Creates a Standard parser agent, with the provided static tokens.
     *
     * @param staticTokens The static replacement tokens to use.
     */
    public DefaultParserAgent(final Map<String, String> staticTokens) {

        if (staticTokens != null) {
            for (String current : staticTokens.keySet()) {
                addStaticReplacement(current, staticTokens.get(current));
            }
        }

        // Add the standard dynamic replacement tokens.
        dynamicTokens.add(ENVIRONMENT_PREFIX + ".*");
        dynamicTokens.add(SYSPROP_PREFIX + ".*");
    }

    /**
     * Perform dynamic replacement for the provided token.
     *
     * @param token The token to dynamically replace.
     * @return The replacement value.
     */
    @Override
    public String performDynamicReplacement(final String token) {

        // Is this an environment variable?
        if (token.startsWith(ENVIRONMENT_PREFIX)) {
            String key = token.substring(ENVIRONMENT_PREFIX.length());
            return "" + System.getenv(key);
        }

        // Is this a system property?
        if (token.startsWith(SYSPROP_PREFIX)) {
            String key = token.substring(SYSPROP_PREFIX.length());
            return "" + System.getProperty(key);
        }

        // Unknown token. Complain.
        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }
}
