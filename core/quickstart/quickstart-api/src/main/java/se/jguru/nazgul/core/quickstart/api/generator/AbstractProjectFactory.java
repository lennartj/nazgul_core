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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.quickstart.api.FileUtils;
import se.jguru.nazgul.core.quickstart.api.InvalidStructureException;
import se.jguru.nazgul.core.quickstart.api.PomType;
import se.jguru.nazgul.core.quickstart.api.analyzer.AbstractNamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.generator.parser.PomToken;
import se.jguru.nazgul.core.quickstart.api.generator.parser.SingleBracketPomTokenParserFactory;
import se.jguru.nazgul.core.quickstart.model.Name;
import se.jguru.nazgul.core.quickstart.model.Project;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.File;

/**
 * Abstract implementation of the ProjectFactory sporting
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractProjectFactory extends AbstractFactory implements ProjectFactory {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractProjectFactory.class.getName());

    /**
     * Creates a new AbstractProjectFactory wrapping the supplied data.
     *
     * @param namingStrategy The active NamingStrategy, used to validate given Project data.
     */
    protected AbstractProjectFactory(final NamingStrategy namingStrategy) {
        super(namingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createProject(final File projectParentDir,
                                 final Project project,
                                 final String packagePrefix,
                                 final String reactorParentMavenVersion,
                                 final String topmostParentMavenVersion)
            throws IllegalArgumentException, IllegalStateException {

        // Check sanity
        Validate.notNull(project, "Cannot handle null projectDefinition argument.");
        Validate.notNull(projectParentDir, "Cannot handle null projectParentDir argument.");
        Validate.isTrue(projectParentDir.exists() && projectParentDir.isDirectory(),
                "Project parent directory [" + FileUtils.getCanonicalPath(projectParentDir)
                        + "] must exist and be a directory.");
        Validate.notEmpty(reactorParentMavenVersion, "Cannot handle null or empty reactorParentMavenVersion argument.");
        Validate.notEmpty(topmostParentMavenVersion, "Cannot handle null or empty topmostParentMavenVersion argument.");

        final String reverseDNS = packagePrefix == null ? "" : packagePrefix.replace("/", ".");

        // Synthesize the Name from the supplied project, and validate it.
        final Name projectName = new Name(
                project.getPrefix(),
                project.getName(),
                AbstractNamingStrategy.REACTOR_SUFFIX);
        getNamingStrategy().validate(projectName, PomType.ROOT_REACTOR);

        // Find the directory of the new project, which should be on the form [parent]/[name].
        final String projectRootDirName = getNamingStrategy().isPrefixRequiredOnAllFolders()
                ? project.getPrefix() + Name.DEFAULT_SEPARATOR + project.getName()
                : project.getName();
        final File projectRootDir = new File(projectParentDir, projectRootDirName);
        if (log.isInfoEnabled()) {
            log.info("Using project root directory [" + projectRootDir.getAbsolutePath() + "].");
        }
        if (!FileUtils.exists(projectRootDir, true) && !projectRootDir.mkdirs()) {
            throw new IllegalStateException("Project root directory [" + FileUtils.getCanonicalPath(projectRootDir)
                    + "] was nonexistent and could not be created.");
        }

        // Create the project structure.
        createParentPom(projectRootDir, "", PomType.ROOT_REACTOR, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);
        createParentPom(projectRootDir, "poms", PomType.PARENT, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);
        createParentPom(projectRootDir, "poms", PomType.API_PARENT, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);
        createParentPom(projectRootDir, "poms", PomType.MODEL_PARENT, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);
        createParentPom(projectRootDir, "poms", PomType.WAR_PARENT, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);
        createParentPom(projectRootDir, "poms", PomType.REACTOR, project,
                reactorParentMavenVersion, topmostParentMavenVersion, reverseDNS);

        // All done.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project createProjectDefinition(final String prefix,
                                           final String name,
                                           final SimpleArtifact reactorParent,
                                           final SimpleArtifact parentParent) {
        // Check sanity
        Validate.notEmpty(name, "Cannot handle null or empty name argument.");
        Validate.notNull(reactorParent, "Cannot handle null reactorParent argument.");
        Validate.notNull(parentParent, "Cannot handle null parentParent argument.");

        // All done.
        return new Project(prefix, name, reactorParent, parentParent);
    }

    /**
     * Synthesizes the directory name from the supplied Project and PomType.
     * The resulting name should adhere to the NamingStrategy's
     * {@code NamingStrategy.isPrefixRequiredOnAllFolders()} property.
     *
     * @param projectName   The name of the active project.
     * @param projectPrefix The optional prefix of the active project. Must be supplied (and non-empty) if
     *                      the NamingStrategy requires prefixes to be present on all folders' names.
     * @param aPomType      The PomType for which a directory name should be synthesized.
     * @return The name of the directory.
     */
    protected String getSkeletonPomDirectoryName(final String projectName,
                                                 final PomType aPomType,
                                                 final String projectPrefix) {

        // Check sanity
        Validate.notNull(aPomType, "Cannot handle null aPomType argument.");
        Validate.notEmpty(projectName, "Cannot handle null or empty projectName argument.");
        if (getNamingStrategy().isPrefixRequiredOnAllFolders() && (projectPrefix == null || projectPrefix.isEmpty())) {
            throw new IllegalArgumentException("NamingStrategy [" + getNamingStrategy().getClass().getSimpleName()
                    + "] requires that project prefix is required on all folders. Therefore, "
                    + "a nonempty projectPrefix argument must be supplied.");
        }

        // Reactor directories should only be called their respective component name.
        if (aPomType == PomType.ROOT_REACTOR || aPomType == PomType.REACTOR) {
            return getNamingStrategy().isPrefixRequiredOnAllFolders()
                    ? projectPrefix : "";
        }

        final String dirPrefix = (getNamingStrategy().isPrefixRequiredOnAllFolders()
                ? projectPrefix + Name.DEFAULT_SEPARATOR + projectName
                : projectName)
                + Name.DEFAULT_SEPARATOR;

        String dirSuffix = aPomType.name().toLowerCase().replace("_", "-");
        if (aPomType == PomType.OTHER_PARENT) {
            dirSuffix = AbstractNamingStrategy.PARENT_SUFFIX;
        }

        final String toReturn = dirPrefix + dirSuffix;
        if (log.isDebugEnabled()) {
            log.debug("PomType [" + aPomType + "] yields directory name [" + toReturn + "]");
        }

        // All done.
        return toReturn;
    }

    //
    // Helpers
    //

    @SuppressWarnings("all")
    private void createParentPom(final File rootDirectory,
                                 final String relativePomDirectory,
                                 final PomType pomType,
                                 final Project project,
                                 final String reactorPomMavenVersion,
                                 final String parentPomMavenVersion,
                                 final String packagePrefix) throws InvalidStructureException {

        // Sane state?
        final String directoryName = getSkeletonPomDirectoryName(project.getName(), pomType, project.getPrefix());
        final String relativePath = relativePomDirectory.isEmpty() ? "" : relativePomDirectory + "/";
        final File pomDirectory = FileUtils.makeDirectory(rootDirectory, relativePath + directoryName);
        final File pomFile = new File(pomDirectory, "pom.xml");

        if (FileUtils.exists(pomFile, false)) {
            throw new InvalidStructureException("POM file [" + FileUtils.getCanonicalPath(pomFile)
                    + "] could not be created as it already exists.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating POM file [" + FileUtils.getCanonicalPath(pomFile) + "]");
        }

        // Read the POM resource, and write the POM file.
        final SingleBracketPomTokenParserFactory.Builder builder = SingleBracketPomTokenParserFactory
                .create(pomType, project)
                .withoutProjectNameAsDirectoryPrefix()
                .inSoftwareComponentWithRelativePath(relativePath)
                .withProjectGroupIdPrefix(packagePrefix)
                .withoutProjectSuffix()
                .withMavenVersions(reactorPomMavenVersion, parentPomMavenVersion);

        if (pomType == PomType.REACTOR) {

            // Find all module names for the active reactor directory.
            final StringBuilder moduleBuilder = new StringBuilder();
            for (String current : FileUtils.getModuleNames(pomDirectory)) {
                moduleBuilder.append("<module>").append(current).append("</module>\n");
            }
            builder.addToken(PomToken.MODULES.getToken(), moduleBuilder.toString());
        }

        final TokenParser tokenParser = builder.build();

        final String pomData = tokenParser.substituteTokens(getTemplateResource(pomType + "/pom.xml"));
        FileUtils.writeFile(pomFile, pomData);
    }
}
