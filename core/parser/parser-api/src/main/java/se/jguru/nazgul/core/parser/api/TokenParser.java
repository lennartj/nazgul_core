/*
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
     * Initializes this TokenParser, indicating that the supplied TokenDefinitions should be used to acquire
     * charSequences for start and end of tokens, as well as a pattern to recognize a Token.
     * This initialize method can only be called once, before any call to the {@code substituteTokens} method
     * has been done.
     *
     * @param tokenDefinitions a non-null TokenDefinitions object.
     * @throws java.lang.IllegalStateException if this TokenParser has already been initialized.
     */
    void initialize(TokenDefinitions tokenDefinitions) throws IllegalStateException;

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
