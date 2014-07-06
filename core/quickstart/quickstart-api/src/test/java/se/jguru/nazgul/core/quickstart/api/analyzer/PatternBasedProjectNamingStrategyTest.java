/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.quickstart.api.analyzer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternBasedProjectNamingStrategyTest {

    // Shared state
    private File projectRootDir;

    @Before
    public void setupSharedState() {

        final URL fooDirURL = getClass().getClassLoader().getResource("testdata/foo");
        projectRootDir = new File(fooDirURL.getPath());
        Assert.assertNotNull(projectRootDir);
        Assert.assertTrue(projectRootDir.exists() && projectRootDir.isDirectory());
    }

    @Test
    public void validateNamingStandard() {

        // Assemble
        final NazgulFooProjectNamingStrategy unitUnderTest = new NazgulFooProjectNamingStrategy();

        // Act

        // Assert
    }
}
