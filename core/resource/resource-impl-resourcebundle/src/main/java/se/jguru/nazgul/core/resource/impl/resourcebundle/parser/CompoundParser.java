/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.resource.impl.resourcebundle.parser;

import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent;
import se.jguru.nazgul.core.parser.api.agent.HostNameParserAgent;

import java.util.Map;
import java.util.TreeMap;

/**
 * Compound parser implementation for resource token substitutions.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CompoundParser extends DefaultTokenParser {

    // Constants
    private static final String SEPARATOR = "=";
    private static final DefaultParserAgent DEFAULT_PARSER_AGENT = new DefaultParserAgent();
    private static final HostNameParserAgent HOST_NAME_PARSER_AGENT = new HostNameParserAgent();

    /**
     * Creates a new CompoundParser using the provided staticReplacementTokens for the maps.
     *
     * @param staticReplacementTokens a map holding static replacement tokens for this ResourceTokenParser.
     */
    public CompoundParser(final Map<String, String> staticReplacementTokens) {

        // Add a ParserAgent with the provided staticReplacementTokens
        if (staticReplacementTokens != null) {

            StaticReplacementParserAgent staticReplacementParserAgent = new StaticReplacementParserAgent();
            for (String current : staticReplacementTokens.keySet()) {
                staticReplacementParserAgent.addStaticReplacement(current, staticReplacementTokens.get(current));
            }

            addAgent(staticReplacementParserAgent);
        }

        // Add the default agents.
        addAgent(DEFAULT_PARSER_AGENT);
        addAgent(HOST_NAME_PARSER_AGENT);
    }

    /**
     * Convenience factory method which creates a new ResourceTokenParser from the list of key=value elements
     * given within the keyValueTokenList.
     *
     * @param keyValueTokenList Either <code>null</code>, or a list holding strings on the form key=value where
     *                          both key and value must be non-empty.
     * @return A fully set up ResourceTokenParser.
     * @throws IllegalArgumentException if any of the tokens are malformed (i.e. not on the form key=value where
     *                                  key and value are non-empty).
     */
    public static CompoundParser create(final String... keyValueTokenList)
            throws IllegalArgumentException {

        if (keyValueTokenList == null || keyValueTokenList.length == 0) {
            return new CompoundParser(null);
        }

        Map<String, String> staticReplacementTokens = new TreeMap<String, String>();
        for (String current : keyValueTokenList) {
            if (!current.contains(SEPARATOR)) {
                throw new IllegalArgumentException("Illegal token [" + current + "]. Required form KEY=VALUE.");
            }

            final String key = current.substring(0, current.indexOf(SEPARATOR)).trim();
            final String value = current.substring(current.indexOf(SEPARATOR) + 1).trim();

            if (key.length() == 0 || value.length() == 0) {
                throw new IllegalArgumentException("Illegal token [" + current
                        + "]. Required form KEY=VALUE (both key and value must be non-empty).");
            }

            staticReplacementTokens.put(key, value);
        }

        // All done.
        return new CompoundParser(staticReplacementTokens);
    }
}