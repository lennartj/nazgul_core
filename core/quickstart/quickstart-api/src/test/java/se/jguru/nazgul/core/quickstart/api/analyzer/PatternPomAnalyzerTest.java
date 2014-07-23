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
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;

import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternPomAnalyzerTest extends AbstractMavenModelTest {

    // Shared state
    private NamingStrategy namingStrategy;
    private PatternPomAnalyzer unitUnderTest;

    @Before
    public void setupSharedState() {
        namingStrategy = new TestNamingStrategy();
        unitUnderTest = new PatternPomAnalyzer(namingStrategy);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamingStrategy() {

        // Act & Assert
        new PatternPomAnalyzer(null);
    }

    @Test
    public void validateCorrectReactorPoms() {

        // Assemble
        final File fooRootReactorPomFile = getTestDataFile("foo/pom.xml");
        final File fooPomsReactorPomFile = getTestDataFile("foo/poms/pom.xml");

        // Act & Assert
        final Model rootReactorPomModel = getPomModel(fooRootReactorPomFile);
        unitUnderTest.validateRootReactorPom(rootReactorPomModel);
        unitUnderTest.validate(getPomModel(fooPomsReactorPomFile), PomType.REACTOR, rootReactorPomModel);
    }

    @Test
    public void validateCorrectRootParentPoms() {

        // Assemble
        final File fooParentPomFile = getTestDataFile("foo/poms/foo-parent/pom.xml");
        final File fooApiParentPomFile = getTestDataFile("foo/poms/foo-api-parent/pom.xml");
        final File fooModelParentPomFile = getTestDataFile("foo/poms/foo-model-parent/pom.xml");
        final File fooWarParentPomFile = getTestDataFile("foo/poms/foo-war-parent/pom.xml");

        // Act & Assert
        final Model fooParentPomModel = getPomModel(fooParentPomFile);
        final Model fooApiParentPomModel = getPomModel(fooApiParentPomFile);
        final Model fooModelParentPomModel = getPomModel(fooModelParentPomFile);
        final Model fooWarParentPomModel = getPomModel(fooWarParentPomFile);

        unitUnderTest.validateTopmostParentPom(fooParentPomModel);
        unitUnderTest.validate(fooApiParentPomModel, PomType.API_PARENT, fooParentPomModel);
        unitUnderTest.validate(fooModelParentPomModel, PomType.MODEL_PARENT, fooApiParentPomModel);
        unitUnderTest.validate(fooWarParentPomModel, PomType.WAR_PARENT, fooApiParentPomModel);
    }

    @Test
    public void validateExceptionOnIncorrectRootReactorGroupId() {

        // Assemble
        final File fooRootReactorPomFile = getTestDataFile("invalid_rootreactor_parentgroupid/pom.xml");

        // Act
        final Model rootReactorPomModel = getPomModel(fooRootReactorPomFile);

        // Assert
        try {
            unitUnderTest.validateRootReactorPom(rootReactorPomModel);
            Assert.fail("An invalid root reactor POM should yield an exception.");
        } catch (InvalidStructureException e) {
            Assert.assertTrue(e.getMessage().contains(" 1: Incorrect parent groupId [some.incorrect.parent.group.id]"));
        }
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnIncompatibleRootReactorPom() {

        // Assemble
        final File fooPomsReactorPomFile = getTestDataFile("foo/poms/pom.xml");

        // Act & Assert
        unitUnderTest.validateRootReactorPom(getPomModel(fooPomsReactorPomFile));
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnIncompatibleTopmostParentPom() {

        // Assemble
        final File fooApiParentPomFile = getTestDataFile("foo/poms/foo-api-parent/pom.xml");

        // Act & Assert
        unitUnderTest.validateTopmostParentPom(getPomModel(fooApiParentPomFile));
    }
}
