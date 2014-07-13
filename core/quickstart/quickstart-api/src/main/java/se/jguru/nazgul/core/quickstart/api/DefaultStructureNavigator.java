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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.NamingStrategy;
import se.jguru.nazgul.core.quickstart.api.analyzer.PomAnalyzer;
import se.jguru.nazgul.core.quickstart.api.analyzer.PomType;
import se.jguru.nazgul.core.quickstart.model.Name;

import java.io.File;
import java.io.FileReader;
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
    private NamingStrategy namingStrategy;
    private PomAnalyzer pomAnalyzer;
    private MavenXpp3Reader pomReader = new MavenXpp3Reader();

    /**
     * Creates a new DefaultStructureNavigator using the supplied ProjectNamingStrategy
     * to identify pieces of the project.
     *
     * @param namingStrategy The non-null ProjectNamingStrategy used to identify parts of the project.
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
            final String cachedProjectRootPath = getCanonicalPath(projectRootDirectoryCache);
            final String givenFileOrDir = getCanonicalPath(fileOrDirectory);

            if (givenFileOrDir.startsWith(cachedProjectRootPath)) {
                return projectRootDirectoryCache;
            }
        }

        // If the supplied fileOrDirectory is a File, originate from its parent directory.
        final File topmostNonNullPom = getRootReactorPomFile(fileOrDirectory);
        final File toReturn = topmostNonNullPom == null ? null : topmostNonNullPom.getParentFile();

        if (toReturn != null) {

            // 1) Validate that we have a properly formed Root Reactor pom.
            final Model rootReactorCandidate = getPomModel(topmostNonNullPom);
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
            final Model pomsReactor = getPomModel(pomsReactorFile);
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
            final Model parentPomModel = getPomModel(parentPomFile);
            final Model apiParentPomModel = getPomModel(apiParentPomFile);
            final Model modelParentPomModel = getPomModel(modelParentPomFile);
            pomAnalyzer.validate(parentPomModel, PomType.PARENT, null);
            pomAnalyzer.validate(apiParentPomModel, PomType.API_PARENT, parentPomModel);
            pomAnalyzer.validate(modelParentPomModel, PomType.MODEL_PARENT, apiParentPomModel);

            // All done.
            projectRootDirectoryCache = toReturn;
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

        final File rootDirPath = getProjectRootDirectory(directory);
        if (rootDirPath == null) {
            throw new IllegalStateException("Directory [" + getCanonicalPath(directory)
                    + "] is not part of a project.");
        }

        final String rootPath = getCanonicalPath(rootDirPath);
        final String leafPath = getCanonicalPath(directory);
        String toReturn = rootPath.substring(rootPath.lastIndexOf(leafPath));
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

    //
    // Private helpers
    //

    private void validateDirectoryExists(final File aDir) {
        boolean directoryExists = aDir != null && aDir.exists() && aDir.isDirectory();

        if(!directoryExists) {
            final String msg = aDir == null
                    ? "Required directory nonexistent."
                    : "Required directory [" + getCanonicalPath(aDir) + "] nonexistent.";
            throw new InvalidStructureException(msg);
        }
    }

    private void validateFileExists(final File aFile) {
        boolean fileExists = aFile != null && aFile.exists() && aFile.isFile();

        if(!fileExists) {
            final String msg = aFile == null
                    ? "Required file nonexistent."
                    : "Required file [" + getCanonicalPath(aFile) + "] nonexistent.";
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
            if (files != null) {
                if (files.length > 0) {
                    if (files.length > 1) {
                        log.warn("More than one file in [" + aDirectory.getAbsolutePath()
                                + "] has the lowercase name 'pom.xml'. "
                                + "This is not recommended, as it confuses tooling. Returning the first one found.");
                    }

                    // All done,
                    return files[0];
                }
            }
        }

        // No pom file found.
        return null;
    }

    private Model getPomModel(final File aPomFile) {
        try {
            return pomReader.read(new FileReader(aPomFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read POM file [" + getCanonicalPath(aPomFile) + "]", e);
        }
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


    private String getCanonicalPath(final File fileOrDirectory) {
        try {
            return fileOrDirectory.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException("Could not acquire canonical path for [" + fileOrDirectory + "]", e);
        }
    }

    /*

    private void validateParentPom(final Model toValidate, final Model requiredParent) {

        final List<String> errors = new ArrayList<>();

        //
        // Note that the version of the toValidate project might be null
        // (in case the version is inherited from the parent, and therefore matching).
        //
        final Parent parentToValidate = toValidate.getParent();
        final boolean versionMatches = toValidate.getVersion() == null
                || requiredParent.getVersion().equals(parentToValidate.getVersion());
        if(!versionMatches) {
            errors.add("Required version mismatch (" + requiredParent.getVersion() + ")");
        }
        if(!requiredParent.getGroupId().equals(parentToValidate.getGroupId())) {
            errors.add("Required groupId mismatch (" + requiredParent.getGroupId() + ")");
        }
        if(!requiredParent.getArtifactId().equals(parentToValidate.getArtifactId())) {
            errors.add("Required artifactId mismatch (" + requiredParent.getArtifactId() + ")");
        }

        if(errors.size() > 0) {

            final String effectiveVersion = toValidate.getVersion() == null
                    ? toValidate.getParent().getVersion()
                    : toValidate.getVersion();

            final StringBuilder builder = new StringBuilder("Incorrect parent relationship for POM ("
                    + toValidate.getGroupId() + "/" + toValidate.getArtifactId() + "/" + effectiveVersion + "): ");
            for(int i = 0; i < errors.size(); i++) {
                builder.append("\n " + (i + 1)).append(errors.get(i));
            }

            // All done.
            throw new InvalidStructureException(builder.toString());
        }
    }


    private boolean isParent(final Model requiredParent, final Model toValidate) {

        if (requiredParent == null || toValidate == null) {
            return false;
        }

        //
        // Note that the version of the toValidate project might be null
        // (in case the version is inherited from the parent, and therefore matching).
        //
        final Parent parentToValidate = toValidate.getParent();
        final boolean versionMatches = toValidate.getVersion() == null
                || requiredParent.getVersion().equals(parentToValidate.getVersion());
        return requiredParent.getGroupId().equals(parentToValidate.getGroupId())
                && requiredParent.getArtifactId().equals(parentToValidate.getArtifactId())
                && versionMatches;
    }

    private String getErrorMessage(final String pomType,
                                   final File pomFile,
                                   final String requiredParentGroupId,
                                   final String requiredParentArtifactId,
                                   final String requiredGroupId,
                                   final String requiredArtifactId) {

        final String parentRequirement = requiredParentGroupId == null
                ? ""
                : "Required Parent [groupId: " + requiredParentGroupId + ", artifactId: "
                + requiredParentArtifactId + "]";

        final String selfRequirement = requiredGroupId == null
                ? ""
                : "Required Own [groupId: " + requiredGroupId + ", artifactId: " + requiredArtifactId + "]";

        return pomType + " pom [" + pomFile.getAbsolutePath()
                + "] nonexistent or does not comply with naming strategy. ("
                + parentRequirement + ", " + selfRequirement + ")";
    }
    */
}
