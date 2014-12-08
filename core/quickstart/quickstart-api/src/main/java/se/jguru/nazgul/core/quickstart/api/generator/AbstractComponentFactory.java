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

    /**
     * Standard directory containing templates.
     */
    public static final String STANDARD_TEMPLATE_DIR = "template/" + System.getProperty("user.name");

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

        // Ensure that existing parent directories contain REACTOR or ROOT_REACTOR poms.
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

        // OK to create the SoftwareComponentPart.
        File componentDir = componentDirectory;
        if (!FileUtils.exists(componentDir, true)) {
            componentDir = FileUtils.makeDirectory(rootDir, relativePath);
        }

        // Create each software component part project.
        for (Map.Entry<SoftwareComponentPart, String> current : parts2SuffixMap.entrySet()) {
            addSoftwareComponentPart(componentDir, current.getKey(), current.getValue());
        }

        // Create or update the reactor POM for the software component.
        generateReactorPom(componentDir);

        // TODO: Update the parent reactor POM to include the currently added component?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSoftwareComponentPart(final File componentReactorDirectory,
                                         final SoftwareComponentPart toAdd,
                                         final String suffix) throws InvalidStructureException {

        // Check sanity
        Validate.notNull(toAdd, "Cannot handle null toAdd argument.");
        Validate.notNull(componentReactorDirectory, "Cannot handle null componentDirectory argument.");
        Validate.isTrue(FileUtils.DIRECTORY_FILTER.accept(componentReactorDirectory),
                "Software Component Directory [" + FileUtils.getCanonicalPath(componentReactorDirectory)
                        + "] was not an existing directory.");
        if (toAdd.isSuffixRequired()) {
            Validate.notEmpty(suffix,
                    "Cannot handle null or empty suffix argument for SoftwareComponentPart [" + toAdd
                            + "], as it requires a project suffix.");
        }

        // Get Project data.
        final Project projectData = getProjectFor(componentReactorDirectory);

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

        //
        // Find the name of the part directory, and then create it.
        // The directory name here is something like "messaging-model", or
        // "foobar-messaging-model" depending on the desired prefix structure.
        //
        final String dirPrefix = rootReactorName.getPrefix() != null && !"".equals(rootReactorName.getPrefix())
                ? rootReactorName.getPrefix() + Name.DEFAULT_SEPARATOR
                : "";
        final String dirSuffix = toAdd.isSuffixRequired() ? Name.DEFAULT_SEPARATOR + suffix : "";
        final String dirName = dirPrefix + rootReactorName.getName()
                + Name.DEFAULT_SEPARATOR + componentDir.getName()
                + Name.DEFAULT_SEPARATOR + toAdd.getType()
                + dirSuffix;

        final File partDir = FileUtils.makeDirectory(componentDir, dirName);
        final String topReactorPomVersion = rootReactorPomModel.getVersion() == null
                ? projectData.getReactorParent().getMavenVersion()
                : rootReactorPomModel.getVersion();
        final String parentPomVersion = parentPomModel.getVersion() == null
                ? projectData.getParentParent().getMavenVersion()
                : parentPomModel.getVersion();

        // Create a Maven project for the given PomType within PartDir
        createMavenProjectForSoftwareComponentPart(toAdd,
                partDir,
                topReactorPomVersion,
                parentPomVersion,
                projectData,
                getProjectGroupIdPrefix(rootReactorPomModel.getGroupId(), rootReactorPomModel.getArtifactId()),
                suffix);
    }

    /**
     * Creates a maven project within {@code mavenProjectDir} for a SoftwareComponentPart of the supplied type.
     * This is a convenience method to create Maven project structures for SoftwareComponentParts. (I.e. not a
     * generic Maven project creation method).
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
    protected void createMavenProjectForSoftwareComponentPart(final SoftwareComponentPart part,
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
        final String dirName = mavenProjectDir.getName();
        final File tmpExtractedFilesRoot = extractTemplateFiles(dirName, part.getComponentPomType().name());
        if (log.isDebugEnabled()) {
            log.debug("Using temporary directory [" + FileUtils.getCanonicalPath(tmpExtractedFilesRoot)
                    + "] to extract template files.");
        }

        // 2) If mavenProjectDir starts with the project prefix, we should use that
        //    setting when creating the tokens as well, and vice versa.
        final String projectNameSep = project.getName() + Name.DEFAULT_SEPARATOR;
        final boolean useProjectNameAsDirectoryPrefix = dirName.startsWith(projectNameSep)
                || dirName.startsWith(project.getPrefix() + Name.DEFAULT_SEPARATOR + projectNameSep);

        // 3) Create a TokenParser to handle template tokens
        final File softwareComponentDir = part.getComponentPomType().name().startsWith("COMPONENT_")
                ? mavenProjectDir.getParentFile()
                : mavenProjectDir;
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(part.getComponentPomType(), project)
                .prependProjectNameAsDirectoryPrefix(useProjectNameAsDirectoryPrefix)
                .inSoftwareComponentWithRelativePath(navigator.getRelativePath(softwareComponentDir, false))
                .withProjectGroupIdPrefix(optionalGroupIdPrefix)
                .withProjectSuffix(partSuffix)
                .withMavenVersions(reactorPomMavenVersion, parentPomMavenVersion)
                .build();

        // 3) Tokenize (as appropriate) and move each file to its destination.
        tokenizeAndDeploy(tmpExtractedFilesRoot, tokenParser, mavenProjectDir);
    }

    /**
     * Generates a reactor POM in the supplied softwareComponentDir directory.
     * Also - before calling this method, the REACTOR_ROOT pom must exist.
     *
     * @param softwareComponentDir A directory which must exist and be located within a Maven reactor.
     */
    protected void generateReactorPom(final File softwareComponentDir) {

        // Check sanity
        Validate.notNull(softwareComponentDir, "Cannot handle null softwareComponentDir argument.");
        Validate.isTrue(FileUtils.DIRECTORY_FILTER.accept(softwareComponentDir),
                "SoftwareComponent directory [" + FileUtils.getCanonicalPath(softwareComponentDir)
                        + "] must exist and be a directory.");

        // Read the template data
        final Project projectData = getProjectFor(softwareComponentDir);
        final String dirName = softwareComponentDir.getName();
        final String reactorParentVersion = projectData.getReactorParent().getMavenVersion();
        final String parentParentVersion = projectData.getParentParent().getMavenVersion();
        final File projectRootDirectory = navigator.getProjectRootDirectory(softwareComponentDir);
        final Model rootReactorPomModel = FileUtils.getPomModel(new File(projectRootDirectory, "pom.xml"));
        final PomType pomType = FileUtils.getCanonicalPath(softwareComponentDir)
                .equals(FileUtils.getCanonicalPath(projectRootDirectory))
                ? PomType.ROOT_REACTOR
                : PomType.REACTOR;
        final String relativePath = navigator.getRelativePath(softwareComponentDir, false);

        if (log.isDebugEnabled()) {
            log.debug("Generating reactor pom within directory [" + FileUtils.getCanonicalPath(softwareComponentDir)
                    + "], which implies relative path [" + relativePath + "]");
        }

        // 1) Extract all template files corresponding to the SoftwareComponentPart to a temporary directory
        final File tmpExtractedFilesRoot = extractTemplateFiles(softwareComponentDir.getName(), pomType.name());

        // 2) Calculate the groupId prefix
        final String projectGroupIdPrefix = getProjectGroupIdPrefix(
                rootReactorPomModel.getGroupId(),
                rootReactorPomModel.getArtifactId());

        // 3) Generate the modules XML text.
        final StringBuilder modulesElementBuilder = new StringBuilder("");
        for (File current : softwareComponentDir.listFiles(FileUtils.MODULE_NAME_FILTER)) {
            modulesElementBuilder.append("<module>").append(current.getName()).append("</module>\n");
        }
        if (modulesElementBuilder.toString().isEmpty() && log.isWarnEnabled()) {
            log.warn("Directory [" + FileUtils.getCanonicalPath(softwareComponentDir) + "] held no module "
                    + "subdirectories. This implies a non-functional SoftwareComponent of type ["
                    + pomType.name() + "] without any parts.");
            modulesElementBuilder.append("<module/>");
        }

        // 4) If mavenProjectDir starts with the project prefix, we should use that
        //    setting when creating the tokens as well, and vice versa.
        final String projectNameSep = projectData.getName() + Name.DEFAULT_SEPARATOR;
        final boolean useProjectNameAsDirectoryPrefix = dirName.startsWith(projectNameSep)
                || dirName.startsWith(projectData.getPrefix() + Name.DEFAULT_SEPARATOR + projectNameSep);

        // 5) Create a TokenParser to handle template tokens
        final TokenParser tokenParser = SingleBracketPomTokenParserFactory
                .create(pomType, projectData)
                .prependProjectNameAsDirectoryPrefix(useProjectNameAsDirectoryPrefix)
                .inSoftwareComponentWithRelativePath(relativePath)
                .withProjectGroupIdPrefix(projectGroupIdPrefix)
                .withoutProjectSuffix()
                .withMavenVersions(reactorParentVersion, parentParentVersion)
                .addToken(PomToken.MODULES.getToken(), modulesElementBuilder.toString())
                .build();

        // 5) Tokenize and write the resulting file(s).
        tokenizeAndDeploy(tmpExtractedFilesRoot, tokenParser, softwareComponentDir);
    }

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
            toReturn = new File(TMP_DIR, STANDARD_TEMPLATE_DIR + tmpDirName + "_" + i);
            if (!toReturn.exists()) {
                toReturn.mkdirs();
                break;
            }
        }

        // Find the template retrieval specification.
        final URL templateRootURL = getTemplateResourceURL(templateResourcePath);
        final String protocol = templateRootURL.getProtocol();

        // Update the error message if other resource URL protocols are supported.
        final boolean usesJarPackaging = "jar".equalsIgnoreCase(protocol);
        final boolean usesFilePackaging = "file".equalsIgnoreCase(protocol);
        if (usesJarPackaging) {

            // Find the template JarFile, and extract all relevant templates to a temporary directory.
            final JarFile templateJarFile = JarExtractor.getJarFileFor(templateRootURL);
            JarExtractor.extractResourcesFrom(
                    templateJarFile,
                    Pattern.compile(templateResourcePath + "/.*"),
                    toReturn,
                    false);

        } else if (usesFilePackaging) {

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
     * Override this method if you choose to derive the project's groupId prefix in another way.
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

    //
    // Private helpers
    //

    /**
     * Creates the Project originating from the supplied dirInReactor directory (which must exist).
     *
     * @param dirInReactor A non-null directory within the reactor structure.
     * @return The Project data for the supplied dirInReactor.
     */
    private Project getProjectFor(final File dirInReactor) {

        // Check sanity
        Validate.notNull(dirInReactor, "Cannot handle null dirInReactor argument.");
        Validate.isTrue(FileUtils.DIRECTORY_FILTER.accept(dirInReactor),
                "Software Component Directory [" + FileUtils.getCanonicalPath(dirInReactor)
                        + "] was not an existing directory.");

        // Proceed to create the structures as instructed.
        final File projectRootDirectory = navigator.getProjectRootDirectory(dirInReactor);
        final Model rootReactorPomModel = FileUtils.getPomModel(new File(projectRootDirectory, "pom.xml"));
        final Name rootReactorName = getNamingStrategy().createName(rootReactorPomModel);

        final File parentPomDir = navigator.getParentPomDirectory(dirInReactor);
        final Model parentPomModel = FileUtils.getPomModel(new File(parentPomDir, "pom.xml"));

        // All done.
        return new Project(
                rootReactorName.getPrefix(),
                rootReactorName.getName(),
                FileUtils.getSimpleArtifact(rootReactorPomModel),
                FileUtils.getSimpleArtifact(parentPomModel));
    }

    /**
     * Tokenizes all files found under extractedTemplatesRoot using the supplied tokenParser.
     * Following the tokenization, the files are copied to their respective relative paths under
     * the mavenProjectDir.
     *
     * @param extractedTemplatesRoot A directory holding the extracted template files.
     * @param tokenParser            The TokenParser used to tokenize the templates.
     * @param targetDirectory        The directory where the project should be written.
     */
    private void tokenizeAndDeploy(final File extractedTemplatesRoot,
                                   final TokenParser tokenParser,
                                   final File targetDirectory) {

        final FileFilter shouldTokenizeFilter = getShouldTokenizeFilter();
        final SortedMap<String, File> path2FileMap = FileUtils.listFilesRecursively(extractedTemplatesRoot);
        for (Map.Entry<String, File> current : path2FileMap.entrySet()) {

            // Tokenize the relative path, and the file data if applicable.
            final String relativePath = tokenParser.substituteTokens(current.getKey());
            final String data = shouldTokenizeFilter.accept(current.getValue())
                    ? tokenParser.substituteTokens(FileUtils.readFile(current.getValue()))
                    : FileUtils.readFile(current.getValue());

            final File toWrite = new File(targetDirectory, relativePath);
            final File parentFile = toWrite.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            // Finally, write the file.
            FileUtils.writeFile(toWrite, data);
        }
    }
}
