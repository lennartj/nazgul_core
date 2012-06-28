/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.parser.api.agent;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;

import java.net.InetAddress;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HostNameParserAgentTest {
    
    @Test
    public void validateHostNameParserAgentSubtitutions() throws Exception {
        
        // Assemble
        final InetAddress localhost = InetAddress.getLocalHost();
        final String canonicalName = localhost.getCanonicalHostName();
        final String address = localhost.getHostAddress();
        final String name = localhost.getHostName();

        final String text = "Substitute tokens ${sysprop:user.dir}, " +
                "${host:canonicalName}, ${host:address} and ${host:name}... and such.";
        final DefaultTokenParser parser = new DefaultTokenParser();
        final HostNameParserAgent unitUnderTest = new HostNameParserAgent();
        parser.addAgent(unitUnderTest);

        // Act
        final String result = parser.substituteTokens(text);

        // Assert
        Assert.assertEquals("Substitute tokens ${sysprop:user.dir}, " +
                canonicalName + ", " + address + " and " + name + "... and such.", result);
    }
}
