/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.quickstart.api;

import java.io.File;
import java.io.FileFilter;

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
    String DIRECTORY_SEPARATOR = "/";

    /**
     * The standard separator for packages.
     */
    String PACKAGE_SEPARATOR = ".";

    /**
     * A FileFilter identifying pom.xml files.
     */
    FileFilter POM_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File fileOrDirectory) {
            return fileOrDirectory.isFile() && fileOrDirectory.getName().equalsIgnoreCase("pom.xml");
        }
    };

    /**
     * Finds the root directory of an existing project structure, given a file or directory inside the project
     * structure. If the root directory could not be found - because the structure in the root directory was
     * incorrect, because found POMs did not have correct parents or follow the required NamingStrategy etc. -
     * an InvalidStructureException is thrown with a message indicating the exact reason.
     *
     * @param fileOrDirectory A file or directory within an existing project.
     * @return The root directory of the project within which the supplied file or directory resides.
     * @throws InvalidStructureException if the supplied dir File was not found within a project (i.e. a
     *                                   directory or file which has the returned File as an ancestor within
     *                                   its directory hierarchy). Also thrown if any of the required
     *                                   poms (reactor or parent) poms were not compliant with the namingStrategy.
     */
    File getProjectRootDirectory(File fileOrDirectory) throws InvalidStructureException;

    /**
     * Finds the directory of the topmost parent pom within an existing project structure, given a file or directory
     * inside the project structure. If the root directory could not be found - because the structure in the root
     * directory was incorrect, because found POMs did not have correct parents or follow the required NamingStrategy
     * etc. - an InvalidStructureException is thrown with a message indicating the exact reason.
     *
     * @param fileOrDirectory A file or directory within an existing project.
     * @return The root directory of the project within which the supplied file or directory resides.
     * @throws InvalidStructureException if the supplied dir File was not found within a project (i.e. a
     *                                   directory or file which has the returned File as an ancestor within
     *                                   its directory hierarchy). Also thrown if any of the required
     *                                   poms (reactor or parent) poms were not compliant with the namingStrategy.
     */
    File getParentPomDirectory(File fileOrDirectory) throws InvalidStructureException;

    /**
     * Retrieves the relative path for the supplied directory within the project.
     *
     * @param directory           A directory which must have the Project Root Directory as a parent.
     * @param usePackageSeparator if {@code true}, the path returned will use {@code PACKAGE_SEPARATOR}s, and
     *                            otherwise {@code DIRECTORY_SEPARATOR}s.
     * @return The relative path between the project root directory and the supplied directory.
     * @throws InvalidStructureException if the supplied dir File was not found within a project (i.e. a directory or
     *                                   file which has the returned File as an ancestor within its directory hierarchy), or if the
     */
    String getRelativePath(File directory, boolean usePackageSeparator) throws InvalidStructureException;
}
