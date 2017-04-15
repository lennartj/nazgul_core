/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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
package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractPomAnalyzerTest extends AbstractMavenModelTest {

    // Shared state
    private List<String> callTrace;
    private TestNamingStrategy testNamingStrategy;

    @Before
    public void setupSharedState() {
        callTrace = new ArrayList<>();
        testNamingStrategy = new TestNamingStrategy(false);
    }

    @Test
    public void validateCorrectReactorPoms() {

        // Assemble
        final File fooRootReactorPomFile = getTestDataFile("foo/pom.xml");
        final File fooPomsReactorPomFile = getTestDataFile("foo/poms/pom.xml");
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        // Act
        final Model rootReactorPomModel = getPomModel(fooRootReactorPomFile);
        unitUnderTest.validateRootReactorPom(rootReactorPomModel);
        unitUnderTest.validate(getPomModel(fooPomsReactorPomFile), PomType.REACTOR, rootReactorPomModel);

        // Assert
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals("performCustomRootReactorPomValidation", callTrace.get(0));
        Assert.assertEquals("validateReactorPom", callTrace.get(1));
    }

    @Test
    public void validateCorrectRootParentPoms() {

        // Assemble
        final File fooParentPomFile = getTestDataFile("foo/poms/foo-parent/pom.xml");
        final File fooApiParentPomFile = getTestDataFile("foo/poms/foo-api-parent/pom.xml");
        final File fooModelParentPomFile = getTestDataFile("foo/poms/foo-model-parent/pom.xml");
        final File fooWarParentPomFile = getTestDataFile("foo/poms/foo-war-parent/pom.xml");
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        // Act
        final Model fooParentPomModel = getPomModel(fooParentPomFile);
        final Model fooApiParentPomModel = getPomModel(fooApiParentPomFile);
        final Model fooModelParentPomModel = getPomModel(fooModelParentPomFile);
        final Model fooWarParentPomModel = getPomModel(fooWarParentPomFile);

        unitUnderTest.validateTopmostParentPom(fooParentPomModel);
        unitUnderTest.validate(fooApiParentPomModel, PomType.API_PARENT, fooParentPomModel);
        unitUnderTest.validate(fooModelParentPomModel, PomType.MODEL_PARENT, fooApiParentPomModel);
        unitUnderTest.validate(fooWarParentPomModel, PomType.WAR_PARENT, fooApiParentPomModel);

        // Assert
        List<String> expectedCallStructure = Arrays.asList("verifyNoModules",
                "performCustomTopmostParentPomValidation",
                "validateParentPom",
                "verifyNoModules",
                "validateParentPom",
                "verifyNoModules",
                "validateParentPom",
                "verifyNoModules");
        Assert.assertEquals(expectedCallStructure.size(), callTrace.size());
        for (int i = 0; i < expectedCallStructure.size(); i++) {
            Assert.assertEquals(expectedCallStructure.get(i), callTrace.get(i));
        }
    }

    @Test
    public void validateInvalidStructureExceptionOnInvalidRootReactorPom() {

        // Assemble
        testNamingStrategy.throwException = true;
        final File fooRootReactorPomFile = getTestDataFile("foo/pom.xml");
        final Model rootReactorPomModel = getPomModel(fooRootReactorPomFile);
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        // Act & Assert
        try {
            unitUnderTest.validateRootReactorPom(rootReactorPomModel);
            Assert.fail("When the naming strategy emits an Exception for invalid POM naming, an "
                    + "InvalidStructureException must be thrown from the AbstractPomAnalyzer");
        } catch (InvalidStructureException e) {
            Assert.assertTrue(e.getMessage().contains("Incompatible POM name for strategy ["));
        }
    }

    @Test
    public void validateExpectedArtifactIdSuffix() {

        // Assemble
        testNamingStrategy.throwException = true;
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        final Map<PomType, String> expected = new TreeMap<>();
        expected.put(PomType.ROOT_REACTOR, AbstractNamingStrategy.REACTOR_SUFFIX);
        expected.put(PomType.REACTOR, AbstractNamingStrategy.REACTOR_SUFFIX);
        expected.put(PomType.PARENT, AbstractNamingStrategy.PARENT_SUFFIX);
        expected.put(PomType.API_PARENT, "api-parent");
        expected.put(PomType.MODEL_PARENT, "model-parent");
        expected.put(PomType.WAR_PARENT, "war-parent");
        expected.put(PomType.OTHER_PARENT, "parent");

        // Act & Assert
        for (PomType current : Arrays.asList(PomType.values())) {
            Assert.assertEquals(expected.get(current), unitUnderTest.getExpectedArtifactIdSuffix(current));
        }
    }

    @Test
    public void validateInvalidStructureExceptionOnInvalidTopmostParentPom() {

        // Assemble
        testNamingStrategy.throwException = true;
        final File fooParentPomFile = getTestDataFile("foo/poms/foo-parent/pom.xml");
        final Model fooParentPomModel = getPomModel(fooParentPomFile);
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        // Act & Assert
        try {
            unitUnderTest.validateTopmostParentPom(fooParentPomModel);
            Assert.fail("When the naming strategy emits an Exception for invalid POM naming, an "
                    + "InvalidStructureException must be thrown from the AbstractPomAnalyzer");
        } catch (InvalidStructureException e) {
            Assert.assertTrue(e.getMessage().contains("Incompatible POM name for strategy ["));
        }
    }

    @Test
    public void validateVerifyingParentPoms() {

        // Assemble
        testNamingStrategy.throwException = false;
        final AbstractPomAnalyzer unitUnderTest = getAbstractPomAnalyzer(testNamingStrategy);

        final Model incorrectParentGID = getParentModel(true);
        final Model incorrectParentAID = getParentModel(true);
        final Model incorrectGID = getParentModel(true);
        final Model incorrectAID = getParentModel(true);

        incorrectParentGID.getParent().setGroupId("some.incorrect.group.id");
        incorrectParentAID.getParent().setArtifactId("some-incorrect-artifact-id");
        incorrectGID.setGroupId("some.incorrect.group.id");
        incorrectAID.setArtifactId("some-incorrect-artifact-id");

        // Act & Assert
        unitUnderTest.verifyParentPom(getParentModel(true), getParentModel(false));
        verifyInvalidParentPom(unitUnderTest, incorrectParentGID);
        verifyInvalidParentPom(unitUnderTest, incorrectParentAID);
        verifyInvalidParentPom(unitUnderTest, incorrectGID);
        verifyInvalidParentPom(unitUnderTest, incorrectAID);
    }

    //
    // Private helpers
    //

    private AbstractPomAnalyzer getAbstractPomAnalyzer(final NamingStrategy namingStrategy) {
        return new AbstractPomAnalyzer(namingStrategy) {
            @Override
            protected void validateParentPom(final Model toValidate,
                                             final PomType expectedType,
                                             final Model parentOrNull)
                    throws InvalidStructureException {
                callTrace.add("validateParentPom");
                super.validateParentPom(toValidate, expectedType, parentOrNull);
            }

            @Override
            protected void validateReactorPom(final Model toValidate,
                                              final Model nonNullParent)
                    throws InvalidStructureException {
                callTrace.add("validateReactorPom");
                super.validateReactorPom(toValidate, nonNullParent);
            }

            @Override
            protected void performCustomRootReactorPomValidation(final Model rootReactorModel)
                    throws InvalidStructureException {
                callTrace.add("performCustomRootReactorPomValidation");
                super.performCustomRootReactorPomValidation(rootReactorModel);
            }

            @Override
            protected void performCustomTopmostParentPomValidation(final Model topmostParentModel)
                    throws InvalidStructureException {
                callTrace.add("performCustomTopmostParentPomValidation");
                super.performCustomTopmostParentPomValidation(topmostParentModel);
            }

            @Override
            protected void verifyNoModules(final Model toVerify) throws InvalidStructureException {
                callTrace.add("verifyNoModules");
                super.verifyNoModules(toVerify);
            }

            @Override
            protected String getExpectedArtifactIdSuffix(final PomType type) {
                callTrace.add("getExpectedArtifactIdSuffix");
                return super.getExpectedArtifactIdSuffix(type);
            }
        };
    }

    private void verifyInvalidParentPom(final AbstractPomAnalyzer unitUnderTest, final Model invalidParent) {

        try {
            unitUnderTest.verifyParentPom(getParentModel(true), invalidParent);
            Assert.fail("InvalidStructureException expected");
        } catch (InvalidStructureException e) {
            // Do nothing.
        } catch (Exception e) {
            Assert.fail("Expected InvalidStructureException, but got [" + e.getClass().getSimpleName() + "]");
        }
    }

    private Model getParentModel(final boolean apiParentPom) {
        final String relativePath = "foo/poms/" + (apiParentPom ? "foo-api-parent/pom.xml" : "foo-parent/pom.xml");
        final File fooApiParentPomFile = getTestDataFile(relativePath);
        return getPomModel(fooApiParentPomFile);
    }
}
