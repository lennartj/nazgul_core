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
            for (Map.Entry<String, String> current : staticTokens.entrySet()) {
                addStaticReplacement(current.getKey(), current.getValue());
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
