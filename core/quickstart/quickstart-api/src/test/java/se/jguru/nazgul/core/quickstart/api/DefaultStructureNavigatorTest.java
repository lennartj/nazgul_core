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
package se.jguru.nazgul.core.quickstart.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestPomAnalyzer;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultStructureNavigatorTest {

    // Shared state
    private NamingStrategy namingStrategy;
    private TestPomAnalyzer pomAnalyzer;

    @Before
    public void setupSharedState() {
        pomAnalyzer = new TestPomAnalyzer();
        this.namingStrategy = pomAnalyzer.getNamingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullProjectNamingStrategy() {

        // Act & Assert
        new DefaultStructureNavigator(null, null);
    }

    @Test
    public void validateFindingRootProjectDirectory() {

        // Assemble
        final File fooRootDir = getFileOrDirectory("testdata/foo");
        final File fooReactorPom = getFileOrDirectory("testdata/foo/poms/pom.xml");
        final File fooParentPomDir = getFileOrDirectory("testdata/foo/poms/foo-parent");
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
        final File notAProjectDirectory = getFileOrDirectory("testdata/foo").getParentFile();
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(notAProjectDirectory);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnIncorrectParentGroupIdInRootReactorPom() {

        // Assemble
        final File aDir = getFileOrDirectory("testdata/invalid_rootreactor_parentgroupid/poms");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(aDir);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnNoPomsDirectory() {

        // Assemble
        final File aDir = getFileOrDirectory("testdata/invalid_no_poms_dir/bar");
        final DefaultStructureNavigator unitUnderTest = new DefaultStructureNavigator(namingStrategy, pomAnalyzer);

        // Act & Assert
        unitUnderTest.getProjectRootDirectory(aDir);
    }

    //
    // Private helpers
    //

    private File getFileOrDirectory(final String relativePath) {
        final URL resource = getClass().getClassLoader().getResource(relativePath);
        if(resource == null) {
            return null;
        }
        return new File(resource.getPath());
    }
}
