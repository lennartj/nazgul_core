/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
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
package se.jguru.nazgul.core.resource.api;

import org.junit.Assert;
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
