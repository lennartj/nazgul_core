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

package se.jguru.nazgul.core.parser.api.agent;

/**
 * Specification for a parser (agent) which handles
 * token substitution with static and/or dynamic tokens.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ParserAgent {

    /**
     * Adds a static replacement, i.e. the token/key to be
     * replaced by the corresponding value.
     *
     * @param key   The key/token to be replaced.
     * @param value The value to use as replacement.
     */
    void addStaticReplacement(String key, String value);

    /**
     * Return true if this ParserAgent can handle the provided token, and false if not.
     *
     * @param token The token to investigate.
     * @return true if this ParserAgent can handle the provided token, and false if not.
     */
    boolean canHandle(String token);

    /**
     * Substitutes the provided token with its corresponding value.
     *
     * @param token The token to replace.
     * @return The replacement value.
     * @throws IllegalArgumentException if the token could not be handled by this ParserAgent.
     */
    String substituteValue(String token) throws IllegalArgumentException;
}
