/*-
 * #%L
 * Nazgul Project: nazgul-core-cache-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.cache.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ReadOnlyIteratorTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullIterator() {

        // Act & Assert
        new ReadOnlyIterator<String>(null);
    }

    @Test
    public void validateIteratorOrdering() {

        // Assemble
        final List<String> stringList = Arrays.asList("foo", "bar", "baz");
        final ReadOnlyIterator<String> unitUnderTest = new ReadOnlyIterator<String>(stringList.iterator());
        final List<String> result = new ArrayList<String>();

        // Act
        while (unitUnderTest.hasNext()) {
            result.add(unitUnderTest.next());
        }

        // Assert
        Assert.assertEquals(stringList, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnRemove() {

        // Assemble
        final List<String> stringList = Arrays.asList("foo", "bar", "baz");
        final ReadOnlyIterator<String> unitUnderTest = new ReadOnlyIterator<String>(stringList.iterator());

        // Act & Assert
        unitUnderTest.next();
        unitUnderTest.remove();
    }
}

