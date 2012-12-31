/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api.collections.common;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.common.PatternMatchFilter;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternMatchFilterTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPattern() {

        // Act & Assert
        new PatternMatchFilter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyPattern() {

        // Act & Assert
        new PatternMatchFilter("");
    }

    @Test
    public void validatePatternMatching() {

        // Assemble
        final List<String> data = Arrays.asList("foo", "FooBar", "fooBar", "gnat", "fooBert");
        final String pattern = "foo.*";
        final PatternMatchFilter unitUnderTest = new PatternMatchFilter(pattern);

        // Act
        final List<String> result = CollectionAlgorithms.filter(data, unitUnderTest);

        // Assert
        Assert.assertEquals(3, result.size());
        for (String current : result) {
            Assert.assertTrue(data.contains(current));
        }
    }
}
