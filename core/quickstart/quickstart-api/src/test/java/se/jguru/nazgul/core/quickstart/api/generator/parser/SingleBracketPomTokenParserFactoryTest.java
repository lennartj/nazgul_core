/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.api.generator.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.parser.api.agent.AbstractParserAgent;
import se.jguru.nazgul.core.parser.api.agent.ParserAgent;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SingleBracketPomTokenParserFactoryTest {

    // Shared state
    private SimpleArtifact reactorParent;
    private SimpleArtifact parentParent;
    private Project acmeFooProject;

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
        acmeFooProject = new Project("foobar", "foo", reactorParent, parentParent);
    }

    @Test
    public void validateStandardBuilderStepsForComponentApiProject() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.COMPONENT_API, acmeFooProject)
                .withoutProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath("services/finance")
                .withProjectGroupIdPrefix("org.acme")
                .withoutProjectSuffix()
                .withMavenVersions("1.0.0-SNAPSHOT", "2.3.4-SNAPSHOT")
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        Assert.assertEquals("COMPONENT_API", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("component-api",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Check path and packaging
        Assert.assertEquals("org.acme",
                staticTokens.get(SingleBracketPomTokenParserFactory.PrefixEnricher.REVERSE_DNS_OF_ORGANISATION));
        Assert.assertEquals("services/finance/finance-api", staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("services.finance.api", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertEquals("../../../poms/foo-api-parent",
                staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));

        // Check local GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.services.finance.api", staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-finance-api", staticTokens.get(PomToken.ARTIFACTID.getToken()));

        // Check parent GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.poms.foobar-foo-api-parent",
                staticTokens.get(PomToken.PARENT_GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-api-parent", staticTokens.get(PomToken.PARENT_ARTIFACTID.getToken()));
        Assert.assertEquals("2.3.4-SNAPSHOT", staticTokens.get(PomToken.PARENT_VERSION.getToken()));
    }

    @Test
     public void validateStandardBuilderStepsForComponentSpiProject() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.COMPONENT_SPI, acmeFooProject)
                .withoutProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath("services/finance")
                .withProjectGroupIdPrefix("org.acme")
                .withProjectSuffix("pojo")
                .withMavenVersions("1.2.3-SNAPSHOT", "4.5.6-SNAPSHOT")
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        Assert.assertEquals("COMPONENT_SPI", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("component-spi",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Check path and packaging
        Assert.assertEquals("org.acme",
                staticTokens.get(SingleBracketPomTokenParserFactory.PrefixEnricher.REVERSE_DNS_OF_ORGANISATION));
        Assert.assertEquals("services/finance/finance-spi-pojo",
                staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("services.finance.spi.pojo", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertEquals("../../../poms/foo-api-parent",
                staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));

        // Check local GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.services.finance.spi.pojo",
                staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-finance-spi-pojo", staticTokens.get(PomToken.ARTIFACTID.getToken()));

        // Check parent GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.poms.foobar-foo-api-parent",
                staticTokens.get(PomToken.PARENT_GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-api-parent", staticTokens.get(PomToken.PARENT_ARTIFACTID.getToken()));
        Assert.assertEquals("4.5.6-SNAPSHOT", staticTokens.get(PomToken.PARENT_VERSION.getToken()));
    }

    @Test
    public void validateStandardBuilderStepsForApiParentProject() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.API_PARENT, acmeFooProject)
                .useProjectNameAsDirectoryPrefix()
                .isParentProject()
                .withProjectGroupIdPrefix("org.acme")
                .withoutProjectSuffix()
                .withMavenVersions("1.2.3-SNAPSHOT", "4.5.6-SNAPSHOT")
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        Assert.assertEquals("API_PARENT", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("api-parent",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Check path and packaging
        Assert.assertEquals("org.acme",
                staticTokens.get(SingleBracketPomTokenParserFactory.PrefixEnricher.REVERSE_DNS_OF_ORGANISATION));
        Assert.assertEquals("poms/foobar-foo-api-parent",
                staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("poms.foobar-foo-api-parent", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertEquals("../foo-parent", staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));

        // Check local GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.poms.foobar-foo-api-parent",
                staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-api-parent", staticTokens.get(PomToken.ARTIFACTID.getToken()));

        // Check parent GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.poms.foobar-foo-parent",
                staticTokens.get(PomToken.PARENT_GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-parent", staticTokens.get(PomToken.PARENT_ARTIFACTID.getToken()));
        Assert.assertEquals("4.5.6-SNAPSHOT", staticTokens.get(PomToken.PARENT_VERSION.getToken()));
    }

    @Test
    public void validateStandardBuilderStepsForPomsReactorProject() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.REACTOR, acmeFooProject)
                .useProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath("poms")
                .withProjectGroupIdPrefix("org.acme")
                .withoutProjectSuffix()
                .withMavenVersions("1.2.3-SNAPSHOT", "4.5.6-SNAPSHOT")
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        Assert.assertEquals("REACTOR", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("reactor",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Check path and packaging
        Assert.assertEquals("org.acme",
                staticTokens.get(SingleBracketPomTokenParserFactory.PrefixEnricher.REVERSE_DNS_OF_ORGANISATION));
        Assert.assertEquals("poms", staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("poms", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertEquals("", staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));

        // Check local GAV tokens
        Assert.assertEquals("org.acme.foobar.foo.poms", staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-poms-reactor", staticTokens.get(PomToken.ARTIFACTID.getToken()));

        // Check parent GAV tokens
        Assert.assertEquals("org.acme.foobar.foo", staticTokens.get(PomToken.PARENT_GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-reactor", staticTokens.get(PomToken.PARENT_ARTIFACTID.getToken()));
        Assert.assertEquals("1.2.3-SNAPSHOT", staticTokens.get(PomToken.PARENT_VERSION.getToken()));
    }

    @Test
    public void validateStandardBuilderStepsForRootReactorProject() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.ROOT_REACTOR, acmeFooProject)
                .useProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath("")
                .withProjectGroupIdPrefix("org.acme")
                .withoutProjectSuffix()
                .withMavenVersions("1.2.3-SNAPSHOT", "4.5.6-SNAPSHOT")
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        Assert.assertEquals("ROOT_REACTOR", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("root-reactor",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Check path and packaging
        Assert.assertEquals("org.acme",
                staticTokens.get(SingleBracketPomTokenParserFactory.PrefixEnricher.REVERSE_DNS_OF_ORGANISATION));
        Assert.assertEquals("", staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertNull(staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));

        // Check local GAV tokens
        Assert.assertEquals("org.acme.foobar.foo", staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("foobar-foo-reactor", staticTokens.get(PomToken.ARTIFACTID.getToken()));

        // Check parent GAV tokens
        Assert.assertEquals(acmeFooProject.getReactorParent().getGroupId(),
                staticTokens.get(PomToken.PARENT_GROUPID.getToken()));
        Assert.assertEquals(acmeFooProject.getReactorParent().getArtifactId(),
                staticTokens.get(PomToken.PARENT_ARTIFACTID.getToken()));
        Assert.assertEquals(acmeFooProject.getReactorParent().getMavenVersion(),
                staticTokens.get(PomToken.PARENT_VERSION.getToken()));
    }

    //
    // Private helpers
    //

    private FactoryParserAgent getFactoryParserAgent(final DefaultTokenParser parser) {

        try {
            final Field parseAgents = DefaultTokenParser.class.getDeclaredField("parseAgents");
            parseAgents.setAccessible(true);
            final List<ParserAgent> parserAgentList = (List<ParserAgent>) parseAgents.get(parser);

            for(ParserAgent current : parserAgentList) {
                if(current instanceof FactoryParserAgent) {
                    return (FactoryParserAgent) current;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not access ParserAgents list.", e);
        }

        throw new IllegalArgumentException("Could not find a FactoryParserAgent in the parserAgentList.");
    }

    private Map<String, String> getStaticTokenMapFrom(final FactoryParserAgent fpa) {

        final String fieldName = "staticTokens";
        try {
            final Field staticTokens = AbstractParserAgent.class.getDeclaredField(fieldName);
            staticTokens.setAccessible(true);

            return (Map<String, String>) staticTokens.get(fpa);
        } catch (Exception e) {
            throw new IllegalStateException("Could not access static tokens from [" + fieldName + "]", e);
        }
    }
}
