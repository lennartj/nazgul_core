/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.api;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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

        Assert.assertTrue(FileUtils.DIRECTORY_FILTER.accept(testDataDir));
        Assert.assertTrue(FileUtils.DIRECTORY_FILTER.accept(fooReactorRootDir));
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

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnTryingToReadNonexistentFile() {

        // Assemble
        final String resourceURL = "a/non/existent/file";

        // Act & Assert
        FileUtils.readFile(resourceURL);
    }

    @Test
    public void validateReadingTextFile() {

        // Assemble
        final String expected = "This is row 1 in the simple text file.\n" +
                "This is row 2 in the simple text file.\n";
        final String filePath = "testdata/directory/simple.txt";

        // Act
        final String data = FileUtils.readFile(filePath);

        // Assert
        Assert.assertEquals(expected, data);
    }

    @Test
    public void validateRecursiveListing() {

        // Assemble
        final String simpleTextFilePath = "simple.txt";
        final String anotherSimpleTextFilePath = "subdirectory/anotherSimple.txt";
        final String binFilePath = "binary/orange_ball.png";
        final List<String> expectedFileNames = Arrays.asList(simpleTextFilePath,
                anotherSimpleTextFilePath,
                binFilePath);

        final String dirPath = "testdata/directory";
        final URL resource = getClass().getClassLoader().getResource(dirPath);
        final File path = new File(resource.getPath());

        // Act
        final SortedMap<String, File> files = FileUtils.listFilesRecursively(path);

        // Assert
        Assert.assertEquals(expectedFileNames.size(), files.size());
        for (String current : expectedFileNames) {

            if (current.equals(binFilePath)) {
                Assert.assertFalse(FileUtils.CHARACTER_DATAFILE_FILTER.accept(files.get(current)));
            } else {
                Assert.assertTrue(FileUtils.CHARACTER_DATAFILE_FILTER.accept(files.get(current)));
            }
            Assert.assertTrue(expectedFileNames.contains(current));
        }

        final File simpleTextFile = files.get(simpleTextFilePath);
        final File anotherSimpleTextFile = files.get(anotherSimpleTextFilePath);

        Assert.assertEquals(
                FileUtils.getCanonicalPath(new File(path, simpleTextFilePath)),
                FileUtils.getCanonicalPath(simpleTextFile));
        Assert.assertEquals(
                FileUtils.getCanonicalPath(new File(path, anotherSimpleTextFilePath)),
                FileUtils.getCanonicalPath(anotherSimpleTextFile));
    }

    @Test
    public void validateReadingFiles() {

        // Assemble
        final String simpleTextFilePath = "simple.txt";
        final String anotherSimpleTextFilePath = "subdirectory/anotherSimple.txt";
        final File path = new File(getClass().getClassLoader().getResource("testdata/directory").getPath());
        final SortedMap<String, File> textFiles = FileUtils.listFilesRecursively(path);

        // Act
        final String simpleTextFileData = FileUtils.readFile(textFiles.get(simpleTextFilePath));
        final String anotherTextFileData = FileUtils.readFile(textFiles.get(anotherSimpleTextFilePath));

        // Assert
        Assert.assertEquals("This is row 1 in the simple text file.\nThis is row 2 in the simple text file.\n",
                simpleTextFileData);
        Assert.assertEquals("This is row 1 in another simple text file.\nThis is row 2 in another simple text file.\n",
                anotherTextFileData);
    }

    @Test
    public void validateCharDataFileDetection() {

        // Assemble
        final String binFilePath = "binary/orange_ball.png";
        final File path = new File(getClass().getClassLoader().getResource("testdata/directory").getPath());
        final SortedMap<String, File> textFiles = FileUtils.listFilesRecursively(path);
        final File nonexistent = new File(path, "aNonexistentFile.txt");

        // Act & Assert
        for (Map.Entry<String, File> current : textFiles.entrySet()) {

            final boolean isCharacterFile = !current.getKey().equals(binFilePath);
            Assert.assertEquals(isCharacterFile, FileUtils.CHARACTER_DATAFILE_FILTER.accept(current.getValue()));
        }
        Assert.assertFalse(FileUtils.CHARACTER_DATAFILE_FILTER.accept(nonexistent));
    }

    @Test
    public void validateExistenceRequiredForFilters() {

        // Assemble
        final File path = new File(getClass().getClassLoader().getResource("testdata/directory").getPath());
        final File nonexistent = new File(path, "aNonexistentFile.txt");

        // Act & Assert
        Assert.assertFalse(FileUtils.MODULE_NAME_FILTER.accept(path));
        Assert.assertFalse(FileUtils.MODULE_NAME_FILTER.accept(nonexistent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnReadingNonExistentFile() {

        // Act & Assert
        FileUtils.readFile(new File("/some/nonexistent/file.txt"));
    }

    @Test
    public void validateAcceptingEmptyExistingDirectory() {

        // Assemble
        final File anEmptyDirectory = createUniqueDirectoryUnderTestData("anEmptyDirectory");
        final File aDotFile = new File(anEmptyDirectory, ".someConfigFile");
        FileUtils.writeFile(aDotFile, "SomeContent");

        // Act & Assert
        Assert.assertTrue(FileUtils.NONEXISTENT_OR_EMPTY_DIRECTORY_FILTER.accept(anEmptyDirectory));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnWritingFileWithoutExistingParent() {

        // Assemble
        final File nonExistingFile = new File(System.getProperty("java.io.tmpdir"), "a/non/existing/file");

        // Act & Assert
        FileUtils.writeFile(nonExistingFile, "foobar!");
    }

    //
    // Helpers
    //

    public static File createUniqueDirectoryUnderTestData(final String name) {

        // Get the testdata directory
        final URL testdata = Thread.currentThread().getContextClassLoader().getResource("testdata");
        final File testdataDir = new File(testdata.getPath());

        Assert.assertTrue("Required testdata directory [" + FileUtils.getCanonicalPath(testdataDir)
                + "] nonexistent.", testdataDir.exists());
        for (int i = 0; true; i++) {
            final String dirName = name + "_" + i;
            final File toReturn = new File(testdataDir, dirName);
            if (!toReturn.exists()) {
                return FileUtils.makeDirectory(testdataDir, dirName);
            }
        }
    }
}
