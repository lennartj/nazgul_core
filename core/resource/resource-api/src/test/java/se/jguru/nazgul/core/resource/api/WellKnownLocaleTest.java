/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.api;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class WellKnownLocaleTest {

    // Shared state
    private Transformer<WellKnownLocale, Tuple<WellKnownLocale, Locale>> localeTransformer
            = new Transformer<WellKnownLocale, Tuple<WellKnownLocale, Locale>>() {
        @Override
        public Tuple<WellKnownLocale, Locale> transform(final WellKnownLocale input) {
            return new Tuple<WellKnownLocale, Locale>(input, input.getLocale());
        }
    };
    private Map<WellKnownLocale, Locale> localeMap;

    @Before
    public void setupSharedState() {
        localeMap = CollectionAlgorithms.map(Arrays.asList(WellKnownLocale.values()), localeTransformer);
    }

    @Test
    public void validateCorrectLocales() {

        // Act & Assert
        for (WellKnownLocale current : WellKnownLocale.values()) {
            Assert.assertEquals(localeMap.get(current), current.getLocale());
        }
    }
}
