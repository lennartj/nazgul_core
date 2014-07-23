package se.jguru.nazgul.core.quickstart.api.generator;

import org.junit.Assert;
import org.junit.Before;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractComponentFactoryTest {

    // Shared state
    private NamingStrategy namingStrategy;
    private File testDataDir;
    private File factoryRootDir;

    @Before
    public void setupSharedState() {

        namingStrategy = new TestNamingStrategy(false);
        final URL testdata = getClass().getClassLoader().getResource("testdata");
        testDataDir = new File(testdata.getPath());
        factoryRootDir = new File(testDataDir, "componentFactoryRoot");

        Assert.assertTrue(testDataDir.exists() && testDataDir.isDirectory());

        if(!factoryRootDir.exists()) {
            factoryRootDir = FileUtils.makeDirectory(testDataDir, "componentFactoryRoot");
        }
    }

    //
    // Private helpers
    //

    /*
    private File createProject() {

    }
    */
}
