/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedNullTest {

    @Test
    public void validateClassInformationAndUsage() {

        // Assemble
        final JaxbAnnotatedNull unitUnderTest = JaxbAnnotatedNull.getInstance();

        // Act
        final List<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(0, classInformation.size());
        Assert.assertEquals(new JaxbAnnotatedNull(), unitUnderTest);
        Assert.assertNotSame(new JaxbAnnotatedNull(), unitUnderTest);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final JaxbAnnotatedNull unitUnderTest = JaxbAnnotatedNull.getInstance();

        // Act
        final boolean result1 = unitUnderTest.compareTo(null) == 0;
        final boolean result2 = unitUnderTest.compareTo(new JaxbAnnotatedNull()) == 0;
        final boolean result3 = unitUnderTest.compareTo(new Date()) == 0;

        // Assert
        Assert.assertEquals(new JaxbAnnotatedNull().hashCode(), unitUnderTest.hashCode());
        Assert.assertTrue(result1);
        Assert.assertTrue(result2);
        Assert.assertFalse(result3);
    }
}
