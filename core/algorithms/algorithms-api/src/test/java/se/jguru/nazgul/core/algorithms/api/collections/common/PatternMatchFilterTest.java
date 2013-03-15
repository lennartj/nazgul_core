/*
 * #%L
 * Nazgul Project: se.jguru.nazgul.core.algorithms.api.nazgul-core-algorithms-api
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
