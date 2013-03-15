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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
