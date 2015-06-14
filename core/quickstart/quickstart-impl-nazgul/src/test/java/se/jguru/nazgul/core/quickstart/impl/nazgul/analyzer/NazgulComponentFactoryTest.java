/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.generator.ComponentFactory;
import se.jguru.nazgul.core.quickstart.api.generator.ProjectFactory;
import se.jguru.nazgul.core.quickstart.api.generator.SoftwareComponentPart;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulComponentFactoryTest {

    @Rule
    public TestName testName = new TestName();

    // Shared state
    private File testDataDirectory;
    private ProjectFactory projectFactory;

    private SimpleArtifact reactorParent = new SimpleArtifact(
            "se.jguru.nazgul.tools.poms.external",
            "nazgul-tools-external-reactor-parent",
            "4.0.0");
    private SimpleArtifact parentParent = new SimpleArtifact(
            "se.jguru.nazgul.core.poms.core-parent",
            "nazgul-core-parent",
            "1.6.1");

    @Before
    public void setupSharedState() {

        final String methodName = testName.getMethodName();
        testDataDirectory = createUniqueDirectoryUnderTestData(methodName);

        projectFactory = new NazgulProjectFactory();
    }

    @Test
    public void validateCreatingStandardNazgulComponent() {

        // Assemble
        final String projectName = "foo";
        final ComponentFactory unitUnderTest = new NazgulComponentFactory();
        final String componentName = "blah";
        final File componentDirectory = new File(testDataDirectory, projectName + "/" + componentName);

        final SortedMap<SoftwareComponentPart, String> parts2SuffixMap = new TreeMap<>();
        parts2SuffixMap.put(SoftwareComponentPart.MODEL, "pojo");
        parts2SuffixMap.put(SoftwareComponentPart.API, "irrelevant");

        final Project project = projectFactory.createProjectDefinition("nazgul", projectName, reactorParent, parentParent);
        final boolean success = projectFactory.createProject(
                testDataDirectory,
                project,
                "se.jguru",
                reactorParent.getMavenVersion(),
                parentParent.getMavenVersion());

        // Act
        unitUnderTest.createSoftwareComponent(componentDirectory, parts2SuffixMap);

        // Assert
        final String expectedModelDirName = "nazgul-foo-blah-model";
        final String expectedApiDirName = "nazgul-foo-blah-api";

        Assert.assertTrue(success);
        Assert.assertTrue(componentDirectory.exists());
        Assert.assertTrue(componentDirectory.isDirectory());

        final File reactorPomFile = new File(componentDirectory, "pom.xml");
        final File modelPomFile = new File(componentDirectory, expectedModelDirName + "/pom.xml");
        final File apiPomFile = new File(componentDirectory, expectedApiDirName + "/pom.xml");

        Assert.assertTrue(reactorPomFile.exists() && reactorPomFile.isFile());
        Assert.assertTrue(modelPomFile.exists() && modelPomFile.isFile());
        Assert.assertTrue(apiPomFile.exists() && apiPomFile.isFile());

        final Model reactorPomModel = FileUtils.getPomModel(reactorPomFile);
        final Model modelPomModel = FileUtils.getPomModel(modelPomFile);
        final Model apiPomModel = FileUtils.getPomModel(apiPomFile);

        final List<String> modules = reactorPomModel.getModules();
        Assert.assertEquals(2, modules.size());
        Assert.assertTrue(modules.contains(expectedModelDirName));
        Assert.assertTrue(modules.contains(expectedApiDirName));

        Assert.assertEquals("se.jguru.nazgul.foo.blah.api", apiPomModel.getGroupId());
        Assert.assertEquals("nazgul-foo-blah-api", apiPomModel.getArtifactId());

        Assert.assertEquals("se.jguru.nazgul.foo.blah.model", modelPomModel.getGroupId());
        Assert.assertEquals("nazgul-foo-blah-model", modelPomModel.getArtifactId());
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
