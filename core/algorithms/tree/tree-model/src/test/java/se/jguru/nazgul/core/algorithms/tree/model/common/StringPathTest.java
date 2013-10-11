/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
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

package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringPathTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSegments() {

        // Assemble
        final List<String> segments = null;

        // Act & Assert
        new StringPath(segments);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSegment() {

        // Assemble
        final String segment = null;

        // Act & Assert
        new StringPath(segment);
    }

    @Test
    public void validateSizeAndIteration() {

        // Assemble
        final List<String> segments = Arrays.asList("one", "two", "three");

        // Act
        final StringPath result = new StringPath(segments);
        final List<String> iterated = new ArrayList<String>();
        for (String aResult : result) {
            iterated.add(aResult);
        }

        // Assert
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(segments, iterated);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnTooBigIndex() {

        // Assemble
        final StringPath unitUnderTest = new StringPath("one");

        // Act & Assert
        unitUnderTest.get(45);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnNegativeIndex() {

        // Assemble
        final StringPath unitUnderTest = new StringPath(Arrays.asList("one", "two", "three"));

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test
    public void validateComparison() {

        // Assemble
        final List<String> segments1 = Arrays.asList("one", "two", "three");
        final List<String> segments2 = Arrays.asList("one", "two", "four");
        final List<String> segments3 = Arrays.asList("one", "two");
        final List<String> segments4 = Arrays.asList("one", "two", "three", "four");

        final AbstractListPath<String> path1 = new StringPath(segments1);
        final AbstractListPath<String> path2 = new StringPath(segments2);
        final AbstractListPath<String> path3 = new StringPath(segments3);
        final AbstractListPath<String> path4 = new StringPath(segments4);
        final List<AbstractListPath<String>> paths = Arrays.asList(path1, path2, path3, path4);

        final SortedSet<AbstractListPath<String>> sortedSet = new TreeSet<AbstractListPath<String>>(paths);

        // Act
        final List<AbstractListPath<String>> sortedList = new ArrayList<AbstractListPath<String>>();
        for (AbstractListPath<String> current : sortedSet) {
            sortedList.add(current);
        }

        final int cmp = path1.compareTo(path2);

        // Assert
        Assert.assertEquals("three".compareTo("four"), cmp);
        Assert.assertSame(path3, sortedList.get(0));
        Assert.assertSame(path2, sortedList.get(1));
        Assert.assertSame(path1, sortedList.get(2));
        Assert.assertSame(path4, sortedList.get(3));
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final AbstractListPath<String> unitUnderTest = new StringPath("one");

        // Act
        final AbstractListPath<String> two = unitUnderTest.append("two");

        // Assert
        Assert.assertNotSame(unitUnderTest, two);
        Assert.assertEquals(1, unitUnderTest.size());
        Assert.assertEquals(2, two.size());

        Assert.assertEquals("two", two.get(1));
    }

    @Test
    public void validateStringConversion() {

        // Assemble
        final AbstractListPath<String> unitUnderTest = new StringPath(Arrays.asList("one", "two", "three"));
        final String expected = "{ one/two/three }";

        // Act
        final String result = unitUnderTest.toString();

        // Assert
        Assert.assertEquals(expected, result);
    }
}
