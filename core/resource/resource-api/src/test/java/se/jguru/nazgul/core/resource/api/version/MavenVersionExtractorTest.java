/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
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
