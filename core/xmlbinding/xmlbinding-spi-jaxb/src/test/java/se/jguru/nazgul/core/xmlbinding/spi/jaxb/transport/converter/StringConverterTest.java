/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedString;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringConverterTest {

    @Test
    public void validatePackageForTransport() {

        // Assemble
        final StringConverter unitUnderTest = new StringConverter();
        final String fooBar = "FooBar!";

        // Act
        final boolean result1 = unitUnderTest.canPackageForTransport(null);
        final boolean result2 = unitUnderTest.canPackageForTransport(fooBar);
        final boolean result3 = unitUnderTest.canPackageForTransport(new JaxbAnnotatedString(fooBar));

        final JaxbAnnotatedString transportForm1 = unitUnderTest.packageForTransport(fooBar);
        final JaxbAnnotatedString transportForm2 = unitUnderTest.packageForTransport(fooBar);

        // Assert
        Assert.assertFalse(result1);
        Assert.assertTrue(result2);
        Assert.assertFalse(result3);
        Assert.assertNotNull(transportForm1);
        Assert.assertNotNull(transportForm2);
        Assert.assertEquals(transportForm1, transportForm2);
    }

    @Test
    public void validateReviveAfterTransport() {

        // Assemble
        final StringConverter unitUnderTest = new StringConverter();
        final String fooBar = "FooBar!";

        // Act
        final boolean result1 = unitUnderTest.canReviveAfterTransport(null);
        final boolean result2 = unitUnderTest.canReviveAfterTransport(fooBar);
        final boolean result3 = unitUnderTest.canReviveAfterTransport(new JaxbAnnotatedString(fooBar));

        final String revived = unitUnderTest.reviveAfterTransport(new JaxbAnnotatedString(fooBar));

        // Assert
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
        Assert.assertTrue(result3);
        Assert.assertEquals(fooBar, revived);
    }
}
