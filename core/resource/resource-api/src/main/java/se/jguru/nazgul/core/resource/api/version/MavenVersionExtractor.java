/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.resource.api.version;

import org.apache.commons.lang3.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Helper class to read versions from a dependencies.properties file, as generated by the
 * {@code org.apache.servicemix.tooling:depends-maven-plugin}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class MavenVersionExtractor {

    // Constants
    private static final String DEPENDENCY_FILE_PATH = "META-INF/maven/dependencies.properties";
    private static final String VERSION_ROW_IDENTIFIER = "/version";
    private static final String EXCEPTION_MESSAGE = "Could not find dependency file ["
            + DEPENDENCY_FILE_PATH + "]. Ensure that the depends-maven-plugin is operative within the reactor.";

    /**
     * Retrieves a Reader connected to the {@code DEPENDENCY_FILE_PATH} as loaded by
     * the local thread's context ClassLoader.
     *
     * @return a Reader connected to the {@code DEPENDENCY_FILE_PATH} as loaded by
     *         the local thread's context ClassLoader.
     */
    public static Reader getDependenciesReader() {

        // Read the dependencies.properties file.
        URL resource = Thread.currentThread().getContextClassLoader().getResource(DEPENDENCY_FILE_PATH);
        if (resource == null) {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        // Open the reader to the stream.
        try {
            return new InputStreamReader(resource.openStream());
        } catch (IOException e) {
            throw new IllegalStateException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Acquires the version for the dependency with the supplied groupId
     * and artifactId from the supplied configuration [Reader]. The configuration
     * is assumed to be on the standard depends-maven-plugin form, i.e:
     * <p/>
     * <pre>
     *     ch.qos.logback/logback-classic/version = 1.0.6
     *     ch.qos.logback/logback-classic/type = jar
     *     ch.qos.logback/logback-classic/scope = compile
     * </pre>
     * <p/>
     * etc.
     *
     * @param groupId             The groupId for which the version should be acquired.
     * @param artifactId          The artifactId for which the version should be acquired.
     * @param configurationReader The Reader connected to the depends-maven-plugin file/stream.
     * @return The version of the given groupId and artifactId, or {@code null} if the version was
     *         not found within the supplied configurationReader.
     * @throws IllegalArgumentException if an exception was thrown while searching for the version
     *                                  for the given groupId and artifactId.
     * @throws IllegalStateException    if the format of a version line was incorrect.
     */
    public static String getDependencyVersion(final String groupId,
                                              final String artifactId,
                                              final Reader configurationReader)
            throws IllegalStateException {

        // Parse the data
        Validate.notNull(configurationReader, "Cannot handle null configurationReader argument.");
        Validate.notEmpty(groupId, "Cannot handle null groupId argument.");
        Validate.notEmpty(artifactId, "Cannot handle null artifactId argument.");

        // Assemble the key.
        final String requiredKey = groupId + "/" + artifactId + VERSION_ROW_IDENTIFIER;

        try {
            final BufferedReader in = new BufferedReader(configurationReader);
            for (String aLine = in.readLine(); aLine != null; aLine = in.readLine()) {

                if (aLine.trim().startsWith(requiredKey)) {

                    // Typical line format:
                    // ch.qos.logback/logback-classic/version = 1.0.6
                    final int equalsIndex = aLine.indexOf("=");
                    if (equalsIndex == -1) {

                        throw new IllegalStateException("Illegal format for dependency version definition ["
                                + requiredKey + "] - '=' sign missing.");
                    }

                    // All done.
                    return aLine.substring(equalsIndex + 1).trim();
                }
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Could not find value for key [" + requiredKey + "]", e);
        }

        // No version found.
        return null;
    }
}
