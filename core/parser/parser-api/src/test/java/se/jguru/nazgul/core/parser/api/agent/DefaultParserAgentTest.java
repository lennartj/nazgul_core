/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.parser.api.agent;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultParserAgentTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnknownTokenReplacement() {

        // Assemble
        final String data = "Your JDK version is nice, which is ${blah:unknown}.";
        final DefaultParserAgent unitUnderTest = new DefaultParserAgent();

        // Act & Assert
        unitUnderTest.performDynamicReplacement(data);
    }

    @Test
    public void validateStaticTokenReplacement() {

        // Assemble
        final String data = "Your JDK version is nice, which is ${good}.";
        final String expected = "Your JDK version is nice, which is bad.";

        final Map<String, String> staticTokenReplacements = new TreeMap<String, String>();
        staticTokenReplacements.put("good", "bad");
        final DefaultParserAgent parserAgent = new DefaultParserAgent(staticTokenReplacements);

        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(parserAgent);

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateDynamicEnvironmentPropertyTokenReplacement() {

        // Assemble
        String key = null;
        String value = null;
        for (String current : new TreeSet<String>(System.getenv().keySet())) {

            key = current;
            value = System.getenv(current);

            if (key != null && !key.equals("") && value != null && !value.equals("")) {
                break;
            }
        }

        final String data = "Your JDK version is ${env:" + key + "}, which is good.";
        final String expected = "Your JDK version is " + value + ", which is good.";

        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(new DefaultParserAgent());

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateDynamicSystemPropertyTokenReplacement() {

        // Assemble
        final String data = "Your JDK version is ${sysprop:java.version}, which is good.";
        final String expected = "Your JDK version is " + System.getProperty("java.version")
                + ", which is good.";

        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(new DefaultParserAgent());

        // Act
        final String result = unitUnderTest.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateDefaultParserAgent() {

        // Assemble
        final String text = "This is a text where we have one token ${sysprop:user.dir}. :)";
        final DefaultTokenParser unitUnderTest = new DefaultTokenParser();
        unitUnderTest.addAgent(new DefaultParserAgent());

        // Act
        final String result = unitUnderTest.substituteTokens(text);

        // Assert
        Assert.assertEquals("This is a text where we have one " +
                "token " + System.getProperty("user.dir") + ". :)", result);
    }
}
