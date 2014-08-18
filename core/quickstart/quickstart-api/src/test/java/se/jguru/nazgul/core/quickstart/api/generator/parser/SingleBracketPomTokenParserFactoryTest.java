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
        acmeFooProject = new Project("acme", "foo", reactorParent, parentParent);
    }

    @Test
    public void validateStandardBuilderSteps() {

        // Assemble

        // Act
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(PomType.COMPONENT_API, acmeFooProject)
                .withRelativeDirectoryPath("services/finance/finance-api")
                .withProjectGroupIdPrefix("org.acme")
                .withoutProjectSuffix()
                .build();

        // Assert
        Assert.assertTrue(tokenParser instanceof DefaultTokenParser);

        final DefaultTokenParser parser = (DefaultTokenParser) tokenParser;
        final FactoryParserAgent fpa = getFactoryParserAgent(parser);
        Assert.assertNotNull(fpa);

        final Map<String, String> staticTokens = getStaticTokenMapFrom(fpa);
        Assert.assertNotNull(staticTokens);

        System.out.println("Got: " + staticTokens);

        Assert.assertEquals("services/finance/finance-api", staticTokens.get(PomToken.RELATIVE_DIRPATH.getToken()));
        Assert.assertEquals("services.finance.api", staticTokens.get(PomToken.RELATIVE_PACKAGE.getToken()));
        Assert.assertEquals("acme-finance-api", staticTokens.get(PomToken.ARTIFACTID.getToken()));
        Assert.assertEquals("org.acme.services.finance.api", staticTokens.get(PomToken.GROUPID.getToken()));
        Assert.assertEquals("org.acme", staticTokens.get("groupIdPrefix"));
        Assert.assertEquals("../../../poms/acme-foo-api-parent",
                staticTokens.get(PomToken.PARENT_POM_RELATIVE_PATH.getToken()));
        Assert.assertEquals("COMPONENT_API", staticTokens.get(SingleBracketPomTokenParserFactory.POMTYPE_KEY));
        Assert.assertEquals("component-api",
                staticTokens.get(SingleBracketPomTokenParserFactory.LOWERCASE_POMTYPE_KEY));

        // Parent tokens?
        // Assert.assertEquals();
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
