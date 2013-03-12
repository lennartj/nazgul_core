/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
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
