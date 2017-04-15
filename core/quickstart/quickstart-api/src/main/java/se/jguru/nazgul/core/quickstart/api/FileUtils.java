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

import org.apache.commons.lang3.Validate;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A suite of utility algorithms for use with Files and related structures.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class FileUtils {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class.getName());

    /**
     * The line ending string.
     */
    public static final String LINE_ENDING = System.getProperty("line.separator");

    /**
     * The file separator ("/" on unix, "\" on windows).
     */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * A FileFilter which accepts directories only.
     */
    public static final FileFilter DIRECTORY_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {

            System.getProperties();
            return pathname.exists() && pathname.isDirectory();
        }
    };

    /**
     * A FileFilter which accepts files only.
     */
    public static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return pathname.exists() && pathname.isFile();
        }
    };

    /**
     * A FileFilter which accepts Maven module directories only (i.e. directories containing a 'pom.xml' file).
     */
    public static final FileFilter MODULE_NAME_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File moduleCandidate) {
            if (moduleCandidate.exists() && moduleCandidate.isDirectory()) {
                return FILE_FILTER.accept(new File(moduleCandidate, "pom.xml"));
            }

            // Not a module directory/name.
            return false;
        }
    };

    /**
     * A FileFilter which accepts nonexistent or empty directories.
     * For this FileFilter, an "empty" directory implies that no directories or files are present within a successful
     * candidate. All files except those whose names starts with "." are considered files.
     */
    public static final FileFilter NONEXISTENT_OR_EMPTY_DIRECTORY_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File candidate) {

            boolean okCandidate = !candidate.exists();
            if (!okCandidate && DIRECTORY_FILTER.accept(candidate)) {

                final File[] childFiles = candidate.listFiles();
                if (childFiles != null && childFiles.length != 0) {
                    for (File current : childFiles) {
                        if (!current.getName().startsWith(".")) {
                            return false;
                        }
                    }
                }

                // All seems well.
                okCandidate = true;
            }

            // All done.
            return okCandidate;
        }
    };

    /**
     * A list containing file suffixes of files normally containing character data, implying that the
     * content of such files can normally be manipulated using TokenParsers.
     */
    public static final List<String> CHARDATA_FILE_SUFFIXES = Arrays.asList("txt", "text", "xml", "xsd", "properties",
            "apt", "log", "md", "readme", "csv", "tab", "odt", "1st", "java", "jsp", "js", "jsf", "c", "cpp", "html",
            "css", "js", "less", "scss");

    /**
     * A FileFilter which identifies file types normally holding character data content.
     */
    public static final FileFilter CHARACTER_DATAFILE_FILTER = new FileFilter() {

        /**
         * Accepts aFile if it is a file whose suffix is found in the CHARDATA_FILE_SUFFIXES list.
         *
         * @param aFile The non-null File to check.
         * @return {@code true} if the supplied aFile is a file whose suffix is found in
         * the {@code CHARDATA_FILE_SUFFIXES} list.
         */
        @Override
        public boolean accept(final File aFile) {

            if (FILE_FILTER.accept(aFile)) {

                // Find the file suffix to compare with.
                final String fileName = aFile.getName();
                final int suffixDelimiterIndex = fileName.lastIndexOf(".");
                final String fileSuffix = "" + (suffixDelimiterIndex == -1
                        ? fileName
                        : fileName.substring(suffixDelimiterIndex + 1)).trim().toLowerCase();

                // All done.
                return CHARDATA_FILE_SUFFIXES.contains(fileSuffix);
            }

            // not recognized as a CharacterData-based file.
            return false;
        }
    };

    // Internal state
    private static MavenXpp3Reader pomReader = new MavenXpp3Reader();

    /*
     * Hide constructor for utility classes.
     */
    private FileUtils() {
        // Do nothing
    }

    /**
     * Gets the canonical path of the supplied (non-null) File.
     *
     * @param fileOrDirectory A non-null File object.
     * @return The canonical path to the supplied fileOrDirectory.
     */
    public static String getCanonicalPath(final File fileOrDirectory) {

        // Check sanity
        Validate.notNull(fileOrDirectory, "Cannot handle null or empty fileOrDirectory argument.");

        try {
            return fileOrDirectory.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire canonical path for [" + fileOrDirectory + "]", e);
        }
    }

    /**
     * Creates all intermediary directories from the rootDirectory to the leafDirectory
     * given by the relativePath supplied.
     *
     * @param rootDirectory The existent or nonexistent rootDirectory.
     * @param relativePath  The relative path of directories to make.
     * @return The directory just made.
     * @throws java.lang.IllegalArgumentException if the leafDirectory (or any of its intermediary directories)
     *                                            could not be created.
     */
    public static File makeDirectory(final File rootDirectory, final String relativePath)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(rootDirectory, "Cannot handle null rootDirectory argument.");
        Validate.notNull(relativePath, "Cannot handle null relativePath argument.");

        final File toReturn = relativePath.isEmpty() ? rootDirectory : new File(rootDirectory, relativePath);
        Validate.isTrue(!toReturn.exists() || (toReturn.exists() && toReturn.isDirectory()),
                "Insane state. [" + getCanonicalPath(toReturn) + "] exists but is not a Directory.");

        // Delegate and return
        if (toReturn.mkdirs()) {
            return toReturn;
        }

        // Should we fail?
        if (!exists(toReturn, true)) {
            throw new IllegalArgumentException("Could not create path to [" + getCanonicalPath(toReturn)
                    + "] fully. Check filesystem; state may be broken.");
        }

        // Seems we are OK anyways.
        return toReturn;
    }

    /**
     * Acquires a Maven Model from the supplied POM file.
     *
     * @param aPomFile A non-null pom.xml File.
     * @return The Maven model converted from the supplied aPomFile.
     */
    public static Model getPomModel(final File aPomFile) {

        // Check sanity
        Validate.notNull(aPomFile, "Cannot handle null aPomFile argument.");
        Validate.isTrue(FILE_FILTER.accept(aPomFile), "File [" + getCanonicalPath(aPomFile)
                + "] must exist and be a File.");

        try {
            return pomReader.read(new FileReader(aPomFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read POM file [" + getCanonicalPath(aPomFile) + "]", e);
        }
    }

    /**
     * Given a directory within a Maven project reactor, list the names of all subdirectories
     * containing a 'pom.xml' file. These directories typically correspond to module names within a
     * reactor POM found in the supplied reactorDirectory.
     *
     * @param reactorDirectory An existing directory.
     * @return The names of all subdirectories to the provided reactorDirectory where a file called "pom.xml" exists.
     * The names of these directories should typically be used as module names within a reactor pom located within
     * the supplied reactorDirectory. No validation of any found pom.xml files (consistency, well-formed-ness etc.)
     * are done within this method.
     */
    public static List<String> getModuleNames(final File reactorDirectory) {

        // Check sanity
        Validate.notNull(reactorDirectory, "Cannot handle null or empty reactorDirectory argument.");
        Validate.isTrue(DIRECTORY_FILTER.accept(reactorDirectory), "reactorDirectory argument ["
                + getCanonicalPath(reactorDirectory) + "] must refer to an existing directory.");

        final List<String> toReturn = new ArrayList<>();
        for (File current : reactorDirectory.listFiles(MODULE_NAME_FILTER)) {
            toReturn.add(current.getName());
        }

        // All done.
        return toReturn;
    }

    /**
     * Extracts a SimpleArtifact from a Maven model.
     *
     * @param aModel The Maven Model from which to extract the SimpleArtifact data.
     * @return A SimpleArtifact wrapping the data found in the supplied aModel.
     */
    public static SimpleArtifact getSimpleArtifact(final Model aModel) {

        // Check sanity
        Validate.notNull(aModel, "Cannot handle null aModel argument.");

        String version = aModel.getVersion();
        if (version == null) {
            Validate.notNull(aModel.getParent(), "A Model requires either a version or a non-null Parent definition.");
            version = aModel.getParent().getVersion();
        }

        // All done.
        return new SimpleArtifact(aModel.getGroupId(), aModel.getArtifactId(), version);
    }

    /**
     * Identifies if the supplied fileOrDir exists.
     *
     * @param fileOrDir   A non-null File.
     * @param isDirectory if {@code true}, the fileOrDir is assumed to be a directory - and otherwise a file.
     * @return {@code true} if the supplied fileOrDir exists and is of the type indicated by {@code isDirectory}.
     */
    public static boolean exists(final File fileOrDir, final boolean isDirectory) {

        Validate.notNull(fileOrDir, "Cannot handle null fileOrDir argument.");
        return isDirectory ? DIRECTORY_FILTER.accept(fileOrDir) : FILE_FILTER.accept(fileOrDir);
    }

    /**
     * Writes the supplied data to the given File.
     *
     * @param aFile The non-null File to write data to.
     * @param data  The non-null data string to write.
     */
    public static void writeFile(final File aFile, final String data) {

        // Check sanity
        Validate.notNull(aFile, "Cannot handle null aFile argument.");
        Validate.notNull(data, "Cannot handle null data argument.");

        final File dirForFile = aFile.getParentFile();
        if (dirForFile == null || !(dirForFile.exists() && dirForFile.isDirectory())) {
            throw new IllegalArgumentException("Cannot write file [" + FileUtils.getCanonicalPath(aFile)
                    + "], since its parent is not an existing directory.");
        }

        // All seems sane. Write the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aFile))) {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            log.warn("Could not write data to [" + getCanonicalPath(aFile) + "]", e);
        }
    }

    /**
     * Reads all (text) data from the supplied File, returning it as a String.
     * All line feeds are converted to {@code System.getProperty("line.separator")}.
     *
     * @param aFile The non-null File to read data from.
     * @return The content of the supplied File.
     */
    public static String readFile(final File aFile) {

        // Check sanity
        Validate.notNull(aFile, "Cannot handle null aFile argument.");
        Validate.isTrue(FILE_FILTER.accept(aFile), "File [" + getCanonicalPath(aFile)
                + "] must exist and be a (text) file.");

        try {
            return readFully(new FileInputStream(aFile), getCanonicalPath(aFile));
        } catch (FileNotFoundException e) {

            // This should never happen
            throw new IllegalArgumentException("Could not read file", e);
        }
    }

    /**
     * Reads all (text) data from the supplied resource URL, returning it as a String.
     * All line feeds are converted to {@code System.getProperty("line.separator")}.
     *
     * @param resourceURL The non-empty resource URL to read data from.
     * @return The content of the supplied resource URL.
     */
    public static String readFile(final String resourceURL) {

        // Check sanity
        Validate.notEmpty(resourceURL, "Cannot handle null resourceURL argument.");

        // Peel off any initial '/' chars
        final String effectiveURL = resourceURL.startsWith("/") ? resourceURL.substring(1) : resourceURL;
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(effectiveURL);
        if (in == null) {
            throw new IllegalArgumentException("No file (or stream) found for resource [" + effectiveURL + "].");
        }

        // All done.
        return readFully(in, resourceURL);
    }

    /**
     * Maps the relative path of all files found under the supplied aDirectory to the files themselves.
     * Note that this operation consumes considerable amounts of resources (memory buffers) when mapping large file
     * trees. Therefore, it is wise to confine the aDirectory to smaller file trees.
     *
     * @param aDirectory A directory under which all Files are mapped.
     * @return A non-null SortedMap relating the relative paths of all Files found under the supplied aDirectory
     * to the Files themselves.
     */
    public static SortedMap<String, File> listFilesRecursively(final File aDirectory) {

        // Check sanity
        Validate.notNull(aDirectory, "Cannot handle null aDirectory argument.");
        Validate.isTrue(DIRECTORY_FILTER.accept(aDirectory),
                "Argument aDirectory must point to an existing directory. Got [" + getCanonicalPath(aDirectory) + "]");

        final SortedMap<String, File> toReturn = new TreeMap<>();
        populate(toReturn, aDirectory, aDirectory);

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    /**
     * Reads all data from the supplied InputStream, assumed to point to a character-based stream.
     * All line feeds are converted to {@code System.getProperty("line.separator")}.
     *
     * @param stream The non-null stream to read fully. The stream is closed before returning from this method,
     *               irrespective of the results of reading the stream.
     * @param desc   A description of the stream. Used within an Exception message should an IOException
     *               occur while reading the stream data.
     * @return The fully read text data.
     */
    @SuppressWarnings("all")
    private static String readFully(final InputStream stream, final String desc) {

        final StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            for (String aLine = reader.readLine(); aLine != null; aLine = reader.readLine()) {
                result.append(aLine).append(LINE_ENDING);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read data from [" + desc + "]", e);
        } finally {

            try {
                // Close the original stream.
                stream.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not close original stream for [" + desc + "]", e);
            }
        }

        // All done.
        return result.toString();
    }

    /**
     * Populates the provided SortedMap with the relative path of each File found under the rootDirectory,
     *
     * @param toPopulate       The SortedMap to populate.
     * @param currentDirectory The current directory.
     * @param rootDirectory    The root directory of the structure to populate, used to calculate the relative
     *                         path of the files found within the currentDirectory (i.e. the keys within the
     *                         toPopulate SortedMap).
     */
    private static void populate(final SortedMap<String, File> toPopulate,
                                 final File currentDirectory,
                                 final File rootDirectory) {

        // Calculate the relative path
        final String rootDirPath = getCanonicalPath(rootDirectory);
        final String currentDirPath = getCanonicalPath(currentDirectory);
        final String tmp = currentDirPath.substring(currentDirPath.indexOf(rootDirPath)
                + rootDirPath.length()
                + (rootDirPath.equals(currentDirPath) ? 0 : 1));
        final String prefix = tmp.length() == 0 ? "" : tmp + "/";

        // Map all files in the current directory
        for (File current : currentDirectory.listFiles(FILE_FILTER)) {
            final File existingFile = toPopulate.put(prefix + current.getName(), current);
            Validate.isTrue(existingFile == null, "Already mapped file at path [" + getCanonicalPath(current) + "]");
        }

        // Recurse
        for (File current : currentDirectory.listFiles(DIRECTORY_FILTER)) {
            populate(toPopulate, current, rootDirectory);
        }
    }
}
