package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractMavenModelTest {

    // Shared state
    protected MavenXpp3Reader pomReader;
    protected File testDataDirectory;

    protected AbstractMavenModelTest() {
        pomReader = new MavenXpp3Reader();

        final ClassLoader classLoader = getClass().getClassLoader();
        testDataDirectory = new File(classLoader.getResource("testdata").getPath());
        Assert.assertTrue(testDataDirectory.exists() && testDataDirectory.isDirectory());
    }

    protected final Model getPomModel(final File aPomFile) {
        String canonicalPath = null;
        try {
            canonicalPath = aPomFile.getCanonicalPath();
            return pomReader.read(new FileReader(aPomFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read POM file [" + canonicalPath+ "]", e);
        }
    }

    protected final File getTestDataFile(final String relativePath) {
        return new File(testDataDirectory, relativePath);
    }
}
