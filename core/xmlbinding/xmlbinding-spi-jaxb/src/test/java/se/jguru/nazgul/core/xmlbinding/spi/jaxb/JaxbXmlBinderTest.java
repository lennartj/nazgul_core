/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbXmlBinderTest {

    // Shared state
    final JaxbXmlBinder unitUnderTest = new JaxbXmlBinder();

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamespacePrefixResolver() {

        // Act & Assert
        new JaxbXmlBinder(null);
    }

    @Test
    public void validateXmlConversion() {

        // Assemble
        final Person person1 = new Person("Lennart", 44);
        final Person person2 = new Person("Malin", 478);
        final String expected = readFully("data/xml/xmlConversion.xml");

        // Act
        final String result = unitUnderTest.convertToXml(person1, person2);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateXmlConversionWithTypeConversion() {

        // Assemble
        final Person person1 = new Person("Lennart", 44);
        final Person person2 = new Person("Malin", 478);
        final String expected = readFully("data/xml/conversionWithTypeConversion.xml");

        // Act
        final String result = unitUnderTest.convertToXml(person1, "FooBar!", null, person2);

        // Assert
        Assert.assertEquals(expected, result);
    }

    //
    // Private helpers
    //

    private String readFully(final String path) {

        final InputStream resource = getClass().getClassLoader().getResourceAsStream(path);

        final BufferedReader tmp = new BufferedReader(new InputStreamReader(resource));
        final StringBuilder toReturn = new StringBuilder(50);

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }
}
