/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.parser.api;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent;
import se.jguru.nazgul.core.parser.api.agent.ParserAgent;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultTokenParserTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullParserAgent() {

        // Assemble
        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();

        // Act & Assert
        unitUnderTest.addAgent(null);
    }

    @Test
    public void validateNotAddingParseAgentTwice() {

        // Assemble
        final DefaultParserAgent parserAgent = new DefaultParserAgent();
        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();

        // Act
        unitUnderTest.addAgent(parserAgent);
        unitUnderTest.addAgent(parserAgent);

        // Assert
        Assert.assertEquals(1, getRegisteredParserAgents(unitUnderTest).size());
        Assert.assertSame(parserAgent, getRegisteredParserAgents(unitUnderTest).get(0));
    }

    @Test
    public void validateNoSubstitutionsWhenNoParserAgentsAreRegistered() {

        // Assemble
        final String data = "Your JDK version is ${sysprop:java.version}, which is ${good}.";
        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(data, result);
    }

    @Test
    public void validateNormalParsing() {

        // Assemble
        final String data = "Your JDK version is ${sysprop:java.version}, which is ${good}.";
        final String expected = "Your JDK version is " + System.getProperty("java.version")
                + ", which is bad.";

        final DefaultParserAgent parserAgent = new DefaultParserAgent();
        parserAgent.addStaticReplacement("good", "bad");

        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(parserAgent);

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    //
    // Private helpers
    //

    private List<ParserAgent> getRegisteredParserAgents(final DefaultTokenParser parser) {

        final Field parseAgentsField;
        try {

            parseAgentsField = parser.getClass().getDeclaredField("parseAgents");
            parseAgentsField.setAccessible(true);
            return (List<ParserAgent>) parseAgentsField.get(parser);

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire parseAgents List.", e);
        }
    }
}
