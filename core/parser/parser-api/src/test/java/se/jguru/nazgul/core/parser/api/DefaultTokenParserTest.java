/*
 * #%L
 * Nazgul Project: nazgul-core-parser-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
        unitUnderTest.initialize(new DefaultTokenDefinitions());

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnMultipleInitializations() {

        // Assemble
        final DefaultParserAgent parserAgent = new DefaultParserAgent();
        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(parserAgent);

        final TokenDefinitions tokenDefinitions = new DefaultTokenDefinitions();

        // Act & Assert
        unitUnderTest.initialize(tokenDefinitions);
        unitUnderTest.initialize(tokenDefinitions);
    }

    @Test
    public void validateNormalParsingUsingDifferentTokenDefinitions() {

        // Assemble
        final String data = "Your JDK version is [sysprop:java.version], which is [good].";
        final String expected = "Your JDK version is " + System.getProperty("java.version")
                + ", which is bad.";

        final DefaultParserAgent parserAgent = new DefaultParserAgent();
        parserAgent.addStaticReplacement("good", "bad");

        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(parserAgent);
        unitUnderTest.initialize(new SingleBracketTokenDefinitions());

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
