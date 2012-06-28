/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.parser.api;

import se.jguru.nazgul.core.parser.api.agent.ParserAgent;

import java.util.regex.Pattern;

/**
 * Specification for a Parser that recognizes String tokens, and offers
 * substituting values for the token placeholders.
 * The substitution is delegated to an extensible set of ParserAgents,
 * to simplify complex substitution policies.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TokenParser {

    /**
     * Definition for the "Start of token" identifier.
     */
    public static final String TOKEN_START = "\\$\\{";

    /**
     * Definition for the "End of token" identifier.
     */
    public static final String TOKEN_END = "\\}";

    /**
     * Regular expression definition of a token.
     */
    public static final Pattern TOKEN_REGEXP = Pattern.compile(TOKEN_START + "[^}]*" + TOKEN_END);

    /**
     * Adds a parserAgent to the list of known AbstractParserAgents.
     *
     * @param parserAgent the parserAgent to add.
     * @throws IllegalArgumentException if the parserAgent argument was <code>null</code>.
     */
    void addAgent(ParserAgent parserAgent) throws IllegalArgumentException;

    /**
     * Replaces the tokens found within the data.
     *
     * @param data The data in which to replace existing tokens.
     * @return The token-substituted data.
     */
    String substituteTokens(String data);
}
