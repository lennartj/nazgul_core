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
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractPomAnalyzerTest extends AbstractMavenModelTest {

    // Shared state
    private List<String> callTrace;
    private NamingStrategy testNamingStrategy;

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
        for(int i = 0; i < expectedCallStructure.size(); i++) {
            Assert.assertEquals(expectedCallStructure.get(i), callTrace.get(i));
        }
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
}
