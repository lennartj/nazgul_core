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
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractNamingStrategyTest extends AbstractMavenModelTest {

    // Shared state
    private AbstractNamingStrategy unitUnderTest;

    @Before
    public void setupSharedState() {

        unitUnderTest = new TestNamingStrategy();
        setThrowsException(true);
    }

    @Test
    public void validateResolvingReactorTypePoms() {

        // Assemble
        final File rootReactorPom = getTestDataFile("foo/pom.xml");
        final File pomsReactorPom = getTestDataFile("foo/poms/pom.xml");

        final Model rootReactorModel = getPomModel(rootReactorPom);
        final Model pomsReactorModel = getPomModel(pomsReactorPom);

        // Act & Assert
        Assert.assertEquals(PomType.ROOT_REACTOR, unitUnderTest.resolvePomTypeForReactorPom(rootReactorModel));
        Assert.assertEquals(PomType.REACTOR, unitUnderTest.resolvePomTypeForReactorPom(pomsReactorModel));
    }

    @Test
    public void validateGettingPomType() {

        // Assemble
        setThrowsException(false);
        final File rootReactorPom = getTestDataFile("foo/pom.xml");
        final File pomsReactorPom = getTestDataFile("foo/poms/pom.xml");
        final File parentPom = getTestDataFile("foo/poms/foo-parent/pom.xml");
        final File apiParentPom = getTestDataFile("foo/poms/foo-api-parent/pom.xml");
        final File modelParentPom = getTestDataFile("foo/poms/foo-model-parent/pom.xml");
        final File warParentPom = getTestDataFile("foo/poms/foo-war-parent/pom.xml");

        final Model rootReactorModel = getPomModel(rootReactorPom);
        final Model pomsReactorModel = getPomModel(pomsReactorPom);
        final Model parentModel = getPomModel(parentPom);
        final Model apiParentModel = getPomModel(apiParentPom);
        final Model modelParentModel = getPomModel(modelParentPom);
        final Model warParentModel = getPomModel(warParentPom);

        // Act & Assert
        Assert.assertEquals(PomType.ROOT_REACTOR, unitUnderTest.getPomType(rootReactorModel));
        Assert.assertEquals(PomType.REACTOR, unitUnderTest.getPomType(pomsReactorModel));
        Assert.assertEquals(PomType.PARENT, unitUnderTest.getPomType(parentModel));
        Assert.assertEquals(PomType.API_PARENT, unitUnderTest.getPomType(apiParentModel));
        Assert.assertEquals(PomType.MODEL_PARENT, unitUnderTest.getPomType(modelParentModel));
        Assert.assertEquals(PomType.WAR_PARENT, unitUnderTest.getPomType(warParentModel));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnInvalidPom() {

        // Assemble
        setThrowsException(true);
        final File apiParentPom = getTestDataFile("foo/poms/foo-api-parent/pom.xml");
        final Model apiParentModel = getPomModel(apiParentPom);

        // Act & Assert
        unitUnderTest.getPomType(apiParentModel);
    }

    @Test
    public void validateOtherParentYieldsNoException() {

        // Assemble
        setThrowsException(false);
        final File earParentPom = getTestDataFile("invalid_modules_in_parent/poms/foo-ear-parent/pom.xml");
        final Model earParentModel = getPomModel(earParentPom);

        // Act & Assert
        Assert.assertEquals(PomType.OTHER_PARENT, unitUnderTest.getPomType(earParentModel));
    }

    @Test
    public void validateExceptionOnUnknownPom() {

        // Assemble
        setThrowsException(false);
        final File apiParentPom = getTestDataFile("invalid_modules_in_parent/poms/foo-not-a-parent/pom.xml");
        final Model apiParentModel = getPomModel(apiParentPom);

        // Act & Assert
        try {
            unitUnderTest.getPomType(apiParentModel);
            Assert.fail("Acquiring the PomType for an invalid Parent POM should yield an Exception.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Could not define PomType for POM "));
        }
    }

    @Test
    public void validateExceptionOnIncorrectParentPom() {

        // Assemble
        setThrowsException(false);
        final File parentPom = getTestDataFile("foo/poms/foo-parent/pom.xml");
        final File apiParentPom = getTestDataFile("foo/poms/foo-api-parent/pom.xml");

        final Model parentModel = getPomModel(parentPom);
        final Model apiParentModel = getPomModel(apiParentPom);

        // Act & Assert #1
        unitUnderTest.validate(unitUnderTest.createName(parentModel), unitUnderTest.getPomType(parentModel));
        unitUnderTest.validate(unitUnderTest.createName(apiParentModel), unitUnderTest.getPomType(apiParentModel));
    }

    //
    // Private helpers
    //

    private void setThrowsException(final boolean throwsException) {
        ((TestNamingStrategy) unitUnderTest).throwException = throwsException;
    }
}
