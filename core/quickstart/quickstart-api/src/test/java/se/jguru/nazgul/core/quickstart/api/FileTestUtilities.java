/*
 * #%L
 * Nazgul Project: nazgul-core-quickstart-api
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
package se.jguru.nazgul.core.quickstart.api;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTimeZone;

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
                if (redirectSystemTmpDirectory) {
                    System.setProperty("java.io.tmpdir", toReturn.getAbsolutePath());
                }

                return toReturn;
            }
        }
    }
}
