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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.StructureNavigator;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.parser.PomToken;
import se.jguru.nazgul.core.quickstart.api.generator.parser.SingleBracketPomTokenParserFactory;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.resource.api.extractor.JarExtractor;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Abstract ComponentFactory implementation sporting utility methods and VCS/Project navigation facilities.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractComponentFactory extends AbstractFactory implements ComponentFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractComponentFactory.class.getName());

    // Internal state
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
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
    public final void createSoftwareComponent(final File componentDirectory,
                                              final SortedMap<SoftwareComponentPart, String> parts2SuffixMap)
            throws InvalidStructureException {

        // Check sanity
        Validate.notNull(componentDirectory, "Cannot handle null componentDirectory argument.");
        Validate.notEmpty(parts2SuffixMap, "Cannot handle null or empty parts2SuffixMap argument.");
        Validate.isTrue(!FileUtils.FILE_FILTER.accept(componentDirectory),
                "ComponentDirectory [" + FileUtils.getCanonicalPath(componentDirectory)
                        + "] must not refer to an existing file.");
        Validate.isTrue(FileUtils.NONEXISTENT_OR_EMPTY_DIRECTORY_FILTER.accept(componentDirectory),
                "ComponentDirectory [" + FileUtils.getCanonicalPath(componentDirectory)
                        + "] must refer to a nonexistent or empty directory.");

        // Check structure of the supplied arguments
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

        // Validate that we should be able to create a component here.
        final File componentPomFile = new File(componentDirectory, "pom.xml");
        if (FileUtils.FILE_FILTER.accept(componentPomFile)) {

            // The componentPomFile must be either of type REACTOR or ROOT_REACTOR.
            final Model pomModel = FileUtils.getPomModel(componentPomFile);
            final PomType pomType = getNamingStrategy().getPomType(pomModel);

            Validate.isTrue(pomType == PomType.REACTOR || pomType == PomType.ROOT_REACTOR,
                    "ComponentPart projects can only be added to components. This implies that ["
                            + FileUtils.getCanonicalPath(componentPomFile)
                            + "] must either be of type REACTOR or ROOT_REACTOR. (Detected type [" + pomType + "])");
        }

        // TODO: Ensure that existing parent directories contain REACTOR or ROOT_REACTOR poms.
        final File originatingDirectory = componentPomFile.exists()
                ? componentDirectory
                : componentDirectory.getParentFile();
        final File rootDir = navigator.getProjectRootDirectory(originatingDirectory);
        final String relativePath = navigator.getRelativePath(componentDirectory, false);

        final List<String> pathSegments = new ArrayList<>();
        if (relativePath != null) {
            final StringTokenizer tok = new StringTokenizer(relativePath, File.separator, false);
            while (tok.hasMoreTokens()) {
                pathSegments.add(tok.nextToken());
            }
        }

        File tmp = new File(rootDir, "pom.xml");
        for (String current : pathSegments) {
            if (FileUtils.FILE_FILTER.accept(tmp)) {

                if (log.isDebugEnabled()) {
                    log.debug("Validating that [" + FileUtils.getCanonicalPath(tmp)
                            + "] is a REACTOR or ROOT_REACTOR pom to enable creating a SoftwareComponent.");
                }

                // The current POM should be of the correct type.
                final Model pomModel = FileUtils.getPomModel(tmp);
                final PomType pomType = getNamingStrategy().getPomType(pomModel);

                Validate.isTrue(pomType == PomType.REACTOR || pomType == PomType.ROOT_REACTOR,
                        "ComponentPart projects can only be added to components. This implies that ["
                                + FileUtils.getCanonicalPath(tmp) + "] must either be of type REACTOR "
                                + "or ROOT_REACTOR. (Detected type [" + pomType + "])");

                // Descend
                tmp = new File(tmp, current + "/pom.xml");
            } else {

                // No point in going deeper.
                break;
            }
        }

        // Sane to create the SoftwareComponentPart
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

        // Now, create or update the reactor POM for the software component.
        addSoftwareComponentPart(componentDir, SoftwareComponentPart.REACTOR, null);
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

        // Synthesize the Project data.
        final Project projectData = new Project(
                rootReactorName.getPrefix(),
                rootReactorName.getName(),
                FileUtils.getSimpleArtifact(rootReactorPomModel),
                FileUtils.getSimpleArtifact(parentPomModel));

        //
        // Find the name of the part directory, and then create it.
        // The directory name here is something like "messaging-model", or
        // "foobar-messaging-model" depending on the desired prefix structure.
        //
        final String dirPrefix = rootReactorName.getPrefix() != null && !"".equals(rootReactorName.getPrefix())
                ? rootReactorName.getPrefix() + Name.DEFAULT_SEPARATOR
                : "";
        final String dirSuffix = toAdd.isSuffixRequired() ? Name.DEFAULT_SEPARATOR + suffix : "";
        final String dirName = dirPrefix + rootReactorName.getName() + dirSuffix;

        final File partDir = FileUtils.makeDirectory(componentDir, dirName);
        final String topReactorPomVersion = rootReactorPomModel.getVersion() == null
                ? projectData.getReactorParent().getMavenVersion()
                : rootReactorPomModel.getVersion();
        final String parentPomVersion = parentPomModel.getVersion() == null
                ? projectData.getParentParent().getMavenVersion()
                : parentPomModel.getVersion();

        // Create a Maven project for the given PomType within PartDir
        createMavenProject(toAdd,
                partDir,
                topReactorPomVersion,
                parentPomVersion,
                projectData,
                getProjectGroupIdPrefix(rootReactorPomModel.getGroupId(), rootReactorPomModel.getArtifactId()),
                suffix);
    }

    /**
     * Creates a maven project of the supplied type within the mavenProjectDir.
     *
     * @param part                   The non-null SoftwareComponentPart indicating which type of project should be created.
     * @param mavenProjectDir        The directory where the maven project should be generated.
     *                               Must exist and be a directory.
     * @param reactorPomMavenVersion The non-empty Maven project version of reactor POMs within the project.
     *                               Should be identical to the version of the reactor parent POM.
     * @param parentPomMavenVersion  The non-empty Maven project version of parent POMs within the project (and,
     *                               normally, the version of all leaf projects within the Maven project reactor).
     *                               Should be identical to the version of the parent parent POM.
     * @param project                The non-null Project data, primarily used to initialize the active TokenParser.
     * @param optionalGroupIdPrefix  The groupId prefix (commonly the reverse DNS, similar to {@code se.jguru} or
     *                               {@code org.apache}) of the organisation that owns the project created.
     *                               The groupId prefix is prepended to the groupId of the created Maven project.
     * @param partSuffix             The suffix of the project SoftwareComponentPart project,
     *                               used only if {@code SoftwareComponentPart.isSuffixRequired()} yields {@code true}.
     */
    @SuppressWarnings("All")
    protected void createMavenProject(final SoftwareComponentPart part,
                                      final File mavenProjectDir,
                                      final String reactorPomMavenVersion,
                                      final String parentPomMavenVersion,
                                      final Project project,
                                      final String optionalGroupIdPrefix,
                                      final String partSuffix) {

        // Check sanity
        Validate.notNull(part, "Cannot handle null part argument.");
        Validate.notNull(mavenProjectDir, "Cannot handle null mavenProjectDir argument.");
        Validate.isTrue(mavenProjectDir.exists() && mavenProjectDir.isDirectory(), "Maven project directory ["
                + FileUtils.getCanonicalPath(mavenProjectDir) + "] must exist and be a directory.");
        Validate.notEmpty(reactorPomMavenVersion, "Cannot handle null or empty reactorPomMavenVersion argument.");
        Validate.notEmpty(parentPomMavenVersion, "Cannot handle null or empty parentPomMavenVersion argument.");

        // 1) Extract all template files corresponding to the SoftwareComponentPart to a temporary directory
        final File tmpExtractedFilesRoot = extractTemplateFiles(
                mavenProjectDir.getName(),
                part.getComponentPomType().name());
        if (log.isDebugEnabled()) {
            log.debug("Using temporary directory [" + FileUtils.getCanonicalPath(tmpExtractedFilesRoot)
                    + "] to extract template files.");
        }

        // 2) Create a TokenParser to handle template tokens
        final SingleBracketPomTokenParserFactory.Builder builder = SingleBracketPomTokenParserFactory
                .create(part.getComponentPomType(), project)
                .withoutProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath(navigator.getRelativePath(mavenProjectDir, false))
                .withProjectGroupIdPrefix(optionalGroupIdPrefix)
                .withProjectSuffix(partSuffix)
                .withMavenVersions(reactorPomMavenVersion, parentPomMavenVersion);

        if (part.getComponentPomType() == PomType.REACTOR || part.getComponentPomType() == PomType.ROOT_REACTOR){

            // Add or update the modules
            final StringBuilder modulesElementBuilder = new StringBuilder();
            for (File current : mavenProjectDir.listFiles(FileUtils.MODULE_NAME_FILTER)) {
                modulesElementBuilder.append("<module>").append(current.getName()).append("</module>\n");
            }

            builder.addToken(PomToken.MODULES.getToken(), modulesElementBuilder.toString());
        }
        final TokenParser tokenParser = builder.build();

        // 3) Tokenize (as appropriate) and move each file to its destination.
        final FileFilter shouldTokenizeFilter = getShouldTokenizeFilter();
        final SortedMap<String, File> path2FileMap = FileUtils.listFilesRecursively(tmpExtractedFilesRoot);
        for (Map.Entry<String, File> current : path2FileMap.entrySet()) {

            // Tokenize the relative path, and the file data if applicable.
            final String relativePath = tokenParser.substituteTokens(current.getKey());
            final String data = shouldTokenizeFilter.accept(current.getValue())
                    ? tokenParser.substituteTokens(FileUtils.readFile(current.getValue()))
                    : FileUtils.readFile(current.getValue());

            final File toWrite = new File(mavenProjectDir, relativePath);
            final File parentFile = toWrite.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            // Finally, write the file.
            FileUtils.writeFile(toWrite, data);
        }
    }

    /*
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

    protected void createMavenProject(final Project project,
                                      final File projectParentDirectory,
                                      final String projectTopPackage,
                                      final String projectDirectoryName,
                                      final String templateResourcePath,
                                      final TokenParser tokenParser) {

        // Check sanity
        Validate.notNull(projectParentDirectory, "Cannot handle null projectParentDirectory argument.");
        Validate.notEmpty(projectTopPackage, "Cannot handle null or empty projectTopPackage argument.");
        Validate.notEmpty(projectDirectoryName, "Cannot handle null or empty projectDirectoryName argument.");
        Validate.notEmpty(templateResourcePath, "Cannot handle null or empty templateResourcePath argument.");
        Validate.notNull(tokenParser, "Cannot handle null tokenParser argument.");

        final File targetDirectory = new File(projectParentDirectory, projectDirectoryName);
        Validate.isTrue(!targetDirectory.exists(), "Project directory ["
                + FileUtils.getCanonicalPath(targetDirectory) + "] must not exist. Aborting creation.");

        // Find an empty temporary file area.
        File tmpExtractedFilesRoot;
        for (int i = 0; true; i++) {
            tmpExtractedFilesRoot = new File(System.getProperty("java.io.tmpdir"),
                    "template/" + projectParentDirectory.getName() + "_" + i);
            if (!tmpExtractedFilesRoot.exists()) {
                tmpExtractedFilesRoot.mkdirs();
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Using temporary directory [" + FileUtils.getCanonicalPath(tmpExtractedFilesRoot)
                    + "] for all templates.");
        }

        // Resources:
        //
        //      /a/path/theJar.jar!/templates/standard/component_api/pom.xml
        //      /a/path/theJar.jar!/templates/standard/component_api/src/main/java/__groupId__/FooBar.java
        //      /a/path/theJar.jar!/templates/standard/component_api/src/test/java/__groupId__/FooBarTest.java

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

        } else if ("file".equalsIgnoreCase(protocol)) {

            // Check sanity
            final File templateRootDir = new File(templateRootURL.getPath());
            Validate.isTrue(templateRootDir.exists() && templateRootDir.isDirectory(),
                    "File template root directory must exist and be a directory. (Got: ["
                            + templateRootDir.getAbsolutePath() + "]");

            // Copy all resources to the tmpExtractedFilesRoot directory.
            final SortedMap<String, File> path2File = FileUtils.listFilesRecursively(templateRootDir);

            for (Map.Entry<String, File> current : path2File.entrySet()) {
                final File toWrite = new File(tmpExtractedFilesRoot, current.getKey());
                final File parentDir = toWrite.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // Copy the template file.
                FileUtils.writeFile(toWrite, FileUtils.readFile(current.getValue()));
            }

        } else {
            throw new IllegalStateException("Cannot handle resource URL [" + templateRootURL.toString()
                    + "]. Supported protocols are ('jar', 'file').");
        }

        // 3) Tokenize and move each file.
        final SortedMap<String, File> path2File = FileUtils.listFilesRecursively(tmpExtractedFilesRoot);
        for (Map.Entry<String, File> current : path2File.entrySet()) {

            final String relativePath = current.getKey();
            final String data = tokenParser.substituteTokens(FileUtils.readFile(current.getValue()));

            final File toWrite = new File(targetDirectory, relativePath);
            final File parentFile = toWrite.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            // Finally, write the file.
            FileUtils.writeFile(toWrite, data);
        }

        // Create the packages as required within the project.
        //
        // src/main/java/[package for project]
        // src/test/java/[package for project]

        final File sourceDir = FileUtils.makeDirectory(targetDirectory,
                "src/main/java/" + projectTopPackage.replace(".", "/"));
        final File testDir = FileUtils.makeDirectory(targetDirectory,
                "src/test/java/" + projectTopPackage.replace(".", "/"));

        // TODO: Move skeleton source files to the correct package directories?
    }
    */

    /**
     * Performs three tasks:
     * <ol>
     * <li>Converts the supplied templateResourcePath to a URL</li>
     * <li>Creates a temporary directory with the local path {@code "template/" + tmpDirName + "_" + i} under the
     * standard temporary directory path</li>
     * <li>Extracts all resources found in the Classpath under the templateResourcePath to the created temporary
     * directory</li>
     * </ol>
     *
     * @param tmpDirName           The name of the temporary directory to which the templates should be extracted.
     * @param templateResourcePath The resource path (not starting with '/') under which all templates are found.
     * @return The root directory under which all templates were extracted.
     */
    protected File extractTemplateFiles(final String tmpDirName,
                                        final String templateResourcePath) {

        // Check sanity
        Validate.notNull(tmpDirName, "Cannot handle null tmpDirName argument.");

        File toReturn;
        for (int i = 0; true; i++) {
            toReturn = new File(TMP_DIR, "template/" + tmpDirName + "_" + i);
            if (!toReturn.exists()) {
                toReturn.mkdirs();
                break;
            }
        }

        // Find the template retrieval specification.
        final URL templateRootURL = getTemplateResourceURL(templateResourcePath);
        final String protocol = templateRootURL.getProtocol();

        // Update the error message if other resource URL protocols are supported.
        if ("jar".equalsIgnoreCase(protocol)) {

            // Find the template JarFile, and extract all relevant templates to a temporary directory.
            final JarFile templateJarFile = JarExtractor.getJarFileFor(templateRootURL);
            JarExtractor.extractResourcesFrom(
                    templateJarFile,
                    Pattern.compile(templateResourcePath + "/.*"),
                    toReturn,
                    false);

        } else if ("file".equalsIgnoreCase(protocol)) {

            // Check sanity
            final File templateDirectory = new File(templateRootURL.getPath());
            Validate.isTrue(FileUtils.DIRECTORY_FILTER.accept(templateDirectory),
                    "File template root directory must exist and be a directory. (Got: ["
                            + templateDirectory.getAbsolutePath() + "]");

            // Copy all resources to the tmpExtractedFilesRoot directory.
            final SortedMap<String, File> path2File = FileUtils.listFilesRecursively(templateDirectory);

            for (Map.Entry<String, File> current : path2File.entrySet()) {

                // All these files are considered temporary; delete on exit
                final File toWrite = new File(toReturn, current.getKey());
                toWrite.deleteOnExit();

                final File parentDir = toWrite.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // Copy the template file.
                FileUtils.writeFile(toWrite, FileUtils.readFile(current.getValue()));
            }

        } else {
            throw new IllegalStateException("Cannot handle resource URL [" + templateRootURL.toString()
                    + "]. Supported protocols are ('jar', 'file').");
        }

        // Since these templates are temporary in nature, ensure that they are deleted on program exit
        final SortedMap<String, File> path2ExtractedTemplateFile = FileUtils.listFilesRecursively(toReturn);
        for (Map.Entry<String, File> current : path2ExtractedTemplateFile.entrySet()) {
            current.getValue().deleteOnExit();
        }

        // All done.
        return toReturn;
    }

    /**
     * Retrieves the prefix to be prepended to groupIds within the project. Normally, the project's groupIdPrefix is
     * identical to the reverse DNS of the organisation hosting the project, plus the name of the project itself.
     * <p/>
     * The default implementation strips the type suffix from rootReactorArtifactId and converts the remainder to a
     * path snippet, used as a delimiter to strip of the previous part from the rootReactorGroupId. This is
     * illustrated by an example.
     * <ol>
     * <li>Given <strong>rootReactorGroupId</strong>: {@code se.jguru.nazgul.core}, and</li>
     * <li>Given <strong>rootReactorArtifactId</strong>: {@code nazgul-core-reactor}</li>
     * <li>First, the prefix and name of the rootReactorArtifactId is joined with the
     * {@code Name.DEFAULT_SEPARATOR} yielding {@code nazgul.core}</li>
     * <li>The return value is found as a substring of the rootReactorGroupId up until the prefix snippet is
     * found, stripping off the trailing ".": {@code se.jguru}</li>
     * </ol>
     * <p/>
     * Override this method if your concrete ComponentFactory uses another algorithm to acquire the
     * projectGroupIdPrefix.
     *
     * @param rootReactorGroupId    The groupId of the root reactor POM within the project.
     * @param rootReactorArtifactId The artifactId of the root reactor POM within the project.
     * @return the projectGroupIdPrefix, prepended to the relative path of a software component to retrieve the full
     * groupID within its POM.
     */
    protected String getProjectGroupIdPrefix(final String rootReactorGroupId,
                                             final String rootReactorArtifactId) {

        // Check sanity
        Validate.notEmpty(rootReactorGroupId, "Cannot handle null or empty rootReactorGroupId argument.");
        Validate.notEmpty(rootReactorArtifactId, "Cannot handle null or empty rootReactorArtifactId argument.");

        final Name artifactIdName = Name.parse(rootReactorArtifactId);
        final String packageSnippet = (artifactIdName.getPrefix() != null ? artifactIdName.getPrefix() + "." : "")
                + artifactIdName.getName();

        if (log.isDebugEnabled()) {
            log.debug("rootReactorArtifactId [" + rootReactorArtifactId + "] yielded packageSnippet ["
                    + packageSnippet + "]");
        }

        // Normal calculation.
        int endIndex = rootReactorGroupId.indexOf(packageSnippet);
        Validate.isTrue(endIndex != -1, "Could not calculate projectGroupIdPrefix from groupID ["
                + rootReactorGroupId + "] and artifactID [" + rootReactorArtifactId
                + "]. (Calculated package snippet [" + packageSnippet + "] not found within groupID).");

        // Polish the rootReactorGroupId and return.
        final String candidate = rootReactorGroupId.substring(0, endIndex);
        int cutoffIndex = candidate.endsWith(".") ? candidate.length() - 1 : candidate.length();
        return candidate.substring(0, cutoffIndex);
    }
}
