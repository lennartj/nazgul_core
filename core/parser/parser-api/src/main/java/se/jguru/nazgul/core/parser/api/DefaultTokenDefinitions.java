/*-
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import se.jguru.nazgul.core.algorithms.api.Validate;

import java.util.regex.Pattern;

/**
 * Default implementation of the TokenDefinitions specification, using tokens on the form <code>${aToken}</code>.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTokenDefinitions implements TokenDefinitions {

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
     * {@inheritDoc}
     */
    @Override
    public String getToken(final String tokenMatch) {

        Validate.notEmpty(tokenMatch, "tokenMatch");

        // Compensate for the extra backslashes within the START and END expressions.
        return tokenMatch.substring(TOKEN_START.length() - 2, tokenMatch.length() - TOKEN_END.length() + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pattern getTokenRegExpPattern() {
        return TOKEN_REGEXP;
    }
}
