/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
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

package se.jguru.nazgul.core.resource.api.extractor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JarExtractorTest {

    // Shared state
    private File testClassesDirectory;
    private File targetDir;
    private JarFile jar1File;
    private File jar1FileFile;
    private FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return pathname.isFile();
        }
    };
    private FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return pathname.isDirectory();
        }
    };

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
        File targetExtractionDir = null;
        for (int i = 0; true; i++) {
            targetExtractionDir = new File(targetDir, "some/nested/directory/properExtraction_" + i);
            if (!targetExtractionDir.exists()) {
                break;
            }
        }

        // Act
        JarExtractor.extractResourcesFrom(jar1File, JarExtractor.ALL_RESOURCES, targetExtractionDir, true);

        // Assert
        Assert.assertTrue(targetExtractionDir.exists());
        Assert.assertTrue(targetExtractionDir.isDirectory());

        final Map<String, File> path2FileMap = new TreeMap<>();
        addFiles(path2FileMap, targetExtractionDir);

        Assert.assertEquals(5, path2FileMap.size());
        Assert.assertNotNull(getPathEndingWith(path2FileMap, "logback-test.xml"));
        Assert.assertNotNull(getPathEndingWith(path2FileMap, "binary/orange_ball.png"));
        Assert.assertNotNull(getPathEndingWith(path2FileMap, "onlyInJar/textfiles/file1.txt"));
        Assert.assertNotNull(getPathEndingWith(path2FileMap, "onlyInJar/textfiles/file2.txt"));
        Assert.assertNotNull(getPathEndingWith(path2FileMap, "META-INF/MANIFEST.MF"));
    }

    @Test
    public void validateExtractingSingleFileUsingPatternFiltering() {

        // Assemble
        final String filePathPattern = "onlyInJar/textfiles/file1.txt";
        File targetExtractionDir = null;
        for (int i = 0; true; i++) {
            targetExtractionDir = new File(targetDir, "extracted/singleFile/pattern_" + i);
            if (!targetExtractionDir.exists()) {
                break;
            }
        }

        final Pattern singleFilePattern = Pattern.compile(filePathPattern);

        // Act
        JarExtractor.extractResourcesFrom(jar1File, singleFilePattern, targetExtractionDir, true);

        // Assert
        Assert.assertTrue(targetExtractionDir.exists());
        Assert.assertTrue(targetExtractionDir.isDirectory());

        final Map<String, File> path2FileMap = new TreeMap<>();
        addFiles(path2FileMap, targetExtractionDir);

        Assert.assertEquals(1, path2FileMap.size());
        Assert.assertNotNull(getPathEndingWith(path2FileMap, filePathPattern));
    }

    //
    // Private helpers
    //

    private void addFiles(final Map<String, File> path2FileMap, final File aDirectory) {

        if (aDirectory.isDirectory()) {
            for (File current : aDirectory.listFiles(fileFilter)) {
                try {
                    path2FileMap.put(current.getCanonicalPath(), current);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not get canonical path for ["
                            + current.getAbsolutePath() + "]");
                }
            }

            for (File current : aDirectory.listFiles(dirFilter)) {
                addFiles(path2FileMap, current);
            }
        }
    }

    private String getPathEndingWith(final Map<String, File> path2FileMap, final String pathEnd) {
        for (String current : path2FileMap.keySet()) {
            if (current.replace(File.separator, "/").endsWith(pathEnd)) {
                return current;
            }
        }

        return null;
    }
}
