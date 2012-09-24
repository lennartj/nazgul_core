/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.tree.helpers.Adjustment;
import se.jguru.nazgul.core.algorithms.api.tree.helpers.AdjustmentStringNode;
import se.jguru.nazgul.core.algorithms.api.tree.helpers.ProcessPath;
import se.jguru.nazgul.core.algorithms.api.tree.helpers.ProcessPathStringNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
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
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(Arrays.asList("Left"));

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
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(
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
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnNegativeIndex() {

        // Assemble
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(Arrays.asList("Left", "Center"));

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionAppendingSegmentToFullPath() {

        // Assemble
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(
                Arrays.asList("Left", "Center", "Right"));

        // Act & Assert
        unitUnderTest.append("FooBar");
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(Arrays.asList("Left", "Center"));

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
        final AdjustmentStringNode unitUnderTest1 = AdjustmentStringNode.create(Arrays.asList("Left", "Center"));
        final AdjustmentStringNode unitUnderTest2 = AdjustmentStringNode.create(
                Arrays.asList("Left", "Center", "Right"));
        final AdjustmentStringNode unitUnderTest3 = AdjustmentStringNode.create(Arrays.asList("Left"));
        final AdjustmentStringNode unitUnderTest4 = AdjustmentStringNode.create(Arrays.asList("Apa"));

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
        final EnumMap<ProcessPath, String> segments = TreeAlgorithms.getEmptyEnumMap(ProcessPath.class);
        segments.put(ProcessPath.NODE, "Something");

        // Act & Assert
        new ProcessPathStringNode(segments);
    }

    @Test
    public void validateToString() {

        // Assemble
        final String expected =
                "EnumMapPath { \n" +
                "  LEFT=Foo\n" +
                "  CENTER=Bar\n" +
                "  RIGHT=Baz }";
        final AdjustmentStringNode unitUnderTest = AdjustmentStringNode.create(Arrays.asList("Foo", "Bar", "Baz"));

        // Act
        final String debugString = unitUnderTest.toString();

        // Assert
        Assert.assertNotNull(debugString);
        Assert.assertEquals(expected, debugString);
    }
}
