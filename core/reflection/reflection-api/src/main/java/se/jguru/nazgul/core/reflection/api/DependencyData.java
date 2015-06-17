/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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

package se.jguru.nazgul.core.reflection.api;

import org.apache.commons.lang3.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String TYPE_LINE_TOKEN = DELIMITER + "type";
    private static final String SCOPE_LINE_TOKEN = DELIMITER + "scope";
    private static final String DEPENDENCY_RESOURCE = "META-INF/maven/dependencies.properties";
    private static final List<String> VALID_SCOPES =
            Arrays.asList("compile", "provided", "runtime", "test", "system", "import");

    // Internal state
    private final String groupId;
    private final String artifactId;
    private final String version;
    private String scope = "compile";
    private String type = "jar";

    /**
     * Creates DependencyData from the provided Maven GAV, using the default "compile" scope and "jar" type.
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
     * @return The scope of the dependency whose Group/Artifact/Version coordinates is wrapped by
     * this DependencyData instance. Unless updated, the scope defaults to "compile".
     */
    public String getScope() {
        return scope;
    }

    /**
     * Assigns the scope of this DependencyData instance.
     * The scope value should adhere to Maven's dependency scope definition.
     *
     * @param scope Should be one of the values {@code compile, provided, runtime, test, system or import}.
     * @throws java.lang.IllegalArgumentException if the scope value was not one of the permitted scopes.
     * @see <a href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism
     * .html#Dependency_Scope">The Maven Dependency Scopes</a>
     */
    public void setScope(final String scope) throws IllegalArgumentException {

        Validate.notEmpty(scope, "Cannot handle null or empty scope argument.");
        for (String current : VALID_SCOPES) {
            final String value = scope.toLowerCase().trim();
            if (value.equals(current)) {
                this.scope = value;
                return;
            }
        }

        throw new IllegalArgumentException("Scope [" + scope + "] not valid. Permitted scopes: " + VALID_SCOPES);
    }

    /**
     * @return The type of the dependency whose Group/Artifact/Version coordinates is wrapped by
     * this DependencyData instance. Unless updated, the type defaults to "jar".
     */
    public String getType() {
        return type;
    }

    /**
     * Assigns the type of this DependencyData instance.
     *
     * @param type The type of this DependencyData. Cannot be null or empty.
     */
    public void setType(final String type) {

        Validate.notEmpty(type, "Cannot handle null or empty ");
        this.type = type;
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
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (!(obj instanceof DependencyData)) {
            return false;
        } else if (obj == this) {
            return true;
        }

        // Delegate
        return this.hashCode() == obj.hashCode();
    }

    /**
     * Simply hashes the string representation of this DependencyData.
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

        final List<DependencyData> toReturn = new ArrayList<>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceStream = classLoader.getResourceAsStream(classpathRelativePath)) {

            if (resourceStream != null) {
                String aLine = null;

                try (BufferedReader in = new BufferedReader(new InputStreamReader(resourceStream))) {

                    // Temporary storage
                    DependencyData current = null;

                    while ((aLine = in.readLine()) != null) {

                        // The lines should come in the order
                        //
                        // versionLine,
                        // typeLine,
                        // scopeLine
                        //
                        if (aLine.contains(VERSION_LINE_TOKEN)) {

                            // some.group.name/artifactId/version = [version]
                            final List<String> data = parseLine(aLine);
                            current = new DependencyData(data.get(0), data.get(1), data.get(2));

                        } else if (aLine.contains(TYPE_LINE_TOKEN)) {

                            // some.group.name/artifactId/type = [type]
                            final List<String> data = parseLine(aLine);

                            boolean okState = current != null
                                    && current.getGroupId().equals(data.get(0))
                                    && current.getArtifactId().equals(data.get(1));

                            if (okState) {
                                current.setType(data.get(2));
                            } else {
                                throw new IllegalStateException("The type information for [" + current
                                        + "] is assumed to be found immediately after the version information.");
                            }
                        } else if (aLine.contains(SCOPE_LINE_TOKEN)) {

                            // some.group.name/artifactId/scope = [scope]
                            final List<String> data = parseLine(aLine);

                            boolean okState = current != null
                                    && current.getGroupId().equals(data.get(0))
                                    && current.getArtifactId().equals(data.get(1));

                            if (okState) {
                                current.setScope(data.get(2));
                                toReturn.add(current);
                                current = null;
                            } else {
                                throw new IllegalStateException("The scope information for [" + current
                                        + "] is assumed to be found after the version information.");
                            }
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

    //
    // Private helpers
    //

    private static List<String> parseLine(final String aLine) {

        final List<String> toReturn = new ArrayList<>();
        final StringTokenizer tok = new StringTokenizer(aLine, DELIMITER, false);
        if (tok.countTokens() != 3) {
            throw new IllegalStateException("Malformed dependency row [" + aLine + "].");
        }

        toReturn.add(tok.nextToken());
        toReturn.add(tok.nextToken());
        toReturn.add(aLine.substring(aLine.lastIndexOf("=") + 1).trim());

        return toReturn;
    }
}
