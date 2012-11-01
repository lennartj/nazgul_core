/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.resource.impl.resourcebundle.parser;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CompoundParserTest {

    @Test
    public void validateDefaultTokenParsersUsingNullFactory() throws Exception {

        // Assemble
        final InetAddress localhost = InetAddress.getLocalHost();
        final String canonicalName = localhost.getCanonicalHostName();
        final String address = localhost.getHostAddress();
        final String name = localhost.getHostName();
        final String userDir = System.getProperty("user.dir");

        final String text = "Substitute tokens ${sysprop:user.dir}, " +
                "${host:canonicalName}, ${host:address} and ${host:name}... and such.";

        final CompoundParser unitUnderTest = CompoundParser.create(null);

        // Act
        final String result = unitUnderTest.substituteTokens(text);

        // Assert
        Assert.assertEquals("Substitute tokens " + userDir + ", " +
                canonicalName + ", " + address + " and " + name + "... and such.", result);
    }
}
