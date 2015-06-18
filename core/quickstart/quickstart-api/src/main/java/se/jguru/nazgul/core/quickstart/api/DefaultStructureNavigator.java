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
package se.jguru.nazgul.core.quickstart.api;

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.PomAnalyzer;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Default DefaultStructureNavigator implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultStructureNavigator implements StructureNavigator, Serializable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(DefaultStructureNavigator.class.getName());

    // Internal state
    private File projectRootDirectoryCache;
    private File projectTopmostParentPomDirectoryCache;
    private NamingStrategy namingStrategy;
    private PomAnalyzer pomAnalyzer;

    /**
     * Creates a new DefaultStructureNavigator using the supplied ProjectNamingStrategy
     * to identify pieces of the project.
     *
     * @param namingStrategy The non-null ProjectNamingStrategy used to identify parts of the project.
     * @param pomAnalyzer    The non-null PomAnalyzer used to check POMs for validity.
     */
    public DefaultStructureNavigator(final NamingStrategy namingStrategy, final PomAnalyzer pomAnalyzer) {

        // Check sanity
        Validate.notNull(namingStrategy, "Cannot handle null namingStrategy argument.");
        Validate.notNull(pomAnalyzer, "Cannot handle null pomAnalyzer argument.");

        // Assign internal state
        this.namingStrategy = namingStrategy;
        this.pomAnalyzer = pomAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("all")
    @Override
    public File getProjectRootDirectory(final File fileOrDirectory) throws InvalidStructureException {

        // Check sanity
        Validate.notNull(fileOrDirectory, "Cannot handle null fileOrDirectory argument.");

        // Result already cached?
        if (projectRootDirectoryCache != null) {
            final String cachedProjectRootPath = FileUtils.getCanonicalPath(projectRootDirectoryCache);
            final String givenFileOrDir = FileUtils.getCanonicalPath(fileOrDirectory);

            if (givenFileOrDir.startsWith(cachedProjectRootPath)) {
                return projectRootDirectoryCache;
            }
        }

        // If the supplied fileOrDirectory is a File, originate from its parent directory.
        final File topmostNonNullPom = getRootReactorPomFile(fileOrDirectory);
        final File toReturn = topmostNonNullPom == null ? null : topmostNonNullPom.getParentFile();

        if (toReturn != null) {

            // 1) Validate that we have a properly formed Root Reactor pom.
            final Model rootReactorCandidate = FileUtils.getPomModel(topmostNonNullPom);
            final Parent rootReactorParent = rootReactorCandidate.getParent();
            Model parentModel = new Model();
            pomAnalyzer.validateRootReactorPom(rootReactorCandidate);

            // 2) Acquire the project Name and the corresponding project folder prefix
            final Name projectName = Name.parse(rootReactorCandidate.getArtifactId());
            namingStrategy.validate(projectName, PomType.ROOT_REACTOR);

            final String sep = projectName.getSeparator();
            final String folderNamePrefix = namingStrategy.isPrefixRequiredOnAllFolders()
                    ? projectName.getPrefix() + sep + projectName.getName() + sep
                    : projectName.getName() + sep;

            // 2) Ensure that we have a poms directory
            final File pomsDir = getRelativeFileOrDirectory(toReturn, "poms", false);
            validateDirectoryExists(pomsDir);

            // 3) ... and a correctly formed reactor POM in the poms directory
            final File pomsReactorFile = new File(pomsDir, "pom.xml");
            validateFileExists(pomsReactorFile);
            final Model pomsReactor = FileUtils.getPomModel(pomsReactorFile);
            pomAnalyzer.validate(pomsReactor, PomType.REACTOR, rootReactorCandidate);

            // 4) Validate that we have a correct setup of parent POM directories
            final File parentPomDir = new File(pomsDir, folderNamePrefix + "parent");
            final File apiParentPomDir = new File(pomsDir, folderNamePrefix + "api" + sep + "parent");
            final File modelParentPomDir = new File(pomsDir, folderNamePrefix + "model" + sep + "parent");
            validateDirectoryExists(parentPomDir);
            validateDirectoryExists(apiParentPomDir);
            validateDirectoryExists(modelParentPomDir);

            // 5) Ensure that the required parent POMs exist
            final File parentPomFile = new File(parentPomDir, "pom.xml");
            final File apiParentPomFile = new File(apiParentPomDir, "pom.xml");
            final File modelParentPomFile = new File(modelParentPomDir, "pom.xml");
            validateFileExists(parentPomFile);
            validateFileExists(apiParentPomFile);
            validateFileExists(modelParentPomFile);

            // 6) Ensure that the Parent POMs are valid, in terms of parent relations.
            final Model parentPomModel = FileUtils.getPomModel(parentPomFile);
            final Model apiParentPomModel = FileUtils.getPomModel(apiParentPomFile);
            final Model modelParentPomModel = FileUtils.getPomModel(modelParentPomFile);
            pomAnalyzer.validate(parentPomModel, PomType.PARENT, null);
            pomAnalyzer.validate(apiParentPomModel, PomType.API_PARENT, parentPomModel);
            pomAnalyzer.validate(modelParentPomModel, PomType.MODEL_PARENT, apiParentPomModel);

            // All done.
            projectRootDirectoryCache = toReturn;
            projectTopmostParentPomDirectoryCache = parentPomDir;
            return toReturn;

        } else {
            throw new InvalidStructureException("File or directory [" + fileOrDirectory.getAbsolutePath()
                    + "] is not located inside a Maven project.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRelativePath(final File directory, final boolean usePackageSeparator)
            throws InvalidStructureException {

        // Check sanity
        Validate.notNull(directory, "Cannot handle null directory argument.");
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("If the directory target exists, "
                    + "it must be a directory. Found incorrect: [" + FileUtils.getCanonicalPath(directory) + "]");
        }

        final File rootDirPath = getProjectRootDirectory(directory);
        final String rootPath = FileUtils.getCanonicalPath(rootDirPath);
        final String leafPath = FileUtils.getCanonicalPath(directory);
        final int lastIndex = leafPath.lastIndexOf(rootPath);

        if (lastIndex == -1) {
            throw new InvalidStructureException("Leaf path [" + leafPath + "] was not below [" + rootPath + "]");
        }

        String toReturn = leafPath.substring(lastIndex + rootPath.length() + 1);
        if (usePackageSeparator) {
            toReturn = toReturn.replace(File.separator, PACKAGE_SEPARATOR);
        }

        if (log.isDebugEnabled()) {
            log.debug("RootPath [" + rootPath + "] and leafPath [" + leafPath + "] yields [" + toReturn
                    + "], given usePackageSeparator [" + usePackageSeparator + "]");
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getParentPomDirectory(final File fileOrDirectory) throws InvalidStructureException {

        // Delegate
        getProjectRootDirectory(fileOrDirectory);

        // All done.
        return projectTopmostParentPomDirectoryCache;
    }

    //
    // Private helpers
    //

    private void validateDirectoryExists(final File aDir) {
        boolean directoryExists = aDir != null && aDir.exists() && aDir.isDirectory();

        if (!directoryExists) {
            final String msg = aDir == null
                    ? "Required directory nonexistent."
                    : "Required directory [" + FileUtils.getCanonicalPath(aDir) + "] nonexistent.";
            throw new InvalidStructureException(msg);
        }
    }

    private void validateFileExists(final File aFile) {
        boolean fileExists = aFile != null && aFile.exists() && aFile.isFile();

        if (!fileExists) {
            final String msg = aFile == null
                    ? "Required file nonexistent."
                    : "Required file [" + FileUtils.getCanonicalPath(aFile) + "] nonexistent.";
            throw new InvalidStructureException(msg);
        }
    }

    private File getRootReactorPomFile(final File fileOrDirectory) {

        File toReturn = null;

        for (File current = fileOrDirectory.isFile() ? fileOrDirectory.getParentFile() : fileOrDirectory;
             current != null;
             current = current.getParentFile()) {

            // Done?
            if (!current.exists()) {
                break;
            }
            File tmp = getPomFile(current);
            if (tmp == null) {
                break;
            }

            // Iterate upwards
            toReturn = tmp;
        }

        return toReturn;
    }

    private File getPomFile(final File aDirectory) {
        if (aDirectory.exists() && aDirectory.isDirectory()) {
            final File[] files = aDirectory.listFiles(POM_FILE_FILTER);
            if (files != null && files.length > 0) {
                if (files.length > 1) {
                    log.warn("More than one file in [" + aDirectory.getAbsolutePath()
                            + "] has the lowercase name 'pom.xml'. "
                            + "This is not recommended, as it confuses tooling. Returning the first one found.");
                }

                // All done,
                return files[0];
            }
        }

        // No pom file found.
        return null;
    }

    private File getRelativeFileOrDirectory(final File rootReactorDirectory,
                                            final String relativePath,
                                            final boolean isFile) {
        final File shouldExist = new File(rootReactorDirectory, relativePath);
        boolean existsProperly = shouldExist.exists() && (isFile ? shouldExist.isFile() : shouldExist.isDirectory());

        if (!existsProperly) {
            try {
                final String path = shouldExist.getCanonicalPath();
                log.warn("Required " + (isFile ? "file" : "directory") + " [" + path + "] does not exist.");
            } catch (IOException e) {
                log.error("Could not retrieve canonical path for [" + shouldExist.getAbsolutePath() + "]", e);
            }
        }

        // All done.
        return shouldExist;
    }
}
