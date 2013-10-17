/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
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
        final String compoundPath = null;

        // Act & Assert
        new StringPath(compoundPath);
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
        final String compoundPath = "one/two/three";

        // Act
        final StringPath result = new StringPath(compoundPath);
        final List<String> iterated = new ArrayList<String>();
        for (String aResult : result) {
            iterated.add(aResult);
        }

        // Assert
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Arrays.asList("one", "two", "three"), iterated);
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
        final StringPath unitUnderTest = new StringPath("one/two/three");

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test
    public void validateComparison() {

        // Assemble
        final String stdSeparator = AbstractPath.DEFAULT_SEGMENT_SEPARATOR;
        final String compoundPath1 = "one/two/three";
        final String compoundPath2 = "one/two/four";
        final String compoundPath3 = "one/two";
        final String compoundPath4 = "one/two/three/four";
        final String compoundPath5 = "one#two#three#four";

        final AbstractPath<String> path1 = new StringPath(compoundPath1);
        final AbstractPath<String> path2 = new StringPath(compoundPath2);
        final AbstractPath<String> path3 = new StringPath(compoundPath3, stdSeparator);
        final AbstractPath<String> path4 = new StringPath(compoundPath4);
        final AbstractPath<String> path5 = new StringPath(compoundPath5, "#");
        final List<AbstractPath<String>> paths = Arrays.asList(path1, path2, path3, path4);

        final SortedSet<AbstractPath<String>> sortedSet = new TreeSet<AbstractPath<String>>(paths);

        // Act
        final List<AbstractPath<String>> sortedList = new ArrayList<AbstractPath<String>>();
        for (AbstractPath<String> current : sortedSet) {
            sortedList.add(current);
        }

        final int cmp = path1.compareTo(path2);

        // Assert
        Assert.assertEquals("three".compareTo("four"), cmp);
        Assert.assertEquals(0, path4.compareTo(path5));
        Assert.assertSame(path3, sortedList.get(0));
        Assert.assertSame(path2, sortedList.get(1));
        Assert.assertSame(path1, sortedList.get(2));
        Assert.assertSame(path4, sortedList.get(3));
        Assert.assertEquals(path4.getSegments(), path5.getSegments());
    }

    @Test
    public void validateEquality() {

        // Assemble
        final AbstractPath<String> path1 = new StringPath("one/two/three");
        final AbstractPath<String> path3 = new StringPath("one/two");
        final AbstractPath<String> path4 = new StringPath("one/two/three");

        // Act & Assert
        Assert.assertTrue(path1.equals(path1));
        Assert.assertFalse(path1.equals(null));
        Assert.assertTrue(path1.equals(path4));
        Assert.assertFalse(path1.equals(path3));
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final AbstractPath<String> unitUnderTest = new StringPath("one");

        // Act
        final AbstractPath<String> two = unitUnderTest.append("two");

        // Assert
        Assert.assertNotSame(unitUnderTest, two);
        Assert.assertEquals(1, unitUnderTest.size());
        Assert.assertEquals(2, two.size());

        Assert.assertEquals("two", two.get(1));
    }

    @Test
    public void validateStringConversion() {

        // Assemble
        final String expected = "one/two/three";
        final AbstractPath<String> unitUnderTest = new StringPath(expected);

        // Act
        final String result = unitUnderTest.toString();

        // Assert
        Assert.assertEquals(expected, result);
    }
}
