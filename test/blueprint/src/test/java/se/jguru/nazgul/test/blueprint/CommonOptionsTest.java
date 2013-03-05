/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.test.blueprint;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.DependencyData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CommonOptionsTest {

    // Shared state
    private List<DependencyData> dependencyData;

    @Before
    public void setupSharedState() {

        dependencyData = DependencyData.parseDefaultPlacedDependencyPropertiesFile();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNonexistentDependencyResource() {

        // Act & Assert
        new CommonOptions("/a/non/existent/resource");
    }

    @Test
    public void validateLogbackOptions() {

        // Act & Assert
        validateCommonOption(CommonOptions.LOGBACK_ARTIFACTS, "getLogbackOptions");
    }

    @Test
    public void validateBlueprintOptions() {

        // Act & Assert
        validateCommonOption(CommonOptions.ARIES_ARTIFACTS, "getAriesBlueprintOptions");
    }

    //
    // Private helpers
    //

    private void validateCommonOption(final String[][] expected, final String methodName) {

        // Assemble
        MavenArtifactProvisionOption[] result;

        try {

            final Method getter = CommonOptions.class.getMethod(methodName, null);
            final CommonOptions unitUnderTest = new CommonOptions();

            // Act
            result = (MavenArtifactProvisionOption[]) getter.invoke(unitUnderTest, null);
        } catch (Exception e) {
            throw new IllegalStateException("Could not acquire MavenArtifactProvisionOptions", e);
        }

        // Assert
        Assert.assertEquals(expected.length, result.length);
        for(int i = 0; i < result.length; i++) {

            Assert.assertEquals(
                    getMavenArtifactProvisionOption(expected[i]),
                    result[i].getURL());
        }
    }

    private String getMavenArtifactProvisionOption(final String[] groupAndArtifact) {

        DependencyData dependencyData = getDependencyData(groupAndArtifact[0], groupAndArtifact[1]);

        return "mvn:" + dependencyData.getGroupId()
                + "/" + dependencyData.getArtifactId()
                + "/" + dependencyData.getVersion();
    }

    private DependencyData getDependencyData(final String groupId, final String artifactId) {

        final List<DependencyData> result = CollectionAlgorithms.filter(dependencyData, new Filter<DependencyData>() {
            @Override
            public boolean accept(final DependencyData candidate) {
                return candidate.getGroupId().equals(groupId) && candidate.getArtifactId().equals(artifactId);
            }
        });

        if(result.size() > 1 || result.size() == 0) {
            throw new IllegalStateException("Got incorrect number of results: " + result);
        }

        return result.get(0);
    }
}
