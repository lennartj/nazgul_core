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

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DependencyDataTest {

    // Shared state
    private String groupId = "testGroupId";
    private String artifactId = "testArtifactId";
    private String version = "1.2.3-SNAPSHOT";

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullGroupId() {

        // Act & Assert
        new DependencyData(null, artifactId, version);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullArtifactId() {

        // Act & Assert
        new DependencyData(groupId, null, version);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyVersion() {

        // Act & Assert
        new DependencyData(groupId, artifactId, "");
    }

    @Test
    public void validateParsingADependenciesFile() {

        // Assemble
        final String artifactId = "nazgul-tools-validation-api";
        final String path = "META-INF/maven/dependencies.properties";

        // Act
        final List<DependencyData> result = DependencyData.parse(path);

        // Assert
        Assert.assertNotNull(result);

        final Map<String, DependencyData> map = CollectionAlgorithms.map(
                result,
                new Transformer<DependencyData, Tuple<String, DependencyData>>() {
                    @Override
                    public Tuple<String, DependencyData> transform(final DependencyData input) {
                        return new Tuple<String, DependencyData>(input.getArtifactId(), input);
                    }
                });

        final DependencyData localDependencyData = map.get(artifactId);
        Assert.assertNotNull(localDependencyData);
        Assert.assertEquals("se.jguru.nazgul.tools.validation.api", localDependencyData.getGroupId());
    }

    @Test
    public void validateComparingDependencyDatas() {

        // Assemble
        final DependencyData info1 = new DependencyData(groupId, artifactId, version);
        final DependencyData info2 = new DependencyData(groupId, artifactId + "Foo", version);

        // Act
        final int result1 = info1.compareTo(info2);
        final int result2 = info1.compareTo(null);

        // Assert
        Assert.assertEquals(info1.toString().compareTo(info2.toString()), result1);
        Assert.assertEquals(Integer.MIN_VALUE, result2);
    }

    @Test
    public void validateStandardParsing() {

        // Act
        final List<DependencyData> dependencyDatas = DependencyData.parseDefaultPlacedDependencyPropertiesFile();

        // Assert
        Assert.assertNotNull(dependencyDatas);
        Assert.assertTrue(dependencyDatas.size() > 0);
        Assert.assertNotNull(dependencyDatas.get(0).toDependencyDataString());
    }
}
