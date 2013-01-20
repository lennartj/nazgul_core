/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.ehcache.helpers;

import junit.framework.Assert;
import se.jguru.nazgul.core.parser.api.agent.AbstractParserAgent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DirectoryParserAgent extends AbstractParserAgent {

    public static final AtomicInteger testCounter = new AtomicInteger(0);

    /**
     * Token key indicating that the replacement value should
     * be acquired using the current project target directory, i.e:
     * <code>${project.basedir}/target</code>.
     */
    public static final String TARGET_DIRECTORY = "dir:target";

    /**
     * Token key indicating that the replacement value should
     * be acquired using a unique (i.e. nonexistent) subdirectory to the
     * current project target directory, i.e: <code>${project.basedir}/target</code>.
     */
    public static final String UNIQUE_SUBTARGET_DIRECTORY = "dir:uniqueSubTarget";

    // Internal state
    private File targetDirectory;
    private String targetDirectoryPath;

    /**
     * Default constructor.
     */
    public DirectoryParserAgent() {

        // Add the dynamic replacement tokens.
        dynamicTokens.add(TARGET_DIRECTORY);
        dynamicTokens.add(UNIQUE_SUBTARGET_DIRECTORY);

        // Find the target directory
        final String configFile = "ehcache/config/LocalHostUnitTestStandaloneConfig.xml";
        final URL resource = getClass().getClassLoader().getResource(configFile);
        final File configFileDir = new File(resource.getPath());

        targetDirectory = configFileDir.getParentFile().getParentFile().getParentFile().getParentFile();
        Assert.assertEquals("target", targetDirectory.getName().toLowerCase());
        targetDirectoryPath = getCanonicalPath(targetDirectory);
    }


    @Override
    protected String performDynamicReplacement(final String token) {

        if (token.equals(TARGET_DIRECTORY)) {
            return targetDirectoryPath;
        }
        if (token.equals(UNIQUE_SUBTARGET_DIRECTORY)) {

            File current = null;
            do {
                current = new File(targetDirectory, "subdir_" + testCounter.incrementAndGet());
            } while (current.exists());

            // Found a directory which does not exist.
            return getCanonicalPath(current);
        }

        throw new IllegalArgumentException("Cannot handle dynamic token [" + token + "].");
    }

    //
    // Private helpers
    //

    private String getCanonicalPath(final File fileOrDirectory) {

        try {
            return fileOrDirectory.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not acquire canonicalPath for [" + fileOrDirectory + "]: ", e);
        }
    }
}