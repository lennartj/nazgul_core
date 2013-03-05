/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.test.blueprint;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;

import java.net.URL;

/**
 * Helper to simplify the configuration of a Pax Exam container.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CommonOptions {

    /**
     * Default dependencies.properties resource path.
     */
    public static final String DEPENDENCY_RESOURCE = "META-INF/maven/dependencies.properties";

    /**
     * The Aries Blueprint standard artifacts.
     */
    public static final String[][] ARIES_ARTIFACTS = {
            {"org.apache.aries.blueprint", "org.apache.aries.blueprint"},
            {"org.apache.aries", "org.apache.aries.util"},
            {"org.apache.aries.proxy", "org.apache.aries.proxy"}
    };

    /**
     * The logback standard artifacts.
     */
    public static final String[][] LOGBACK_ARTIFACTS = {
            {"ch.qos.logback", "logback-core"},
            {"ch.qos.logback", "logback-classic"}
    };

    /**
     * The Nazgul core algorithms standard artifacts.
     */
    public static final String[][] CORE_ALGORITHMS = {
            {"org.apache.commons", "commons-lang3"},
            {"se.jguru.nazgul.core.algorithms.api", "nazgul-core-algorithms-api"}
    };

    /**
     * Creates a new CommonOptions instance using {@code DEPENDENCY_RESOURCE} for the maven dependencies.
     */
    public CommonOptions() {
        this(DEPENDENCY_RESOURCE);
    }

    /**
     * Creates a new CommonOptions instance,
     *
     * @throws IllegalStateException if the supplied dependencyResource instance could not be found.
     */
    public CommonOptions(final String dependencyResource) {

        // Check that we have a dependencies.properties file available.
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(dependencyResource);
        if (resource == null) {

            final String dependsMavenPluginExample = "      <plugin>\n"
                    + "        <groupId>org.apache.servicemix.tooling</groupId>\n"
                    + "        <artifactId>depends-maven-plugin</artifactId>\n"
                    + "        <version>1.2</version>\n"
                    + "        <executions>\n"
                    + "          <execution>\n"
                    + "            <id>generate-depends-file</id>\n"
                    + "            <goals>\n"
                    + "              <goal>generate-depends-file</goal>\n"
                    + "            </goals>\n"
                    + "          </execution>\n"
                    + "        </executions>\n"
                    + "      </plugin>\n";

            throw new IllegalStateException("Could not find resource [" + dependencyResource
                    + "]. Please create manually, or include the dependency-maven-plugin into your pom file.\n"
                    + "An example of such a plugin definition is found below:\n" + dependsMavenPluginExample);
        }
    }

    /**
     * @return a MavenArtifactProvisionOption array holding the logback-core and logback-classic
     *         dependencies using the versions supplied within the local pom.xml.
     */
    public MavenArtifactProvisionOption[] getLogbackOptions() {
        return convert(LOGBACK_ARTIFACTS);
    }

    /**
     * The MavenArtifactProvisionOptions required to include Aries Blueprint into the OSGi container.
     * Aries dependencies are expected to be included within the project effective pom.
     *
     * @return The MavenArtifactProvisionOptions required to include
     *         Aries Blueprint into the OSGi container.
     * @see #ARIES_ARTIFACTS
     */
    public MavenArtifactProvisionOption[] getAriesBlueprintOptions() {
        return convert(ARIES_ARTIFACTS);
    }

    /**
     * The MavenArtifactProvisionOptions required to include Nazgul Core Algorithms into the OSGi container.
     *
     * @return The MavenArtifactProvisionOptions required to include Nazgul Core Algorithms into the OSGi container.
     * @see #CORE_ALGORITHMS
     */
    public MavenArtifactProvisionOption[] getCoreAlgorithmsOptions() {
        return convert(CORE_ALGORITHMS);
    }

    //
    // Private helpers
    //

    private MavenArtifactProvisionOption[] convert(final String[][] groupAndArtifactIDs) {

        final MavenArtifactProvisionOption[] toReturn = new MavenArtifactProvisionOption[groupAndArtifactIDs.length];

        for (int i = 0; i < groupAndArtifactIDs.length; i++) {
            final String[] current = groupAndArtifactIDs[i];
            toReturn[i] = CoreOptions.mavenBundle(current[0], current[1]).versionAsInProject();
        }

        return toReturn;
    }
}
