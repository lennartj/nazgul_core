/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedStringTest {

    @Test
    public void validateClassInformationAndUsage() {

        // Assemble
        final String value = "FooBar!";

        // Act
        final JaxbAnnotatedString unitUnderTest = new JaxbAnnotatedString(value);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(value, unitUnderTest.getValue());
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
