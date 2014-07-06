package se.jguru.nazgul.core.quickstart.api;

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.api.analyzer.ProjectNamingStrategy;

import java.io.File;
import java.io.FileFilter;
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

            // Simply assign the
            currentPom = getPomFile(current);
        }

        // Does the top directory contain a

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
}
