package se.jguru.nazgul.core.quickstart.api;

import org.apache.commons.lang3.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.ProjectNamingStrategy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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

    /**
     * A FileFilter identifying pom.xml files.
     */
    public static final FileFilter POM_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File fileOrDirectory) {
            return fileOrDirectory.isFile() && fileOrDirectory.getName().equalsIgnoreCase("pom.xml");
        }
    };

    // Internal state
    private ProjectNamingStrategy namingStrategy;
    private MavenXpp3Reader pomReader;

    /**
     * {@inheritDoc}
     */
    @Override
    public File getProjectRootDirectory(final File fileOrDirectory) throws IllegalStateException {

        // Check sanity
        Validate.notNull(fileOrDirectory, "Cannot handle null fileOrDirectory argument.");

        // If the supplied fileOrDirectory is a File, originate from its parent directory.
        File current = fileOrDirectory.isFile() ? fileOrDirectory.getParentFile() : fileOrDirectory;
        File currentPom;
        for(currentPom = getPomFile(current);
            current != null && currentPom != null;
            current = current.getParentFile()) {

            // Simply assign the current pom file.
            currentPom = getPomFile(current);
        }

        // Does the top directory contain a reactor pom file?
        final Model rootReactorCandidate = getPomModel(currentPom);
        if(namingStrategy.isRootReactorPom(
                rootReactorCandidate.getParent().getGroupId(),
                rootReactorCandidate.getParent().getArtifactId(),
                rootReactorCandidate.getGroupId(),
                rootReactorCandidate.getArtifactId())) {

            // We have found a root reactor POM. Ensure that we have a poms directory here as well.
            @SuppressWarnings("all")
            final File[] pomsDir = current.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File aDir) {
                    return aDir.isDirectory() && aDir.getName().equalsIgnoreCase("poms");
                }
            });

            if(pomsDir != null && pomsDir.length == 1) {
                final File[] files = pomsDir[0].listFiles(POM_FILE_FILTER);

            }
        }

        // No project root found.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRelativePath(final File directory, final boolean usePackageSeparator) throws IllegalStateException {
        return null;
    }

    //
    // Private helpers
    //

    private File getPomFile(final File aDirectory) {
        if(aDirectory.exists() && aDirectory.isDirectory()) {
            final File[] files = aDirectory.listFiles(POM_FILE_FILTER);
            if(files != null) {
                if(files.length > 0) {
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
            throw new IllegalArgumentException("Could not read POM file [" + aPomFile.getAbsolutePath() + "]", e);
        }
    }
}
