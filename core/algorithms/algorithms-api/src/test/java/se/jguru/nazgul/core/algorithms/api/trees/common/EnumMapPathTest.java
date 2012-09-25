/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.trees.common;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.trees.TreeAlgorithms;
import se.jguru.nazgul.core.algorithms.api.trees.common.helpers.Adjustment;
import se.jguru.nazgul.core.algorithms.api.trees.common.helpers.AdjustmentPath;
import se.jguru.nazgul.core.algorithms.api.trees.common.helpers.ProcessPathSegments;
import se.jguru.nazgul.core.algorithms.api.trees.common.helpers.ProcessStringPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPathTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSegments() {

        // Act & Assert
        new EnumMapPath<Adjustment, String>(null, Adjustment.class);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullEnumType() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);

        // Act & Assert
        new EnumMapPath<Adjustment, String>(segments, null);
    }

    @Test
    public void validateKeyPadding() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left"));

        // Act
        final int size = unitUnderTest.size();
        final int maxSize = unitUnderTest.getMaxSize();

        // Assert
        Assert.assertEquals(1, size);
        Assert.assertEquals(Adjustment.values().length, maxSize);
    }

    @Test
    public void validateSizeAndIteration() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));

        // Act
        final List<String> iterated = new ArrayList<String>();
        for (String aResult : unitUnderTest) {
            iterated.add(aResult);
        }

        // Assert
        Assert.assertEquals(3, unitUnderTest.size());
        Assert.assertEquals(Arrays.asList("Left", "Center", "Right"), iterated);
        Assert.assertEquals("Left", unitUnderTest.get(0));
        Assert.assertEquals("Center", unitUnderTest.get(1));
        Assert.assertEquals("Right", unitUnderTest.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnTooBigIndex() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnNegativeIndex() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionAppendingSegmentToFullPath() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));

        // Act & Assert
        unitUnderTest.append("FooBar");
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Left", "Center"));

        // Act
        final EnumMapPath<Adjustment, String> three = unitUnderTest.append("Right");

        // Assert
        Assert.assertNotSame(unitUnderTest, three);
        Assert.assertEquals(2, unitUnderTest.size());
        Assert.assertEquals(3, three.size());

        Assert.assertEquals("Left", unitUnderTest.get(Adjustment.LEFT));
        Assert.assertEquals("Center", unitUnderTest.get(Adjustment.CENTER));
        Assert.assertEquals(null, unitUnderTest.get(Adjustment.RIGHT));

        Assert.assertEquals("Left", three.get(Adjustment.LEFT));
        Assert.assertEquals("Center", three.get(Adjustment.CENTER));
        Assert.assertEquals("Right", three.get(Adjustment.RIGHT));

        Assert.assertEquals("Right", three.get(2));
    }

    @Test
    public void validateComparison() {

        // Assemble
        final AdjustmentPath unitUnderTest1 = AdjustmentPath.create(Arrays.asList("Left", "Center"));
        final AdjustmentPath unitUnderTest2 = AdjustmentPath.create(
                Arrays.asList("Left", "Center", "Right"));
        final AdjustmentPath unitUnderTest3 = AdjustmentPath.create(Arrays.asList("Left"));
        final AdjustmentPath unitUnderTest4 = AdjustmentPath.create(Arrays.asList("Apa"));

        // Act
        final SortedSet<EnumMapPath<Adjustment, String>> sortedSet = new TreeSet<EnumMapPath<Adjustment, String>>();
        sortedSet.add(unitUnderTest1);
        sortedSet.add(unitUnderTest2);
        sortedSet.add(unitUnderTest3);
        sortedSet.add(unitUnderTest4);

        final List<EnumMapPath<Adjustment, String>> sortedList = new ArrayList<EnumMapPath<Adjustment, String>>();
        for(EnumMapPath<Adjustment, String> current : sortedSet) {
            sortedList.add(current);
        }

        // Assert
        Assert.assertEquals("Left".compareTo("Apa"), unitUnderTest3.compareTo(unitUnderTest4));
        Assert.assertSame(unitUnderTest4, sortedList.get(0));
        Assert.assertSame(unitUnderTest3, sortedList.get(1));
        Assert.assertSame(unitUnderTest1, sortedList.get(2));
        Assert.assertSame(unitUnderTest2, sortedList.get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNullsWithinPath() {

        // Assemble
        final EnumMap<ProcessPathSegments, String> segments = TreeAlgorithms.getEmptyEnumMap(ProcessPathSegments.class);
        segments.put(ProcessPathSegments.NODE, "Something");

        // Act & Assert
        new ProcessStringPath(segments);
    }

    @Test
    public void validateToString() {

        // Assemble
        final String expected =
                "EnumMapPath { \n" +
                        "  LEFT=Foo\n" +
                        "  CENTER=Bar\n" +
                        "  RIGHT=Baz }";
        final AdjustmentPath unitUnderTest = AdjustmentPath.create(Arrays.asList("Foo", "Bar", "Baz"));

        // Act
        final String debugString = unitUnderTest.toString();

        // Assert
        Assert.assertNotNull(debugString);
        Assert.assertEquals(expected, debugString);
    }
}
