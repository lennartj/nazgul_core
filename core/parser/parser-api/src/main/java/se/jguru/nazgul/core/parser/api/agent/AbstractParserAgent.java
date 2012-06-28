/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.parser.api.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Abstract parser agent which handles token substitution with static and/or dynamic tokens.
 * Static token replacement is performend within this class, while dynamic replacement
 * is delegated to concrete subclasses.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractParserAgent implements ParserAgent {

    // Internal state
    private Map<String, String> staticTokens = new TreeMap<String, String>();
    protected List<String> dynamicTokens = new ArrayList<String>();

    /**
     * Adds a static replacement, i.e. the token/key to be replaced by the corresponding value.
     *
     * @param key   The key/token to be replaced.
     * @param value The value to use as replacement.
     */
    @Override
    public void addStaticReplacement(final String key, final String value) {
        staticTokens.put(key, value);
    }

    /**
     * Return true if this AbstractParserAgent can handle the
     * provided token, and false if not.
     *
     * @param token The token to investigate.
     * @return true if this AbstractParserAgent can handle the provided
     *         token, and false if not.
     */
    @Override
    public final boolean canHandle(final String token) {

        if (tokenMatchInCollection(token, dynamicTokens)) {
            // This is a dynamic token
            return true;
        }

        if (tokenMatchInCollection(token, staticTokens.keySet())) {
            // This is a static token
            return true;
        }

        // All done.
        return false;
    }

    /**
     * Substitutes the provided token with its corresponding value.
     *
     * @param token The token to replace.
     * @return The replacement value.
     * @throws IllegalArgumentException if the token could not be handled by this ParserAgent.
     */
    @Override
    public final String substituteValue(final String token) throws IllegalArgumentException {

        // Is this a dynamic token?
        if (tokenMatchInCollection(token, dynamicTokens)) {
            return performDynamicReplacement(token);
        }

        // This should be a static token.
        for (String current : staticTokens.keySet()) {
            if (Pattern.compile(current).matcher(token).matches()) {
                return staticTokens.get(current);
            }
        }

        // Token unknown. Complain.
        throw new IllegalArgumentException("Could not handle token [" + token + "]");
    }

    /**
     * Perform dynamic replacement for the provided token.
     *
     * @param token The token to dynamically replace.
     * @return The replacement value.
     */
    protected abstract String performDynamicReplacement(final String token);

    //
    // Private helpers
    //

    private boolean tokenMatchInCollection(final String token, final Collection<String> coll) {

        for (String current : coll) {
            if (Pattern.compile(current).matcher(token).matches()) {
                return true;
            }
        }

        return false;
    }
}
