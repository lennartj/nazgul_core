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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.quickstart.model.SimpleArtifact;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A suite of utility algorithms for use with Files and related structures.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class FileUtils {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class.getName());

    /**
     * The line ending string,
     */
    public static final String LINE_ENDING = System.getProperty("line.separator");

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
        Validate.isTrue(aPomFile.exists() && aPomFile.isFile(), "File [" + getCanonicalPath(aPomFile)
                + "] must exist and be a File.");

        try {
            return pomReader.read(new FileReader(aPomFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read POM file [" + getCanonicalPath(aPomFile) + "]", e);
        }
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
        return fileOrDir.exists() && (isDirectory ? fileOrDir.isDirectory() : fileOrDir.isFile());
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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aFile))) {
            writer.write(data);
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
        Validate.isTrue(aFile.exists() && aFile.isFile(), "File [" + getCanonicalPath(aFile)
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
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceURL);
        Validate.notNull(in, "Resource [" + resourceURL + "] yielded null input stream.");

        // All done.
        return readFully(in, resourceURL);
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
            result.append(reader.readLine()).append(LINE_ENDING);
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
}
