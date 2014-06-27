/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer.NazgulProjectNamingStrategy;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulProjectNamingStrategyTest {

    // Shared state
    private NazgulProjectNamingStrategy unitUnderTest = new NazgulProjectNamingStrategy();

    private static final String REACTOR_PARENT_GROUP_ID = "se.jguru.nazgul.tools.poms.external";
    private static final String REACTOR_PARENT_ARTIFACT_ID = "nazgul-tools-external-reactor-parent";
    private static final String REACTOR_GROUP_ID = "com.acme.foobar";
    private static final String REACTOR_ARTIFACT_ID = "foobar-reactor";

    private static final String PARENT_PARENT_GROUP_ID = "se.jguru.nazgul.core.poms.core-parent";
    private static final String PARENT_PARENT_ARTIFACT_ID = "nazgul-core-parent";
    private static final String PARENT_GROUP_ID = "com.acme.foobar.poms.foobar-parent";
    private static final String PARENT_ARTIFACT_ID = "foobar-parent";

    @Test
    public void validateProjectNamePattern() {

        // Assemble
        final String nullProjectName = null;
        final String emptyProjectName = "";
        final String singleWordProjectName = "theproject";
        final String digitFirstCharProjectName = "9projects";
        final String digitInProjectName = "project9";
        final String upperCaseInProjectName = "pRojects";
        final String dualWordProjectName = "nazgul-core";
        final String tooManySeparatorsProjectName = "some-silly-project";
        final String whitespaceInWordProjectName = "da coolproject";
        final String nonCharacterInWordProjectName = "anotherproject!";
        final String underscoreInWordProjectName = "another_project";

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isValidProjectName(nullProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(emptyProjectName));
        Assert.assertTrue(unitUnderTest.isValidProjectName(singleWordProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(digitFirstCharProjectName));
        Assert.assertTrue(unitUnderTest.isValidProjectName(digitInProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(upperCaseInProjectName));
        Assert.assertTrue(unitUnderTest.isValidProjectName(dualWordProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(tooManySeparatorsProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(whitespaceInWordProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(nonCharacterInWordProjectName));
        Assert.assertFalse(unitUnderTest.isValidProjectName(underscoreInWordProjectName));
    }

    @Test
    public void validateProjectRootDirectoryName() {

        // Assemble
        final String singleWordProjectName = "someproject";
        final String singleWordProjectNameWithDigit = "project2";
        final String dualWordProjectName = "nazgul-core";
        final String dualWordProjectNameWithDigit = "nazgul-c0re";

        // Act
        final String resultSingle = unitUnderTest.getRootDirectoryName(singleWordProjectName);
        final String resultSingleWithDigit = unitUnderTest.getRootDirectoryName(singleWordProjectNameWithDigit);
        final String resultDual = unitUnderTest.getRootDirectoryName(dualWordProjectName);
        final String resultDualWithDigit = unitUnderTest.getRootDirectoryName(dualWordProjectNameWithDigit);

        // Assert
        Assert.assertEquals("someproject", resultSingle);
        Assert.assertEquals("project2", resultSingleWithDigit);
        Assert.assertEquals("core", resultDual);
        Assert.assertEquals("c0re", resultDualWithDigit);
    }

    @Test
    public void validateProjectPrefixPattern() {

        // Assemble
        final String nullProjectPrefix = null;
        final String emptyProjectPrefix = "";
        final String singleWordProjectPrefix = "com";
        final String digitFirstCharProjectPrefix = "9com";
        final String digitFirstCharInWordProjectPrefix = "com.9foo";
        final String digitInProjectPrefix = "com9";
        final String upperCaseInProjectPrefix = "pRojects";
        final String dualWordProjectPrefix = "com.nazgul-core";
        final String whitespaceInWordProjectPrefix = "com.da coolproject";
        final String nonCharacterInWordProjectPrefix = "com.another-project!";
        final String underscoreInWordProjectPrefix = "another_project";

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(nullProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(emptyProjectPrefix));
        Assert.assertTrue(unitUnderTest.isValidProjectPrefix(singleWordProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(digitFirstCharProjectPrefix));
        Assert.assertTrue(unitUnderTest.isValidProjectPrefix(digitFirstCharInWordProjectPrefix));
        Assert.assertTrue(unitUnderTest.isValidProjectPrefix(digitInProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(upperCaseInProjectPrefix));
        Assert.assertTrue(unitUnderTest.isValidProjectPrefix(dualWordProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(whitespaceInWordProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(nonCharacterInWordProjectPrefix));
        Assert.assertFalse(unitUnderTest.isValidProjectPrefix(underscoreInWordProjectPrefix));
    }

    @Test
    public void validateProjectTopLevelPackage() {

        // Assemble
        final String reverseDns = "com.foo";
        final String reverseDnsWithDigit = "com.project2";
        final String reverseDnsWithDigitFirstInWord = "org.1st.project";

        // Act
        final String simpleResult = unitUnderTest.getTopLevelPackage(reverseDns);
        final String simpleResultWithDigit = unitUnderTest.getTopLevelPackage(reverseDnsWithDigit);
        final String simpleResultWithDigitFirst = unitUnderTest.getTopLevelPackage(reverseDnsWithDigitFirstInWord);

        // Assert
        Assert.assertEquals(reverseDns, simpleResult);
        Assert.assertEquals(reverseDnsWithDigit, simpleResultWithDigit);
        Assert.assertEquals(reverseDnsWithDigitFirstInWord, simpleResultWithDigitFirst);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnReverseDnsEndingInDot() {

        // Act & Assert
        unitUnderTest.getTopLevelPackage("com.foobar.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnReverseDnsEndingInDash() {

        // Act & Assert
        unitUnderTest.getTopLevelPackage("com.foobar-");
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
    public void validateGettingProjectName() {

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
