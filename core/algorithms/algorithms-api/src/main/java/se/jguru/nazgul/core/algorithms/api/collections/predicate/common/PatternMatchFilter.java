/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
     *         should be included.
     */
    @Override
    public boolean accept(final String candidate) {
        return regexpPattern.matcher(candidate).matches();
    }
}
