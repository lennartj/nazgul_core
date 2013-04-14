/*
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
    String TOKEN_START = "\\$\\{";

    /**
     * Definition for the "End of token" identifier.
     */
    String TOKEN_END = "\\}";

    /**
     * Regular expression definition of a token.
     */
    Pattern TOKEN_REGEXP = Pattern.compile(TOKEN_START + "[^}]*" + TOKEN_END);

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
