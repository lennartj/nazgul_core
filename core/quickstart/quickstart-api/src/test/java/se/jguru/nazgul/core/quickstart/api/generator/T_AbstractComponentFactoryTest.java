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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.quickstart.api.FileTestUtilities;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.FileUtilsTest;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.helpers.TestNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.helpers.TestComponentFactory;
import se.jguru.nazgul.core.quickstart.api.generator.helpers.TestProjectFactory;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractComponentFactoryTest {

    // Shared state
    private SortedMap<SoftwareComponentPart, String> parts2SuffixMap;
    private TestProjectFactory projectFactory;
    private ComponentFactory unitUnderTest;
    private NamingStrategy namingStrategy;
    private File factoryRootDir;
    private File tmpTmpIoFileDir;

    private SimpleArtifact reactorParent = new SimpleArtifact(
            "se.jguru.nazgul.tools.poms.external",
            "nazgul-tools-external-reactor-parent",
            "4.0.0");
    private SimpleArtifact parentParent = new SimpleArtifact(
            "se.jguru.nazgul.core.poms.core-parent",
            "nazgul-core-parent",
            "1.6.1");
    private Project project;

    @Before
    public void setupSharedState() {

        // Redirect the java.io.tmpdir
        tmpTmpIoFileDir = FileTestUtilities.createTmpDirectory(true);

        // Create a ProjectFactory to generate the skeleton project.
        namingStrategy = new TestNamingStrategy(false);
        projectFactory = new TestProjectFactory(namingStrategy);

        // Create the directory structure to use, and the component Factory.
        factoryRootDir = FileUtilsTest.createUniqueDirectoryUnderTestData("componentFactoryRoot");
        Assert.assertTrue(FileUtils.DIRECTORY_FILTER.accept(factoryRootDir));
        unitUnderTest = new TestComponentFactory(namingStrategy);

        parts2SuffixMap = new TreeMap<>();
        parts2SuffixMap.put(SoftwareComponentPart.MODEL, null);
        parts2SuffixMap.put(SoftwareComponentPart.API, null);
        parts2SuffixMap.put(SoftwareComponentPart.SPI, "foobar");
        parts2SuffixMap.put(SoftwareComponentPart.IMPLEMENTATION, "blah");

        // Create the project skeleton structure.
        project = projectFactory.createProjectDefinition(
                "gnat",
                "foo",
                reactorParent,
                parentParent);

        final boolean created = projectFactory.createProject(
                factoryRootDir,
                project,
                "org.acme",
                "1.0.0-SNAPSHOT",
                "1.0.0-SNAPSHOT");
        Assert.assertTrue("Could not create project structure skeleton in ["
                + FileUtils.getCanonicalPath(factoryRootDir) + "]", created);

        // Validate the groupId of the poms reactor pom.
        final Model pomsReactorModel = FileUtils.getPomModel(
                new File(factoryRootDir, project.getName() + "/poms/pom.xml"));

        final String groupId = pomsReactorModel.getGroupId();
        Assert.assertTrue("Got incorrect groupId for poms reactor POM [" + groupId + "]", groupId.endsWith("poms"));
    }

    @After
    public void restoreSharedState() {
        FileTestUtilities.restoreOriginalTmpDirectory();
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullComponentDirectory() {

        // Act & Assert
        unitUnderTest.createSoftwareComponent(null, parts2SuffixMap);
    }

    @Test(expected = InvalidStructureException.class)
    public void validateExceptionOnInconsistentPartsMap() {

        // Assemble
        final File compDirectory = FileUtils.makeDirectory(factoryRootDir, "incorrectComponent");
        parts2SuffixMap.remove(SoftwareComponentPart.API);
        parts2SuffixMap.remove(SoftwareComponentPart.SPI);

        // Act & Assert
        unitUnderTest.createSoftwareComponent(compDirectory, parts2SuffixMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnMissingRequiredProjectSuffix() {

        // Assemble
        final File compDirectory = FileUtils.makeDirectory(factoryRootDir, "incorrectComponent");
        parts2SuffixMap.put(SoftwareComponentPart.SPI, "");

        // Act & Assert
        unitUnderTest.createSoftwareComponent(compDirectory, parts2SuffixMap);
    }

    @Test
    public void validateCreatingSoftwareComponent() {

        // Assemble
        final String componentName = "messaging";
        final File componentDir = new File(factoryRootDir, project.getName() + "/" + componentName);

        // Act
        unitUnderTest.createSoftwareComponent(componentDir, parts2SuffixMap);

        // Assert
        final SortedMap<String, File> relPath2FileMap = FileUtils.listFilesRecursively(componentDir);

        // Generated reactor POM validation
        validatePomModelFile("pom.xml",
                relPath2FileMap.get("pom.xml"),
                "org.acme.gnat.foo",
                "gnat-foo-reactor",
                "1.0.0-SNAPSHOT",
                "org.acme.gnat.foo.messaging",
                "gnat-foo-messaging-reactor",
                "messaging");

        // Generated model POM validation
        validatePomModelFile("gnat-foo-messaging-model/pom.xml",
                relPath2FileMap.get("gnat-foo-messaging-model/pom.xml"),
                "org.acme.gnat.foo.poms.gnat-foo-model-parent",
                "gnat-foo-model-parent",
                "1.0.0-SNAPSHOT",
                "org.acme.gnat.foo.messaging.model",
                "gnat-foo-messaging-model",
                "messaging/gnat-foo-messaging-model");

        // Generated api POM validation
        validatePomModelFile("gnat-foo-messaging-api/pom.xml",
                relPath2FileMap.get("gnat-foo-messaging-api/pom.xml"),
                "org.acme.gnat.foo.poms.gnat-foo-api-parent",
                "gnat-foo-api-parent",
                "1.0.0-SNAPSHOT",
                "org.acme.gnat.foo.messaging.api",
                "gnat-foo-messaging-api",
                "messaging/gnat-foo-messaging-api");

        // Generated 'foobar' spi POM validation
        validatePomModelFile("gnat-foo-messaging-spi-foobar/pom.xml",
                relPath2FileMap.get("gnat-foo-messaging-spi-foobar/pom.xml"),
                "org.acme.gnat.foo.poms.gnat-foo-api-parent",
                "gnat-foo-api-parent",
                "1.0.0-SNAPSHOT",
                "org.acme.gnat.foo.messaging.spi.foobar",
                "gnat-foo-messaging-spi-foobar",
                "messaging/gnat-foo-messaging-spi-foobar");

        // Generated 'blah' implementation POM validation
        validatePomModelFile("gnat-foo-messaging-impl-blah/pom.xml",
                relPath2FileMap.get("gnat-foo-messaging-impl-blah/pom.xml"),
                "org.acme.gnat.foo.poms.gnat-foo-parent",
                "gnat-foo-parent",
                "1.0.0-SNAPSHOT",
                "org.acme.gnat.foo.messaging.implementation.blah",
                "gnat-foo-messaging-impl-blah",
                "messaging/gnat-foo-messaging-impl-blah");
    }

    //
    // Private helpers
    //

    private void validatePomModelFile(final String relativePath,
                                      final File pomFile,
                                      final String parentGroupId,
                                      final String parentArtifactId,
                                      final String parentMavenVersion,
                                      final String groupId,
                                      final String artifactId,
                                      final String relativePathInReactor) {

        // Find the Maven Model, and compare it with the required data.
        final Model pomModel = FileUtils.getPomModel(pomFile);

        validatePomModelData(relativePath, "parentGroupId", parentGroupId, pomModel.getParent().getGroupId());
        validatePomModelData(relativePath, "parentArtifactId", parentArtifactId, pomModel.getParent().getArtifactId());
        validatePomModelData(relativePath, "parentMavenVersion", parentMavenVersion, pomModel.getParent().getVersion());
        validatePomModelData(relativePath, "groupId", groupId, pomModel.getGroupId());
        validatePomModelData(relativePath, "artifactId", artifactId, pomModel.getArtifactId());

        final String actualPathInReactor = pomModel.getProperties().getProperty("path.in.reactor");
        validatePomModelData(relativePath, "relativePathInReactor", relativePathInReactor, actualPathInReactor);
    }

    private void validatePomModelData(final String relativePathToPom,
                                      final String criterion,
                                      final String required,
                                      final String actual) {

        final String errorMessage = "Incorrect '" + criterion + "' in POM [" + relativePathToPom
                + "].\n\t Expected [" + required + "], but got [" + actual + "]";
        Assert.assertEquals(errorMessage, required, actual);
    }
}
