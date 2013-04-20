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

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.parser.api.agent.ParserAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Simple implementation of a String replacement parser.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTokenParser implements TokenParser {

    // Internal state
    private List<ParserAgent> parseAgents = new ArrayList<ParserAgent>();

    /**
     * Adds a parserAgent to the list of known AbstractParserAgents.
     *
     * @param parserAgent the parserAgent to add.
     * @throws IllegalArgumentException if the parserAgent argument was <code>null</code>.
     */
    @Override
    public final void addAgent(final ParserAgent parserAgent) throws IllegalArgumentException {

        Validate.notNull(parserAgent, "Cannot handle null parserAgent.");

        if (!parseAgents.contains(parserAgent)) {
            parseAgents.add(parserAgent);
        }
    }

    /**
     * Replaces the tokens found within the data.
     *
     * @param data The data in which to replace existing tokens.
     * @return The token-substituted data.
     */
    @Override
    public final String substituteTokens(final String data) {

        StringBuilder toReturn = new StringBuilder();

        // Find all matches within data
        Matcher m = TOKEN_REGEXP.matcher(data);
        int currentStartIndex = 0;

        while (m.find(currentStartIndex)) {

            // Found a token hit within the data.
            int matchStartIndex = m.start();
            int matchEndIndex = m.end();
            String matchedToken = m.group();

            // Compensate for the extra escape backslashes.
            String strippedToken = matchedToken.substring(TOKEN_START.length() - 2,
                    matchedToken.length() - TOKEN_END.length() + 1);

            boolean handled = false;

            // Let all known parseAgents attempt to replace the token.
            for (ParserAgent currentParser : parseAgents) {

                boolean canHandle = currentParser.canHandle(strippedToken);
                if (canHandle) {
                    handled = true;

                    // Inject the data into the return buffer.
                    String replacement = currentParser.substituteValue(strippedToken);
                    toReturn.append(data.substring(currentStartIndex, matchStartIndex));
                    toReturn.append(replacement);

                    currentStartIndex = matchEndIndex;
                    break;
                }
            }

            if (!handled) {

                // Found no ParseAgents capable of handling the token substitution.
                // Re-inject the original token into the return buffer.
                toReturn.append(data.substring(currentStartIndex, matchEndIndex));
                currentStartIndex = matchEndIndex;
            }
        }

        // Append the rest of the data.
        toReturn.append(data.substring(currentStartIndex, data.length()));

        // All done.
        return toReturn.toString();
    }
}
