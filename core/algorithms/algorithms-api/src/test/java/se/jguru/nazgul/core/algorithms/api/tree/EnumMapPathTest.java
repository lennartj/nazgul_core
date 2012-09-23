/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.api.tree;

import junit.framework.Assert;
import org.junit.Test;

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

    enum Adjustment {
        LEFT,
        CENTER,
        RIGHT
    }

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
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

        // Act
        final int size = unitUnderTest.size();
        final int maxSize = unitUnderTest.getMaxSize();

        // Assert
        Assert.assertEquals(0, size);
        Assert.assertEquals(Adjustment.values().length, maxSize);
    }

    @Test
    public void validateSizeAndIteration() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        segments.put(Adjustment.LEFT, "Left");
        segments.put(Adjustment.CENTER, "Center");
        segments.put(Adjustment.RIGHT, "Right");

        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

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
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        segments.put(Adjustment.LEFT, "Left");
        segments.put(Adjustment.CENTER, "Center");

        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

        // Act & Assert
        unitUnderTest.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnNegativeIndex() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        segments.put(Adjustment.LEFT, "Left");
        segments.put(Adjustment.CENTER, "Center");

        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

        // Act & Assert
        unitUnderTest.get(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionAppendingSegmentToFullPath() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        segments.put(Adjustment.LEFT, "Left");
        segments.put(Adjustment.CENTER, "Center");
        segments.put(Adjustment.RIGHT, "Right");

        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

        // Act & Assert
        unitUnderTest.append("FooBar");
    }

    @Test
    public void validateAppendingSegmentToPath() {

        // Assemble
        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        segments.put(Adjustment.LEFT, "Left");
        segments.put(Adjustment.CENTER, "Center");

        final EnumMapPath<Adjustment, String> unitUnderTest =
                new EnumMapPath<Adjustment, String>(segments, Adjustment.class);

        // Act
        final EnumMapPath<Adjustment, String> three = unitUnderTest.append("Right");

        // Assert
        Assert.assertNotSame(unitUnderTest, three);
        Assert.assertEquals(2, unitUnderTest.size());
        Assert.assertEquals(3, three.size());

        Assert.assertEquals("Right", three.get(2));
    }

    @Test
    public void validateComparison() {

        // Assemble
        final EnumMapPath<Adjustment, String> unitUnderTest1 = getEMP(Arrays.asList("Left", "Center"));
        final EnumMapPath<Adjustment, String> unitUnderTest2 = getEMP(Arrays.asList("Left", "Center", "Right"));
        final EnumMapPath<Adjustment, String> unitUnderTest3 = getEMP(Arrays.asList("Left"));
        final EnumMapPath<Adjustment, String> unitUnderTest4 = getEMP(Arrays.asList("Apa"));

        // Act
        final SortedSet<EnumMapPath<Adjustment, String>> sortedSet = new TreeSet<EnumMapPath<Adjustment, String>>();
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

    //
    // Private helpers
    //

    private EnumMapPath<Adjustment, String> getEMP(final List<String> values) {

        final EnumMap<Adjustment, String> segments = new EnumMap<Adjustment, String>(Adjustment.class);
        Iterator<String> it = values.iterator();
        for (Adjustment current : segments.keySet()) {

            // Get the next value pair
            if (!it.hasNext()) {
                break;
            }

            final String nextValue = it.next();
            segments.put(current, nextValue);
        }

        // All Done.
        return new EnumMapPath<Adjustment, String>(segments, Adjustment.class);
    }
}
