package se.jguru.nazgul.core.quickstart.api.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.helpers.TestProjectFactory;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractProjectFactoryTest {

    // Shared state
    private NamingStrategy namingStrategy;
    private File testDataDir;

    @Before
    public void setupSharedState() {

        namingStrategy = new TestNamingStrategy(false);
        final URL testdata = getClass().getClassLoader().getResource("testdata");
        testDataDir = new File(testdata.getPath());

        Assert.assertTrue(testDataDir.exists() && testDataDir.isDirectory());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamingStrategy() {

        // Act & Assert
        new TestProjectFactory(null);
    }

    @Test
    public void validateCreatingProjectData() {

        // Assemble
        final TestProjectFactory unitUnderTest = new TestProjectFactory(namingStrategy);

        final SimpleArtifact reactorParent = new SimpleArtifact(
                "se.jguru.nazgul.tools.poms.external",
                "nazgul-tools-external-reactor-parent",
                "4.0.0");

        final SimpleArtifact parentParent = new SimpleArtifact(
                "se.jguru.nazgul.core.poms.core-parent",
                "nazgul-core-parent",
                "1.6.1");

        // Act
        final Project projectDefinition = unitUnderTest.createProjectDefinition(
                "nazgul", "blah", reactorParent, parentParent);

        // Assert
        Assert.assertNotNull(projectDefinition);
        Assert.assertEquals("blah", projectDefinition.getName());
        Assert.assertEquals("nazgul", projectDefinition.getPrefix());
        Assert.assertSame(parentParent, projectDefinition.getParentParent());
        Assert.assertSame(reactorParent, projectDefinition.getReactorParent());
    }

    @Test
    public void validateCreatingProjectSkeleton() {

        // Assemble
        final File rootDir = new File(testDataDir, "createProjectData");
        rootDir.mkdir();
        final TestProjectFactory unitUnderTest = new TestProjectFactory(namingStrategy);

        final SimpleArtifact reactorParent = new SimpleArtifact(
                "se.jguru.nazgul.tools.poms.external",
                "nazgul-tools-external-reactor-parent",
                "4.0.0");

        final SimpleArtifact parentParent = new SimpleArtifact(
                "se.jguru.nazgul.core.poms.core-parent",
                "nazgul-core-parent",
                "1.6.1");

        final Project projectDefinition = unitUnderTest.createProjectDefinition(
                "nazgul", "blah", reactorParent, parentParent);

        // Act
        final boolean created = unitUnderTest.createProject(rootDir, projectDefinition);

        // Assert
        Assert.assertTrue(created);

        final File reactorRoot = new File(rootDir, projectDefinition.getName());
        validateDirectory(reactorRoot, PomType.ROOT_REACTOR);
        validateDirectory(new File(reactorRoot, "poms"), PomType.REACTOR);
        validateDirectory(new File(reactorRoot, "poms/blah-parent"), PomType.PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-api-parent"), PomType.API_PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-model-parent"), PomType.MODEL_PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-war-parent"), PomType.WAR_PARENT);

        Assert.assertEquals("[] ==> [ROOT_REACTOR]", unitUnderTest.callTrace.get(0));
        Assert.assertEquals("[poms] ==> [REACTOR]", unitUnderTest.callTrace.get(1));
        Assert.assertEquals("[poms] ==> [PARENT]", unitUnderTest.callTrace.get(2));
        Assert.assertEquals("[poms] ==> [API_PARENT]", unitUnderTest.callTrace.get(3));
        Assert.assertEquals("[poms] ==> [MODEL_PARENT]", unitUnderTest.callTrace.get(4));
        Assert.assertEquals("[poms] ==> [WAR_PARENT]", unitUnderTest.callTrace.get(5));
    }

    //
    // Private helpers
    //

    private void validateDirectory(final File dir, final PomType pomType) {
        Assert.assertTrue("Required directory [" + dir.getAbsolutePath() + "] was not found.",
                dir.exists() && dir.isDirectory());

        final File pomFile = new File(dir, "pom.xml");
        Assert.assertTrue("Required POM file [" + pomFile.getAbsolutePath() + "] was not found.",
                pomFile.exists() && pomFile.isFile());

        // The TestPomFactory should have generated POM file data holding the PomType in text.
        // Validate that the correct pom is written to the correct location.
        final String data = FileUtils.readFile(pomFile).trim();
        Assert.assertEquals("pomData: [" + pomType + "]", data);
    }
}
