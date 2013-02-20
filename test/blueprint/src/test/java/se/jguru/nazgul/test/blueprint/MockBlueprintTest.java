/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.blueprint;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBlueprintTest extends AbstractBlueprintTest {

    /**
     * {@inheritDoc}
     */
    public MockBlueprintTest(final boolean scanClasspathForBundles) {
        super(scanClasspathForBundles);
    }

    /**
     * {@inheritDoc}
     */
    public MockBlueprintTest(boolean scanClasspathForBundles, String blueprintConfigurationPath) {
        super(scanClasspathForBundles, blueprintConfigurationPath);
    }
}
