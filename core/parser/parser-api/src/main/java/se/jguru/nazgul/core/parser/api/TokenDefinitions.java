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

import java.util.regex.Pattern;

/**
 * Specification for how to identify tokens (within text), as well as how to get the actual name
 * from the token when a Pattern match is found.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TokenDefinitions {

    /**
     * Retrieves a Regular expression which can be used by a TokenParser to identify tokens within text.
     *
     * @return a Regular expression which can be used by a TokenParser to identify tokens within text.
     * Will never return a {@code null} Pattern.
     */
    Pattern getTokenRegExpPattern();

    /**
     * Retrieves the actual token name from a match from the {@code getTokenRegExpPattern()} Pattern,
     * i.e. with any token start and token end sequences peeled off. For example, if a Token should be
     * recognized using {@code ${aToken}}, then this method would return {@code aToken} (i.e. without
     * any start or end delimiter chars).
     *
     * @param tokenMatch The matched string from the {@code getTokenRegExpPattern()} Pattern.
     * @return The name of the token embedded in the tokenMatch argument String.
     */
    String getToken(String tokenMatch);
}
