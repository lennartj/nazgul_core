/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.reflection.api;

import org.apache.commons.lang3.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Maven dependency information wrapper, as read from a dependency.properties file
 * written by the maven-dependency-plugin.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DependencyData implements Comparable<DependencyData> {

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
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(final DependencyData o) {

        if (o == null) {
            return Integer.MIN_VALUE;
        }

        return toString().compareTo(o.toString());
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
        final InputStream resourceStream = classLoader.getResourceAsStream(classpathRelativePath);

        if (resourceStream != null) {
            final BufferedReader in = new BufferedReader(new InputStreamReader(resourceStream));
            String aLine = null;
            try {
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
                e.printStackTrace();
            }
        }

        // All done
        return toReturn;
    }
}
