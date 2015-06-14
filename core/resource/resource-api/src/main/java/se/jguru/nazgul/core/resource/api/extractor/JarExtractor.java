/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.core.resource.api.extractor;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Trivial utility which extracts resources from a (self-contained) JAR.
 * Also provides utility methods to simplify finding JarFiles originating from URLs to resources placed within JARs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class JarExtractor {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(JarExtractor.class);

    /**
     * Pattern matching all resource names.
     */
    public static final Pattern ALL_RESOURCES = Pattern.compile(".*");

    // Internal state
    private static final String JAR_PATH_SEPARATOR = "!";

    /**
     * Acquires the JarFile for the supplied packageResourceURL, which should be pointing to a resource packaged
     * within a JAR. The first/outermost protocol for the supplied packagedResourceURL must be "jar",
     * as a normal JAR resource URL has the form {@code jar:file:/some/path/aJarFile.jar!/path/in/jar/aFile.txt},
     * where the path part is operating system dependent. (For instance, the windows counterpart could start with
     * something like {@code jar:file:/C:/some/path/aJarFile.jar!/path/in/jar/aFile.txt}).
     *
     * @param packagedResourceURL a non-null URL pointing to a resource packaged within a JAR.
     * @return The JarFile for the JAR within which the packagedResourceURL is found.
     * @throws java.lang.IllegalArgumentException if the supplied URL did not correspond to a resource packaged
     *                                            within a JAR.
     */
    public static JarFile getJarFileFor(final URL packagedResourceURL) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(packagedResourceURL, "Cannot handle null packagedResourceURL argument.");

        final String jarProtocol = packagedResourceURL.getProtocol();
        Validate.isTrue("jar".equalsIgnoreCase(jarProtocol),
                "packagedResourceURL must have a 'jar' protocol. (Found: " + jarProtocol + ")");

        // Peel off all protocols, to find the path to the JarFile's File.
        URL innermostURL = packagedResourceURL;
        while (true) {
            try {
                innermostURL = new URL(innermostURL.getPath());
            } catch (MalformedURLException e) {
                // Expected
                break;
            }
        }

        final String path = innermostURL.getPath();
        final int exclamationIndex = path.indexOf(JAR_PATH_SEPARATOR);
        Validate.isTrue(exclamationIndex > 0, "Required JAR path separator [" + JAR_PATH_SEPARATOR
                + "] not found in URLs path [" + path + "], distilled from packagedResourceURL ["
                + packagedResourceURL.toString() + "].");

        final File jarFileFile = new File(path.substring(0, exclamationIndex));
        Validate.isTrue(jarFileFile.exists() && jarFileFile.isFile(), "Inconsistent JarFile ["
                + jarFileFile.getAbsolutePath() + "]: nonexistent or not a File, distilled from packagedResourceURL ["
                + packagedResourceURL.toString() + "].");

        // All seems well.
        try {
            return new JarFile(jarFileFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create a JarFile from File ["
                    + jarFileFile.getAbsolutePath() + "]", e);
        }
    }

    /**
     * Retrieves the JarEntry name for the supplied packagedResourceURL. Typically,
     * this value can be used to acquire a JarEntry for the
     * <p/>
     * <pre>
     *     <code>
     *         // Get the JarFile for a JAR-based URL
     *         final JarFile foundJarFile = JarExtractor.getJarFileFor(resourceInJarURL);
     *
     *         // Now, find the JarEntry name for the given resourceInJarURL
     *         final String name = JarExtractor.getEntryNameFor(resourceInJarURL);
     *
     *         // Get the JarEntry for the resourceInJarURL
     *         final JarEntry entry = foundJarFile.getJarEntry(name);
     *     </code>
     * </pre>
     *
     * @param packagedResourceURL A JAR URL, which must contain an '!' char. Typically on a form similar to
     *                            {@code jar:file:/some/path/aJarFile.jar!/path/in/jar/aFile.txt}
     * @return The entry name of the URL, which is the part following the '!/' char. Note that the first '/' must be
     * peeled off to retrieve a valid JarEntry name.
     */
    public static String getEntryNameFor(final URL packagedResourceURL) {

        // Check sanity
        Validate.notNull(packagedResourceURL, "Cannot handle null packagedResourceURL argument.");

        // Find the path for the supplied packagedResourceURL
        final String urlString = packagedResourceURL.toString();
        final String tmp = urlString.substring(urlString.indexOf(JAR_PATH_SEPARATOR) + 1);

        // Peel off the initial '/' to make a valid name.
        Validate.isTrue(tmp.startsWith("/"), "The absolute path within the JAR should start with a '/' char. Got ["
                + tmp + "]");
        return tmp.substring(1);
    }

    /**
     * Extracts all resources whose names matches the supplied resourceIdentifier from jarFile
     * to the targetDirectory. If targetDirectory does not exist and the createTargetDirectoryIfNonexistent
     * parameter is {@code true}, the directory (and any parent directories) will be created.
     *
     * @param jarFile                            The JAR file from which some resources should be extracted.
     * @param resourceIdentifier                 A Pattern matching the names of all resources which should be extracted.
     * @param targetDirectory                    The directory to which all resources should be extracted.
     * @param createTargetDirectoryIfNonexistent if {@code true}, the targetDirectory will be
     *                                           created if it does not already exist.
     */
    public static void extractResourcesFrom(final JarFile jarFile,
                                            final Pattern resourceIdentifier,
                                            final File targetDirectory,
                                            final boolean createTargetDirectoryIfNonexistent) {

        // Check sanity
        Validate.notNull(jarFile, "Cannot handle null jarFile argument.");
        Validate.notNull(resourceIdentifier, "Cannot handle null resourceIdentifier argument.");
        Validate.notNull(targetDirectory, "Cannot handle null targetDirectory argument.");

        if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target [" + targetDirectory.getAbsolutePath()
                    + "] exists and is not a directory.");
        }

        if (!createTargetDirectoryIfNonexistent && !targetDirectory.exists()) {
            throw new IllegalArgumentException("Target directory [" + targetDirectory.getAbsolutePath()
                    + "] does not exist - and instructed not to create it.");
        }

        // Do we need to create targetDir?
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new IllegalStateException("Could not create directory ["
                        + targetDirectory.getAbsolutePath() + "]");
            } else {
                targetDirectory.mkdirs();
            }
        }

        for (Enumeration<JarEntry> en = jarFile.entries(); en.hasMoreElements(); ) {

            // Dig out the entry and its internal path.
            final JarEntry current = en.nextElement();
            final String entryPath = current.getName();

            if (!current.isDirectory() && resourceIdentifier.matcher(entryPath).matches()) {

                // Extract the file at its relative path location.
                final File toWrite = new File(targetDirectory, entryPath);
                final File parentFile = toWrite.getParentFile();

                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }

                log.debug("Extracting [" + current.getName() + "] to [" + toWrite.getAbsolutePath() + "]");

                InputStream inStream = null;
                ReadableByteChannel inChannel = null;
                FileOutputStream outStream = null;
                FileChannel outChannel = null;

                try {
                    try {

                        // Copy using a NIO channel to improve performance.
                        inStream = jarFile.getInputStream(current);
                        inChannel = Channels.newChannel(inStream);
                        outStream = new FileOutputStream(toWrite);
                        outChannel = outStream.getChannel();
                        outChannel.transferFrom(inChannel, 0, current.getSize());

                    } finally {

                        // Close all opened NIO objects
                        if (inStream != null) {
                            inStream.close();
                        }
                        if (inChannel != null) {
                            inChannel.close();
                        }
                        if (outStream != null) {
                            outStream.close();
                        }
                        if (outChannel != null) {
                            outChannel.close();
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Could not create copy [" + entryPath + "] to ["
                            + targetDirectory.getAbsolutePath() + "]", e);
                }
            }
        }
    }
}
