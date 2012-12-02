/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.resource.api.version;

import org.junit.Assert;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MavenVersionExtractorTest {

    @Test
    public void validateReaderExistsForStandardProjects() {

        // Act
        final Reader dependenciesReader = MavenVersionExtractor.getDependenciesReader();

        // Assert
        Assert.assertNotNull(dependenciesReader);
    }

    @Test
    public void validateExtractingVersionFromThisProject() {

        // Assemble
        final String groupId = "org.slf4j";
        final String artifactId = "slf4j-api";

        // Act
        final String result = MavenVersionExtractor.getDependencyVersion(
                groupId,
                artifactId,
                MavenVersionExtractor.getDependenciesReader());

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(result.trim(), result);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnIncorrectlyFormattedDependency() {

        // Assemble
        final StringReader fakeSource = new StringReader("se.jguru.foo/bar/version");

        // Act
        MavenVersionExtractor.getDependencyVersion("se.jguru.foo", "bar", fakeSource);
    }

    @Test
    public void validateNullReturnForUnknownArtifact() {

        // Assemble
        final StringReader fakeSource = new StringReader("se.jguru.foo/bar/version");

        // Act
        final String result = MavenVersionExtractor.getDependencyVersion("se.jguru.bar", "gnat", fakeSource);

        // Assert
        Assert.assertNull(result);
    }
}
