/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NullConverterTest {

    @Test
    public void validatePackageForTransport() {

        // Assemble
        final NullConverter unitUnderTest = new NullConverter();

        // Act
        final boolean result1 = unitUnderTest.canPackageForTransport(null);
        final boolean result2 = unitUnderTest.canPackageForTransport("FooBar!");
        final boolean result3 = unitUnderTest.canPackageForTransport(JaxbAnnotatedNull.getInstance());

        final JaxbAnnotatedNull transportForm1 = unitUnderTest.packageForTransport(null);
        final JaxbAnnotatedNull transportForm2 = unitUnderTest.packageForTransport(null);

        // Assert
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
        Assert.assertFalse(result3);
        Assert.assertNotNull(transportForm1);
        Assert.assertNotNull(transportForm2);
        Assert.assertSame(transportForm1, transportForm2);
    }

    @Test
    public void validateReviveAfterTransport() {

        // Assemble
        final NullConverter unitUnderTest = new NullConverter();

        // Act
        final boolean result1 = unitUnderTest.canReviveAfterTransport(null);
        final boolean result2 = unitUnderTest.canReviveAfterTransport("FooBar!");
        final boolean result3 = unitUnderTest.canReviveAfterTransport(JaxbAnnotatedNull.getInstance());

        final Object revived = unitUnderTest.reviveAfterTransport(JaxbAnnotatedNull.getInstance());

        // Assert
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
        Assert.assertTrue(result3);
        Assert.assertNull(revived);
    }
}
