/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedCollectionTest {

    // Shared state
    private String fooBar = "FooBar!";
    private DateTime firstAprilThreePm = new DateTime(2012, 4, 1, 15, 0);
    private int meaningOfLife = 42;

    private List<Object> data1D;
    private List<Object> data2D;
    private List<Object> data3D;

    @Before
    public void setupSharedState() {

        // Create the data structures
        data1D = new ArrayList<Object>();
        data2D = new ArrayList<Object>();
        data3D = new ArrayList<Object>();

        // data1D
        data1D.add(fooBar);
        data1D.add(firstAprilThreePm);
        data1D.add(meaningOfLife);

        // data2D
        data2D.add(fooBar);
        data2D.add(firstAprilThreePm);
        data2D.add(data1D);
        data2D.add(meaningOfLife);

        // data3d
        data3D.add(fooBar);
        data3D.add(data2D);
    }

    @Test
    public void validateClassInformationAndUsage() {

        // Act
        final JaxbAnnotatedCollection<List> unitUnderTest = new JaxbAnnotatedCollection<List>(data1D);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        final List result = unitUnderTest.getValue();
        Assert.assertEquals(data1D.size(), result.size());
        for(int i = 0; i < result.size(); i++) {
            Assert.assertEquals(data1D.get(i), result.get(i));
        }

        Assert.assertEquals(1, classInformation.size());
        Assert.assertTrue(classInformation.contains(String.class.getName()));
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final String value = "FooBar!";
        final JaxbAnnotatedString unitUnderTest = new JaxbAnnotatedString(value);

        // Act
        final boolean stringEqualsResult1 = unitUnderTest.equals("FooBar!");
        final boolean stringEqualsResult2 = unitUnderTest.equals("somethingElse");
        final boolean jaxbAnnotatedStringEqualsResult1 = unitUnderTest.equals(new JaxbAnnotatedString("FooBar!"));
        final boolean jaxbAnnotatedStringEqualsResult2 = unitUnderTest.equals(
                new JaxbAnnotatedString("somethingElse"));
        final boolean result1 = unitUnderTest.equals(null);
        final boolean result2 = unitUnderTest.equals(new Date());

        // Assert
        Assert.assertTrue(stringEqualsResult1);
        Assert.assertFalse(stringEqualsResult2);
        Assert.assertTrue(jaxbAnnotatedStringEqualsResult1);
        Assert.assertFalse(jaxbAnnotatedStringEqualsResult2);
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
        Assert.assertEquals(value.hashCode(), unitUnderTest.hashCode());
    }

    @Test(expected = ClassCastException.class)
    public void validateExceptionOnComparingWithNonStringType() {

        // Assemble
        final String value = "FooBar!";
        final JaxbAnnotatedString unitUnderTest = new JaxbAnnotatedString(value);

        // Act & Assert
        unitUnderTest.compareTo(new Date());
    }
}
