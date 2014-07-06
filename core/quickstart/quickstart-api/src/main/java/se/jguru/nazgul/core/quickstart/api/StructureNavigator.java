package se.jguru.nazgul.core.quickstart.api;

import java.io.File;

/**
 * Specification for how to navigate in a File system containing a Project,
 * as well as how to extract data from the file system as required by the Project.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface StructureNavigator {

    /**
     * The standard separator char for directories.
     */
    public static final String DIRECTORY_SEPARATOR = "/";

    /**
     * The standard separator for packages.
     */
    public static final String PACKAGE_SEPARATOR = ".";

    /**
     * Finds the root directory of an existing project structure, given a file or
     * directory inside the project structure.
     *
     * @param fileOrDirectory A file or directory within an existing project.
     * @return The root directory of the project within which the supplied file or directory resides.
     * @throws java.lang.IllegalStateException if the supplied dir File was not found within a project (i.e. a
     *                                         directory or file which has the returned File as an ancestor within
     *                                         its directory hierarchy).
     */
    File getProjectRootDirectory(File fileOrDirectory) throws IllegalStateException;

    /**
     * Retrieves the relative path for the supplied directory within the project.
     *
     * @param directory           A directory which must have the Project Root Directory as a parent.
     * @param usePackageSeparator if {@code true}, the path returned will use {@code PACKAGE_SEPARATOR}s, and
     *                            otherwise {@code DIRECTORY_SEPARATOR}s.
     * @return The relative path between the project root directory and the supplied directory.
     * @throws IllegalStateException if the supplied dir File was not found within a project (i.e. a
     *                               directory or file which has the returned File as an ancestor within
     *                               its directory hierarchy).
     */
    String getRelativePath(File directory, boolean usePackageSeparator) throws IllegalStateException;
}
