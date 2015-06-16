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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractMavenModelTest;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.PomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPatternPomAnalyzer;

import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultStructureNavigatorTest extends AbstractMavenModelTest {

    // Shared state
    private NamingStrategy namingStrategy;
    private PomAnalyzer pomAnalyzer;

    @Before
    public void setupSharedState() {
        TestPatternPomAnalyzer pomAnalyzer = new TestPatternPomAnalyzer();
        this.namingStrategy = pomAnalyzer.getNamingStrategy();
        this.pomAnalyzer = pomAnalyzer;
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullProjectNamingStrategy() {

        // Act & Assert
        new DefaultStructureNavigator(null, null);
    }

    @Test
    public void validateFindingRootProjectDirectory() {

        // Assemble
        final String parentPomRelativePath = "foo/poms/pom.xml";
        final File fooRootDir = getTestDataFile("foo");
        final File fooReactorPom = getTestDataFile(parentPomRelativePath);
        final File fooParentPomDir = getTestDataFile("foo/poms/foo-parent");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act
        final File projectRootDirectory1 = unitUnderTest.getProjectRootDirectory(fooReactorPom);
        final File projectRootDirectory2 = unitUnderTest.getProjectRootDirectory(fooParentPomDir);

        // Assert
        Assert.assertNotNull(projectRootDirectory1);
        Assert.assertNotNull(projectRootDirectory2);
        Assert.assertEquals(fooRootDir.getAbsolutePath(), projectRootDirectory1.getAbsolutePath());
        Assert.assertEquals(fooRootDir.getAbsolutePath(), projectRootDirectory2.getAbsolutePath());
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnNonProjectDirectory() {

        // Assemble
        final File notAProjectDirectory = getTestDataFile("foo").getParentFile();
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(notAProjectDirectory);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnIncorrectParentGroupIdInRootReactorPom() {

        // Assemble
        final File aDir = getTestDataFile("invalid_rootreactor_parentgroupid/poms");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(aDir);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnNoPomsDirectory() {

        // Assemble
        final File aDir = getTestDataFile("invalid_no_poms_dir/bar");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(aDir);
    }

    @Test
    public void validateRelativePaths() {

        // Assemble
        final File fooPomsReactorDir = getTestDataFile("foo/poms");
        final File barBazDir = getTestDataFile("foo/bar/baz");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act
        final String pomReactorRelativePackagePath = unitUnderTest.getRelativePath(fooPomsReactorDir, true);
        final String pomReactorRelativePath = unitUnderTest.getRelativePath(fooPomsReactorDir, false);
        final String barBazRelativePackagePath = unitUnderTest.getRelativePath(barBazDir, true);
        final String barBazRelativePath = unitUnderTest.getRelativePath(barBazDir, false);

        // Assert
        Assert.assertEquals("poms", pomReactorRelativePackagePath);
        Assert.assertEquals("poms", pomReactorRelativePath);
        Assert.assertEquals("bar.baz", barBazRelativePackagePath);
        Assert.assertEquals("bar/baz", barBazRelativePath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnExistentNonDirectoryArgument() {

        // Assemble
        final File fooPomsReactorPom = getTestDataFile("foo/poms/pom.xml");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getRelativePath(fooPomsReactorPom, true);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnSubmittingNonProjectDirectory() {

        // Assemble
        final File fooPomsReactorPom = getTestDataFile("foo/poms/pom.xml");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        final File projectRootDirectory = unitUnderTest.getProjectRootDirectory(fooPomsReactorPom);
        Assert.assertNotNull(projectRootDirectory);
        Assert.assertTrue(projectRootDirectory.exists() && projectRootDirectory.isDirectory());

        final File directoryOutsideOfProject = getTestDataFile("invalid_no_poms_dir/bar");
        unitUnderTest.getRelativePath(directoryOutsideOfProject, true);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnParentContainingModules() {

        // Assemble
        final File pomsReactorPom = getTestDataFile("invalid_modules_in_parent/poms/pom.xml");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(pomsReactorPom);
    }
}
