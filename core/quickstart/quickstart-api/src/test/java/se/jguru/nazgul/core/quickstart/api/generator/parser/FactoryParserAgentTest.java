/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.SingleBracketTokenDefinitions;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FactoryParserAgentTest {

    // Shared state
    private SimpleArtifact reactorParent;
    private SimpleArtifact parentParent;
    private Project project;
    private DefaultTokenParser tokenParser;

    @Before
    public void setupSharedState() {

        reactorParent = new SimpleArtifact(
                "se.jguru.nazgul.tools.poms.external",
                "nazgul-tools-external-reactor-parent",
                "4.0.0");
        parentParent = new SimpleArtifact(
                "se.jguru.nazgul.core.poms.core-parent",
                "nazgul-core-parent",
                "1.6.1");
        project = new Project("nazgul", "blah", reactorParent, parentParent);

        tokenParser = new DefaultTokenParser();
    }

    @Test
    public void validateDynamicSystemPropertyTokenReplacement() {

        // Assemble
        final String data = "Your project is ${project:name}, with reactorParent version " +
                "${project:reactorParent.mavenVersion}.";
        final String expected = "Your project is " + project.getName() + ", with reactorParent version " +
                reactorParent.getMavenVersion() + ".";
        tokenParser.addAgent(new FactoryParserAgent(project));

        // Act
        final String result = tokenParser.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnUnknownToken() {

        // Assemble
        final String data = "Not a known property [project:foobar]";
        tokenParser.addAgent(new FactoryParserAgent(project));
        tokenParser.initialize(new SingleBracketTokenDefinitions());

        // Act
        final String result = tokenParser.substituteTokens(data);

        // Assert
        Assert.assertEquals(data, result);
    }

    @Test
    public void validateTokenReplacementUsingSingleBracket() {

        // Assemble
        final String data =
                "<parent>\n" +
                        "<groupId>[project:reactorParent.groupId]</groupId>\n" +
                        "<artifactId>[project:reactorParent.artifactId]</artifactId>\n" +
                        "<version>[project:name]</version>\n" +
                        "</parent>";

        final String expected =
                "<parent>\n" +
                        "<groupId>" + project.getReactorParent().getGroupId() + "</groupId>\n" +
                        "<artifactId>" + project.getReactorParent().getArtifactId() + "</artifactId>\n" +
                        "<version>" + project.getName() + "</version>\n" +
                        "</parent>";
        tokenParser.addAgent(new FactoryParserAgent(project));
        tokenParser.initialize(new SingleBracketTokenDefinitions());

        // Act
        final String result = tokenParser.substituteTokens(data);

        // Assert
        Assert.assertEquals(expected, result);
    }
}
