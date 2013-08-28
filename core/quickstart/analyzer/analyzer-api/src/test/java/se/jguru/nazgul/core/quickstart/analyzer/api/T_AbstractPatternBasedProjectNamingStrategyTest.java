/*
 * #%L
 * Nazgul Project: nazgul-core-analyzer-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.core.quickstart.analyzer.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractPatternBasedProjectNamingStrategyTest {

    // Shared state
    private String projectNamePattern;
    private String projectPrefixPattern;
    private AbstractPatternBasedProjectNamingStrategy unitUnderTest;

    private static final String REACTOR_PARENT_GROUP_ID = "se.jguru.nazgul.tools.poms.external";
    private static final String REACTOR_PARENT_ARTIFACT_ID = "nazgul-tools-external-reactor-parent";
    private static final String REACTOR_GROUP_ID = "com.acme.foobar";
    private static final String REACTOR_ARTIFACT_ID = "foobar-reactor";

    private static final String PARENT_PARENT_GROUP_ID = "se.jguru.nazgul.core.poms.core-parent";
    private static final String PARENT_PARENT_ARTIFACT_ID = "nazgul-core-parent";
    private static final String PARENT_GROUP_ID = "com.acme.foobar.poms.foobar-parent";
    private static final String PARENT_ARTIFACT_ID = "foobar-parent";

    @Before
    public void setupSharedState() {

        projectNamePattern = "test-project";
        projectPrefixPattern = "test-prefix";
        unitUnderTest = new MockProjectNamingStrategy(projectNamePattern, projectPrefixPattern);
    }

    @Test
    public void validateGettingProjectName() {

        // Assemble
        final String expected = "RootDirectoryName_test-project";

        // Act
        final String result = unitUnderTest.getRootDirectoryName(projectNamePattern);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateGettingTopLevelPackage() {

        // Assemble
        final String expected = "TopLevelPackage_test-prefix";

        // Act
        final String result = unitUnderTest.getTopLevelPackage(projectPrefixPattern);

        // Assert
        Assert.assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnIllegalProjectName() {

        // Act & Assert
        unitUnderTest.getRootDirectoryName("notAValidProjectName");
    }

    @Test
    public void validateProjectPrefixPatterns() {

        // Assemble
        final String nullPrefixPattern = null;
        final String invalidPrefixPattern = "invalid";
        final String validPrefixPattern = projectPrefixPattern;

        // Act & Assert
        Assert.assertTrue(unitUnderTest.isValidProjectPrefix(validPrefixPattern));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(nullPrefixPattern));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(invalidPrefixPattern));
    }

    @Test
    public void validateProjectNamePatterns() {

        // Assemble
        final String nullPattern = null;
        final String invalidNamePattern = "invalid";
        final String validNamePattern = projectNamePattern;

        // Act & Assert
        Assert.assertTrue(unitUnderTest.isValidProjectName(validNamePattern));
        Assert.assertFalse(unitUnderTest.isValidProjectName(nullPattern));
        Assert.assertFalse(unitUnderTest.isValidProjectName(invalidNamePattern));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyArtifactIdWhenGettingProjectName() {

        // Act & Assert
        unitUnderTest.getProjectName(REACTOR_PARENT_GROUP_ID, REACTOR_PARENT_ARTIFACT_ID, REACTOR_GROUP_ID, "");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullArtifactIdWhenGettingProjectName() {

        // Act & Assert
        unitUnderTest.getProjectName(REACTOR_PARENT_GROUP_ID, REACTOR_PARENT_ARTIFACT_ID, REACTOR_GROUP_ID, null);
    }

    @Test
    public void validateGettingProjectNameFromMavenModelData() {

        // Assemble
        final String projectName = "foobar";

        // Act
        final String reactorDefinedProjectName = unitUnderTest.getProjectName(
                REACTOR_PARENT_GROUP_ID, REACTOR_PARENT_ARTIFACT_ID, REACTOR_GROUP_ID, REACTOR_ARTIFACT_ID);
        final String parentDefinedProjectName = unitUnderTest.getProjectName(
                PARENT_PARENT_GROUP_ID, PARENT_PARENT_ARTIFACT_ID, PARENT_GROUP_ID, PARENT_ARTIFACT_ID);

        // Assert
        Assert.assertEquals(projectName, reactorDefinedProjectName);
        Assert.assertEquals(projectName, parentDefinedProjectName);
    }
}
