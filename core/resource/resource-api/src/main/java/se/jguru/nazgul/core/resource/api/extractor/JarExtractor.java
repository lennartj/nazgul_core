/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.resource.api.extractor;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Trivial utility which extracts resources from a (self-contained) JAR.
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

    /**
     * Extracts all resources whose names matches the supplied resourceIdentifier from jarFile
     * to the targetDirectory. If targetDirectory does not exist and the createTargetDirectoryIfNonexistent
     * parameter is {@code true}, the directory (and any parent directories) will be created.
     *
     * @param jarFile            The JAR file from which some resources should be extracted.
     * @param resourceIdentifier A Pattern matching the names of all resources which should be extracted.
     * @param targetDirectory    The directory to which all resources should be extracted.
     * @param createTargetDirectoryIfNonexistent
     *                           if {@code true}, the targetDirectory will be
     *                           created if it does not already exist.
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

                // Extract the native file.
                final String fileName = entryPath.substring(entryPath.lastIndexOf("/") + 1);
                final File toWrite = new File(targetDirectory, fileName);

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
