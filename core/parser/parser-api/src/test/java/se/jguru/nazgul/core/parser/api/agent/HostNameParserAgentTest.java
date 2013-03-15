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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
        final InetAddress localhost = HostNameParserAgent.getLocalhostNonLoopbackAddress();
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
