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

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.resource.api.extractor.JarExtractor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Abstract ComponentFactory implementation sporting utility methods and
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractComponentFactory extends AbstractFactory implements ComponentFactory {

    /**
     * A DateTimeFormatter on the format "20140213_052652".
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat
            .forPattern("yyyyMMdd_HHmmss")
            .withLocale(Locale.ENGLISH);

    // Internal state
    private StructureNavigator navigator;

    /**
     * Creates a new AbstractComponentFactory wrapping the supplied NamingStrategy.
     *
     * @param structureNavigator A StructureNavigator used to navigate the project in which this
     *                           AbstractComponentFactory should create Software Components or SoftwareComponentParts.
     * @param namingStrategy     The active NamingStrategy, used to validate supplied data.
     */
    protected AbstractComponentFactory(final NamingStrategy namingStrategy,
                                       final StructureNavigator structureNavigator) {
        super(namingStrategy);

        // Assign internal state
        Validate.notNull(structureNavigator, "Cannot handle null structureNavigator argument.");
        this.navigator = structureNavigator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSoftwareComponent(final File componentDirectory,
                                        final SortedMap<SoftwareComponentPart, String> parts2SuffixMap)
            throws InvalidStructureException {

        // Check sanity
        Validate.notNull(componentDirectory, "Cannot handle null componentDirectory argument.");
        Validate.notEmpty(parts2SuffixMap, "Cannot handle null or empty parts2SuffixMap argument.");

        // Check sanity
        final Set<SoftwareComponentPart> parts = parts2SuffixMap.keySet();
        if (parts.contains(SoftwareComponentPart.IMPLEMENTATION)
                && !(parts.contains(SoftwareComponentPart.API) || parts.contains(SoftwareComponentPart.SPI))) {
            throw new InvalidStructureException("Software components containing an Implementation project should"
                    + "contain either an API or SPI project as well.");
        }
        for (Map.Entry<SoftwareComponentPart, String> current : parts2SuffixMap.entrySet()) {
            final String suffix = current.getValue();
            if (current.getKey().isSuffixRequired()) {
                Validate.notEmpty(suffix, "A suffix is required for SoftwareComponentPart [" + current.getKey().name()
                        + "], but none was given.");
            }
        }

        // Create the software component's directory.
        final File rootDir = navigator.getProjectRootDirectory(componentDirectory);
        final String relativePath = navigator.getRelativePath(componentDirectory, false);

        File componentDir = componentDirectory;
        if (!FileUtils.exists(componentDir, true)) {
            componentDir = FileUtils.makeDirectory(rootDir, relativePath);
        }
        Validate.isTrue(FileUtils.exists(componentDir, true), "Could not create directory ["
                + FileUtils.getCanonicalPath(componentDir) + "]");

        // Create each software component part project.
        for (Map.Entry<SoftwareComponentPart, String> current : parts2SuffixMap.entrySet()) {
            addSoftwareComponentPart(componentDir, current.getKey(), current.getValue());
        }

        // Synthesize the Software Component's reactor POM.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSoftwareComponentPart(final File componentReactorDirectory,
                                         final SoftwareComponentPart toAdd,
                                         final String suffix) throws InvalidStructureException {

        // Check sanity
        Validate.notNull(componentReactorDirectory, "Cannot handle null componentDirectory argument.");
        Validate.notNull(toAdd, "Cannot handle null toAdd argument.");
        if (toAdd.isSuffixRequired()) {
            Validate.notEmpty(suffix,
                    "Cannot handle null or empty suffix argument for SoftwareComponentPart [" + toAdd + "].");
        }
        Validate.isTrue(componentReactorDirectory.exists() && componentReactorDirectory.isDirectory(),
                "Software Component Directory [" + FileUtils.getCanonicalPath(componentReactorDirectory)
                        + "] was not an existing directory.");

        // Proceed to create the structures as instructed.
        final File projectRootDirectory = navigator.getProjectRootDirectory(componentReactorDirectory);
        final String relativePath = navigator.getRelativePath(componentReactorDirectory, false);
        final Model rootReactorPomModel = FileUtils.getPomModel(new File(projectRootDirectory, "pom.xml"));
        final Name rootReactorName = getNamingStrategy().createName(rootReactorPomModel);

        final File parentPomDir = navigator.getParentPomDirectory(componentReactorDirectory);
        final Model parentPomModel = FileUtils.getPomModel(new File(parentPomDir, "pom.xml"));

        File componentDir = componentReactorDirectory;
        if (!FileUtils.exists(componentDir, true)) {
            componentDir = FileUtils.makeDirectory(projectRootDirectory, relativePath);
        }

        // Create the part directory
        final String prefix = getNamingStrategy().isPrefixRequiredOnAllFolders() ? rootReactorName.getPrefix() : "";
        final Name partName = toAdd.createName(prefix, toAdd.getType(), suffix);
        final File partDir = FileUtils.makeDirectory(componentDir, partName.toString());

        // Synthesize the Project data.
        final Project projectData = new Project(
                rootReactorName.getPrefix(),
                rootReactorName.getName(),
                FileUtils.getSimpleArtifact(rootReactorPomModel),
                FileUtils.getSimpleArtifact(parentPomModel));

        // Create the part POM, and write it appropriately.
        final String partPomData = synthesizeResource(
                relativePath + StructureNavigator.DIRECTORY_SEPARATOR + partDir.getName(),
                toAdd.getComponentPomType(),
                projectData);
        final File componentPomFile = new File(partDir, "pom.xml");
        FileUtils.writeFile(componentPomFile, partPomData);

        // Add the standard project structure around the pom itself.
        final Model componentPomModel = FileUtils.getPomModel(componentPomFile);
        createMavenProject(componentDir, componentPomModel.getGroupId(), "foo");
    }

    /**
     * Adds the standard project structure for a component project (which should typically be created within the
     * supplied componentProjectDirectory).
     *
     * @param projectParentDirectory The directory within the VCS where the Maven project should be created.
     * @param projectTopPackage      The top package of the Maven project to create. This could be identical to the
     *                               groupId of the Maven project itself. Separated by dots,
     *                               this parameter typically has the structure {@code se.jguru.nazgul.foo.bar} or
     *                               equivalent.
     * @param templateResourcePath   The resource path to the template directory. Should <strong>not</strong> start
     *                               with {@code /}, and therefore typically be on the form
     *                               {@code my/quickstart/templates/COMPONENT_MODEL} or equivalent.
     */
    protected void createMavenProject(final File projectParentDirectory,
                                      final String projectTopPackage,
                                      final String templateResourcePath) {

        // 0) Find an empty temporary file area.
        File tmpExtractedFilesRoot;
        for (int i = 0; true; i++) {
            tmpExtractedFilesRoot = new File(System.getProperty("java.io.tmpdir"),
                    "template/" + projectParentDirectory.getName() + "_" + i);
            if (!tmpExtractedFilesRoot.exists()) {
                tmpExtractedFilesRoot.mkdirs();
                break;
            }
        }

        // Resources:
        //
        //      /a/path/theJar.jar!/templates/standard/component_api/pom.xml
        //      /a/path/theJar.jar!/templates/standard/component_api/src/main/java/__groupId__/FooBar.java
        //      /a/path/theJar.jar!/templates/standard/component_api/src/test/java/__groupId__/FooBar.java
        //      /a/path/theJar.jar!/templates/standard/component_api/src/test/java/__groupId__/FooBar.java
        //

        // 1) Find the JarFile or File (i.e. directory) where the templateRootUrl points to.
        final URL templateRootURL = getUrlFor(templateResourcePath);
        final String protocol = templateRootURL.getProtocol();

        // Update the error message if other resource URL protocols are supported.
        if ("jar".equalsIgnoreCase(protocol)) {

            // Find the template JarFile, and extrat all relevant templates to a temporary directory.
            final JarFile templateJarFile = JarExtractor.getJarFileFor(templateRootURL);
            JarExtractor.extractResourcesFrom(
                    templateJarFile,
                    Pattern.compile(templateResourcePath + "/.*"),
                    tmpExtractedFilesRoot,
                    false);

            // Tokenize each extracted template, and move it to the correct location.
            Map<String, File>

        } else if ("file".equalsIgnoreCase(protocol)) {

        } else {
            throw new IllegalStateException("Cannot handle resource URL [" + templateRootURL.toString()
                    + "]. Supported protocols are ('jar', 'file').");
        }

        // JarExtractor.extractResourcesFrom()

        // 3) Tokenize and move each file.


        // Create the following structure.
        //
        // src/main/java/[package for project]
        // src/test/java/[package for project]
        // src/test/resources/logback-test.xml
        // src/test/resources/testdata

        final File sourceDir = FileUtils.makeDirectory(projectParentDirectory, "src/main/java/" + projectTopPackage);
        final File testDir = FileUtils.makeDirectory(projectParentDirectory, "src/test/java/" + projectTopPackage);
        final File testdataDirectory = new File(projectParentDirectory, "src/test/resources/testdata");

        // First off, create the logback-test.xml file.
        FileUtils.readFile(templateResourcePath + "src/test/resources/logback-test.xml");
        FileUtils.writeFile(new File(testdataDirectory, "logback-test.xml"), );
    }

    //
    // Private helpers
    //

    private URL getUrlFor(final String templateResourcePath) {

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final List<URL> resourceURLs;
        try {
            resourceURLs = Collections.list(contextClassLoader.getResources(templateResourcePath));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not extract URLs from TemplateResourcePath ["
                    + templateResourcePath + "]", e);
        }

        if (resourceURLs.size() > 1) {
            final StringBuilder builder = new StringBuilder("Found [" + resourceURLs.size()
                    + "] resource URLs corresponding to templateResourcePath [" + templateResourcePath
                    + "]. Expected exactly 1 match, but got the following URLs:\n");

            for (int i = 0; i < resourceURLs.size(); i++) {
                builder.append("[" + i + "]: " + resourceURLs.get(i) + "\n");
            }
            throw new IllegalArgumentException(builder.toString());
        } else if (resourceURLs.size() == 0) {
            throw new IllegalArgumentException("templateResourcePath [" + templateResourcePath
                    + "] was not found in thread context ClassLoader. "
                    + "This implies that the project templates cannot be found, which is a configuration error.");
        }

        // We have exactly one templateRootUrl.
        return resourceURLs.get(0);
    }
}
