/*
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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

import java.util.regex.Pattern;

/**
 * TokenDefinitions implementation, using brackets for token delimitation (i.e. tokens on
 * the form <code>[aToken]</code>).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SingleBracketTokenDefinitions implements TokenDefinitions {

    /**
     * Definition for the "Start of token" identifier.
     */
    public static final String TOKEN_START = "\\[";

    /**
     * Definition for the "End of token" identifier.
     */
    public static final String TOKEN_END = "\\]";

    /**
     * Regular expression definition of a token.
     */
    public static final Pattern TOKEN_REGEXP = Pattern.compile(TOKEN_START + "[^\\]]*" + TOKEN_END);

    /**
     * {@code}
     */
    @Override
    public Pattern getTokenRegExpPattern() {
        return TOKEN_REGEXP;
    }

    /**
     * {@code}
     */
    @Override
    public String getToken(final String tokenMatch) {
        Validate.notEmpty(tokenMatch, "Cannot handle null or empty tokenMatch argument.");

        // Compensate for the extra backslashes within the START and END expressions.
        return tokenMatch.substring(TOKEN_START.length() - 1, tokenMatch.length() - TOKEN_END.length() + 1);
    }
}
