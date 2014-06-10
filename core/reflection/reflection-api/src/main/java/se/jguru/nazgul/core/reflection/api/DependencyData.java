/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.jguru.nazgul.core.reflection.api;

import org.apache.commons.lang3.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Maven dependency information wrapper, as read from a dependency.properties file
 * written by the maven-dependency-plugin.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DependencyData implements Serializable, Comparable<DependencyData> {

    // Constants
    private static final String DELIMITER = "/";
    private static final String VERSION_LINE_TOKEN = DELIMITER + "version";
    private static final String DEPENDENCY_RESOURCE = "META-INF/maven/dependencies.properties";

    // Internal state
    private final String groupId;
    private final String artifactId;
    private final String version;

    /**
     * Creates DependencyData from the provided Maven GAV.
     *
     * @param groupId    The groupId of this DependencyData.
     * @param artifactId The artifactId of this DependencyData.
     * @param version    The version of this DependencyData.
     */
    public DependencyData(final String groupId, final String artifactId, final String version) {

        // Check sanity
        Validate.notEmpty(groupId, "Cannot handle null or empty groupId argument.");
        Validate.notEmpty(artifactId, "Cannot handle null or empty artifactId argument.");
        Validate.notEmpty(version, "Cannot handle null or empty version argument.");

        // Assign internal state
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * @return The groupId of this DependencyData instance.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return The artifactId of this DependencyData instance.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return The version of this DependencyData instance.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Simply uses the string-wise hashCode method.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if(!(obj instanceof DependencyData)) {
            return false;
        } else if(obj == this) {
            return true;
        }

        // Delegate
        return this.hashCode() == obj.hashCode();
    }

    /**
     * Simply hashes the string representation of this DependencyData.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final DependencyData that) {

        // Check sanity
        Validate.notNull(that, "Cannot handle null DependencyData argument.");

        // Simply compare the string forms of the two DependencyData objects
        final String thisStringForm = toString();
        return thisStringForm.compareTo(that.toString());
    }

    /**
     * @return a string representation of this object.
     */
    @Override
    public String toString() {
        return getGroupId() + DELIMITER + getArtifactId() + DELIMITER + getVersion();
    }

    /**
     * @return a synthesized dependency version info string for this DependencyData instance.
     */
    public String toDependencyDataString() {
        return getGroupId() + DELIMITER + getArtifactId() + DELIMITER + "version = " + getVersion();
    }

    /**
     * Parses a dependency.properties file, found at the default location.
     *
     * @return A List of fully populated DependencyInfo instances.
     */
    public static List<DependencyData> parseDefaultPlacedDependencyPropertiesFile() {
        return parse(DEPENDENCY_RESOURCE);
    }

    /**
     * Parses a dependency.properties file, found at the provided classpathRelativePath.
     *
     * @param classpathRelativePath The path relative to classpath where the dependency.properties file resides.
     * @return A List of fully populated DependencyInfo instances.
     */
    public static List<DependencyData> parse(final String classpathRelativePath) {

        final List<DependencyData> toReturn = new ArrayList<DependencyData>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceStream = classLoader.getResourceAsStream(classpathRelativePath)) {

            if (resourceStream != null) {
                String aLine = null;

                try (BufferedReader in = new BufferedReader(new InputStreamReader(resourceStream))) {
                    while ((aLine = in.readLine()) != null) {

                        if (aLine.contains(VERSION_LINE_TOKEN)) {

                            // some.group.name/artifactId/version = [version]
                            StringTokenizer tok = new StringTokenizer(aLine, DELIMITER, false);
                            if (tok.countTokens() != 3) {
                                throw new IllegalStateException("Malformed dependency row [" + aLine + "].");
                            }

                            final String groupId = tok.nextToken();
                            final String artifactId = tok.nextToken();
                            final String tmp = tok.nextToken().trim();
                            final String version = tmp.substring(tmp.lastIndexOf('=') + 1).trim();

                            toReturn.add(new DependencyData(groupId, artifactId, version));
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Could not read resource from [" + classpathRelativePath + "]", e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not obtain resource from [" + classpathRelativePath + "]", e);
        }

        // All done
        return toReturn;
    }
}
