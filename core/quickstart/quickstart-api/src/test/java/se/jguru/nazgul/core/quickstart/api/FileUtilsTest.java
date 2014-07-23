package se.jguru.nazgul.core.quickstart.api;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FileUtilsTest {

    // Shared state
    private File testDataDir;
    private File fooReactorRootDir;

    @Before
    public void setupSharedState() {

        final URL testDataURL = getClass().getClassLoader().getResource("testdata");
        testDataDir = new File(testDataURL.getPath());
        fooReactorRootDir = new File(testDataDir, "foo");

        Assert.assertTrue(testDataDir.exists() && testDataDir.isDirectory());
        Assert.assertTrue(fooReactorRootDir.exists() && fooReactorRootDir.isDirectory());
    }

    @Test
    public void validateExtractingSimpleArtifactFromModel() {

        // Assemble
        final Model fooModel = FileUtils.getPomModel(new File(fooReactorRootDir, "pom.xml"));
        final Model fooPomsReactorModel = FileUtils.getPomModel(new File(fooReactorRootDir, "poms/pom.xml"));

        // Act
        final SimpleArtifact fooReactorArtifact = FileUtils.getSimpleArtifact(fooModel);
        final SimpleArtifact fooPomsReactorArtifact = FileUtils.getSimpleArtifact(fooPomsReactorModel);

        // Assert
        Assert.assertEquals("se.jguru.nazgul.foo", fooReactorArtifact.getGroupId());
        Assert.assertEquals("nazgul-foo-reactor", fooReactorArtifact.getArtifactId());
        Assert.assertEquals("1.0.0-SNAPSHOT", fooReactorArtifact.getMavenVersion());

        Assert.assertEquals("se.jguru.nazgul.foo.poms", fooPomsReactorArtifact.getGroupId());
        Assert.assertEquals("nazgul-foo-poms-reactor", fooPomsReactorArtifact.getArtifactId());
        Assert.assertEquals("1.0.0-SNAPSHOT", fooPomsReactorArtifact.getMavenVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnInvalidPom() {

        // Assemble
        final File incorrectPom = new File(testDataDir, "invalid_pom/pom.xml");

        // Act & Assert
        FileUtils.getPomModel(incorrectPom);
    }
}
