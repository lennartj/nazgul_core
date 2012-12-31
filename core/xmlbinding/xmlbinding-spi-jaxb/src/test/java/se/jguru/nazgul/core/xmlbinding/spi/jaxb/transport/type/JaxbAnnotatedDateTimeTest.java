/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.junit.Assert;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedDateTimeTest {

    // Shared state
    private DateTime value;

    @Before
    public void setupSharedState() {

        value = new DateTime(2012, 7, 6, 5, 4, 3);
    }

    @Test
    public void validateClassInformationAndUsage() {

        // Assemble

        // Act
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(value, unitUnderTest.getValue());
        Assert.assertEquals(1, classInformation.size());

        Assert.assertTrue(classInformation.contains(DateTime.class.getName()));
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final DateTime valueCopy = new DateTime(2012, 7, 6, 5, 4, 3);
        final DateTime anotherValue = new DateTime(2012, 8, 7, 6, 5, 4);
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);

        // Act & Assert
        Assert.assertTrue(unitUnderTest.equals(valueCopy));
        Assert.assertFalse(unitUnderTest.equals(anotherValue));

        Assert.assertEquals(new JaxbAnnotatedDateTime(valueCopy), unitUnderTest);
        Assert.assertFalse(unitUnderTest.equals(new JaxbAnnotatedDateTime(anotherValue)));

        Assert.assertFalse(unitUnderTest.equals(null));
        Assert.assertFalse(unitUnderTest.equals(new Date()));

        Assert.assertEquals(value.hashCode(), unitUnderTest.hashCode());
    }

    @Test(expected = ClassCastException.class)
    public void validateExceptionOnComparingWithNonStringType() {

        // Assemble
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);

        // Act & Assert
        unitUnderTest.compareTo(new Date());
    }
}
