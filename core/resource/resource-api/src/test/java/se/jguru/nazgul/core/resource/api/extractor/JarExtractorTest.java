/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.resource.api.extractor;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JarExtractorTest {

    // Shared state
    private File testClassesDirectory;
    private File targetDir;
    private JarFile jar1File;
    private File jar1FileFile;

    @Before
    public void setupSharedState() throws IOException {

        final URL resource = getClass().getClassLoader().getResource("logback-test.xml");
        testClassesDirectory = new File(resource.getPath()).getParentFile();
        targetDir = testClassesDirectory.getParentFile();
        jar1FileFile = new File(testClassesDirectory, "extractor/jar1.jar");
        jar1File = new JarFile(jar1FileFile);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullJarFile() {

        // Assemble
        final File targetDirectory = new File(targetDir, "irrelevant");

        // Act & Assert
        JarExtractor.extractResourcesFrom(null, JarExtractor.ALL_RESOURCES, targetDirectory, true);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPattern() {

        // Assemble
        final File targetDirectory = new File(targetDir, "irrelevant");

        // Act & Assert
        JarExtractor.extractResourcesFrom(jar1File, null, targetDirectory, true);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullTargetDirectory() {

        // Act & Assert
        JarExtractor.extractResourcesFrom(jar1File, JarExtractor.ALL_RESOURCES, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentTargetDirectoryAndNoWrite() {

        // Assemble
        final File targetDirectory = new File(targetDir, "irrelevant");

        // Act & Assert
        JarExtractor.extractResourcesFrom(jar1File, JarExtractor.ALL_RESOURCES, targetDirectory, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnTargetDirectoryNotDirectory() {

        // Act & Assert
        JarExtractor.extractResourcesFrom(jar1File, JarExtractor.ALL_RESOURCES, jar1FileFile, true);
    }

    @Test
    public void validateDirectoryCreationAndExtraction() {

        // Assemble
        final File targetDirectory = new File(targetDir, "properExtraction");

        // Act
        JarExtractor.extractResourcesFrom(jar1File, JarExtractor.ALL_RESOURCES, targetDirectory, true);

        // Assert
        Assert.assertTrue(targetDirectory.exists());
        Assert.assertTrue(targetDirectory.isDirectory());

        final List<File> content = Arrays.asList(targetDirectory.listFiles());
        final Map<String, File> fileName2ContentMap = CollectionAlgorithms.map(content, new Transformer<File, Tuple<String, File>>() {
            @Override
            public Tuple<String, File> transform(File input) {
                return new Tuple<String, File>(input.getName(), input);
            }
        });

        Assert.assertEquals(3, content.size());
        Assert.assertTrue(fileName2ContentMap.keySet().contains("logback-test.xml"));
        Assert.assertTrue(fileName2ContentMap.keySet().contains("orange_ball.png"));
        Assert.assertTrue(fileName2ContentMap.keySet().contains("MANIFEST.MF"));
    }
}
