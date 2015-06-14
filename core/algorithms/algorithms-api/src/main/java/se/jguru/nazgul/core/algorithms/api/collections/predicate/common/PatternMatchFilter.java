/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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
package se.jguru.nazgul.core.algorithms.api.collections.predicate.common;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

import java.util.regex.Pattern;

/**
 * Java regular expression pattern match filter.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see java.util.regex.Pattern
 */
public class PatternMatchFilter implements Filter<String> {

    // Internal state
    private Pattern regexpPattern;

    /**
     * Creates a new PatternMatchFilter using the provided regexp pattern to match candidates.
     *
     * @param pattern A non-empty pattern to be used for regexp pattern in a matcher.
     */
    public PatternMatchFilter(final String pattern) {

        // Check sanity
        Validate.notEmpty(pattern, "Cannot handle null or empty pattern argument.");

        regexpPattern = Pattern.compile(pattern);
    }

    /**
     * Tests whether or not the specified candidate should be
     * included in this filter's selection.
     *
     * @param candidate The candidate to be tested.
     * @return <code>true</code> if the <code>candidate</code>
     * should be included.
     */
    @Override
    public boolean accept(final String candidate) {
        return regexpPattern.matcher(candidate).matches();
    }
}
