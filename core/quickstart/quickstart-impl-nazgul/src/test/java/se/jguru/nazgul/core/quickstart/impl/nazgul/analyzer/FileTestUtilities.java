/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-impl-nazgul
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
package se.jguru.nazgul.core.quickstart.impl.nazgul.analyzer;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class FileTestUtilities {

    /**
     * Swedish TimeZone, used to manage times and dates.
     */
    public static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");

    /**
     * Swedish DateTimeZone, used to convert times and dates.
     */
    public static final DateTimeZone SWEDISH_DATETIME_ZONE = DateTimeZone.forTimeZone(SWEDISH_TIMEZONE);

    // Our log
    private static final Logger log = LoggerFactory.getLogger(FileTestUtilities.class.getName());

    // Internal state
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final Object[] lock = new Object[0];
    private static String originalTmpDirectory;

    /**
     * Creates an empty directory under the target folder, and optionally redirects the java.io.tmpdir to it.
     *
     * @param redirectSystemTmpDirectory if {@code true}, redirects the java.io.tmpdir property to
     *                                   the returned directory.
     * @return A temporary directory.
     */
    public static File createTmpDirectory(boolean redirectSystemTmpDirectory) {

        // Find the testdata directory
        final URL testdata = Thread.currentThread().getContextClassLoader().getResource("testdata");
        Validate.notNull(testdata, "Could not find 'testdata' URL.");

        final File testDataDir = new File(testdata.getPath());
        Validate.isTrue(testDataDir.exists(), "TestDataDir did not exist.");
        Validate.isTrue(testDataDir.isDirectory(), "TestDataDir was not a directory.");

        for (int i = 0; true; i++) {
            File toReturn = new File(testDataDir, "tmpdir_" + i);

            if (!toReturn.exists()) {

                synchronized (lock) {

                    // Ensure that the directory exists
                    final boolean success = toReturn.mkdirs();
                    if(!success) {
                        log.warn("Could not create directory [" + toReturn.getAbsolutePath() + "] using mkdirs.");
                    } else {
                        log.info("Created directory [" + toReturn.getAbsolutePath() + "] for temporary io tmp dir.");
                    }

                    if (redirectSystemTmpDirectory) {

                        if (originalTmpDirectory != null) {
                            throw new IllegalStateException("Cannot redirect property '" + JAVA_IO_TMPDIR
                                    + "' twice. Please ");
                        }

                        // Stash the original tmpdir
                        originalTmpDirectory = toReturn.getAbsolutePath();
                    }

                    // Redirect
                    System.setProperty(JAVA_IO_TMPDIR, toReturn.getAbsolutePath());
                }

                // All done.
                return toReturn;
            }
        }
    }

    /**
     * Restores the java.io.tmpdir to its original value, assuming
     * that it was first redirected.
     */
    public static void restoreOriginalTmpDirectory() {

        // Check sanity
        if (originalTmpDirectory == null) {
            throw new IllegalStateException("Cannot restore original '" + JAVA_IO_TMPDIR
                    + "' directory before redirecting it.");
        }

        // Restore the original java.io.tmpdir
        synchronized (lock) {
            System.setProperty(JAVA_IO_TMPDIR, originalTmpDirectory);
            originalTmpDirectory = null;
        }
    }
}
