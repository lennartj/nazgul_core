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
package se.jguru.nazgul.core.quickstart.api.generator;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.FileUtilsTest;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.helpers.TestProjectFactory;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractProjectFactoryTest {

    // Shared state
    // private StructureNavigator structureNavigator;
    private NamingStrategy namingStrategy;
    private File testDataDir;

    @Before
    public void setupSharedState() {

        namingStrategy = new TestNamingStrategy(false);
        final URL testdata = getClass().getClassLoader().getResource("testdata");
        testDataDir = new File(testdata.getPath());
        // structureNavigator = new DefaultStructureNavigator(namingStrategy, new TestPatternPomAnalyzer());

        Assert.assertTrue(testDataDir.exists() && testDataDir.isDirectory());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamingStrategy() {

        // Act & Assert
        new TestProjectFactory(null);
    }

    @Test
    public void validateCreatingProjectData() {

        // Assemble
        final TestProjectFactory unitUnderTest = new TestProjectFactory(namingStrategy);

        final SimpleArtifact reactorParent = new SimpleArtifact(
                "se.jguru.nazgul.tools.poms.external",
                "nazgul-tools-external-reactor-parent",
                "4.0.0");

        final SimpleArtifact parentParent = new SimpleArtifact(
                "se.jguru.nazgul.core.poms.core-parent",
                "nazgul-core-parent",
                "1.6.1");

        // Act
        final Project projectDefinition = unitUnderTest.createProjectDefinition(
                "nazgul", "blah", reactorParent, parentParent);

        // Assert
        Assert.assertNotNull(projectDefinition);
        Assert.assertEquals("blah", projectDefinition.getName());
        Assert.assertEquals("nazgul", projectDefinition.getPrefix());
        Assert.assertSame(parentParent, projectDefinition.getParentParent());
        Assert.assertSame(reactorParent, projectDefinition.getReactorParent());
    }

    @Test
    public void validateCreatingProjectSkeleton() {

        // Assemble
        final File rootDir = FileUtilsTest.createUniqueDirectoryUnderTestData("createProjectData");
        final TestProjectFactory unitUnderTest = new TestProjectFactory(namingStrategy);

        final SimpleArtifact reactorParent = new SimpleArtifact(
                "se.jguru.nazgul.tools.poms.external",
                "nazgul-tools-external-reactor-parent",
                "4.0.0");

        final SimpleArtifact parentParent = new SimpleArtifact(
                "se.jguru.nazgul.core.poms.core-parent",
                "nazgul-core-parent",
                "1.6.1");

        final Project projectDefinition = unitUnderTest.createProjectDefinition(
                "acme", "blah", reactorParent, parentParent);

        // Act
        final boolean created = unitUnderTest.createProject(
                rootDir,
                projectDefinition,
                "se.jguru",
                "1.0.0-SNAPSHOT",
                "1.0.0-SNAPSHOT");

        // Assert
        Assert.assertTrue(created);

        final File reactorRoot = new File(rootDir, projectDefinition.getName());
        validateDirectory(reactorRoot, PomType.ROOT_REACTOR);
        validateDirectory(new File(reactorRoot, "poms"), PomType.REACTOR);
        validateDirectory(new File(reactorRoot, "poms/blah-parent"), PomType.PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-api-parent"), PomType.API_PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-model-parent"), PomType.MODEL_PARENT);
        validateDirectory(new File(reactorRoot, "poms/blah-war-parent"), PomType.WAR_PARENT);

        System.out.println("Got calltrace:\n" + unitUnderTest.callTrace);

        // [
        //  createProjectDefinition(nazgul,
        //      blah,
        //      se.jguru.nazgul.tools.poms.external/nazgul-tools-external-reactor-parent/4.0.0,
        //      se.jguru.nazgul.core.poms.core-parent/nazgul-core-parent/1.6.1),
        //
        //  createProject,
        //  getDirectoryName(blah, ROOT_REACTOR, nazgul),
        //  getTemplateResource(ROOT_REACTOR/pom.xml),
        //  getTemplateResourceURL(ROOT_REACTOR/pom.xml),
        //  getDirectoryName(blah, REACTOR, nazgul),
        //  getTemplateResource(REACTOR/pom.xml),
        //  getTemplateResourceURL(REACTOR/pom.xml),
        //  getDirectoryName(blah, PARENT, nazgul),
        //  getTemplateResource(PARENT/pom.xml),
        //  getTemplateResourceURL(PARENT/pom.xml),
        //  getDirectoryName(blah, API_PARENT, nazgul),
        //  getTemplateResource(API_PARENT/pom.xml),
        //  getTemplateResourceURL(API_PARENT/pom.xml),
        //  getDirectoryName(blah, MODEL_PARENT, nazgul),
        //  getTemplateResource(MODEL_PARENT/pom.xml),
        //  getTemplateResourceURL(MODEL_PARENT/pom.xml),
        //  getDirectoryName(blah, WAR_PARENT, nazgul),
        //  getTemplateResource(WAR_PARENT/pom.xml),
        //  getTemplateResourceURL(WAR_PARENT/pom.xml)
        // ]

        /*
        Assert.assertEquals("[] ==> [ROOT_REACTOR]", unitUnderTest.callTrace.get(0));
        Assert.assertEquals("[poms] ==> [REACTOR]", unitUnderTest.callTrace.get(1));
        Assert.assertEquals("[poms] ==> [PARENT]", unitUnderTest.callTrace.get(2));
        Assert.assertEquals("[poms] ==> [API_PARENT]", unitUnderTest.callTrace.get(3));
        Assert.assertEquals("[poms] ==> [MODEL_PARENT]", unitUnderTest.callTrace.get(4));
        Assert.assertEquals("[poms] ==> [WAR_PARENT]", unitUnderTest.callTrace.get(5));
        */
    }

    //
    // Private helpers
    //

    private void validateDirectory(final File dir, final PomType pomType) {
        Assert.assertTrue("Required directory [" + dir.getAbsolutePath() + "] was not found.",
                dir.exists() && dir.isDirectory());

        final File pomFile = new File(dir, "pom.xml");
        Assert.assertTrue("Required POM file [" + pomFile.getAbsolutePath() + "] was not found.",
                pomFile.exists() && pomFile.isFile());

        // The TestPomFactory should have generated POM file data holding the PomType in text.
        // Validate that the correct pom is written to the correct location.
        final Model pomModel = FileUtils.getPomModel(pomFile);
        Assert.assertEquals(pomType, namingStrategy.getPomType(pomModel));
    }
}
