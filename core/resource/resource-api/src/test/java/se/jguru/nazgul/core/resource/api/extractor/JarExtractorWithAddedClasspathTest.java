/*
 * #%L
 * Nazgul Project: nazgul-core-resource-api
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.jguru.nazgul.core.resource.api.extractor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JarExtractorWithAddedClasspathTest {

    private static final String EXTRA_JAR_IN_CLASSPATH_PATH = "extractor/jar1.jar";
    private ClassLoader originalClassLoader;
    private File extractorSubDirectory;

    @Before
    public void setupSharedState() {

        // Find the target/test-classes/extractor directory.
        final URL resource = getClass().getClassLoader().getResource("extractor");
        Assert.assertNotNull(resource);
        extractorSubDirectory = new File(resource.getPath());
        Assert.assertTrue(extractorSubDirectory.exists() && extractorSubDirectory.isDirectory());

        // Stash the original ClassLoader
        final Thread activeThread = Thread.currentThread();
        originalClassLoader = activeThread.getContextClassLoader();

        final URL extraClassLoaderURL = getClass().getClassLoader().getResource(EXTRA_JAR_IN_CLASSPATH_PATH);
        Assert.assertNotNull("Could not find resource [" + EXTRA_JAR_IN_CLASSPATH_PATH + "]", extraClassLoaderURL);

        // Assign the local classloader.
        URLClassLoader extraJarClassLoader = new URLClassLoader(new URL[]{extraClassLoaderURL}, originalClassLoader);
        Thread.currentThread().setContextClassLoader(extraJarClassLoader);
    }

    @After
    public void teardownSharedState() {

        // Restore the original classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void validateLoadingResourceFromExtraJarUsingContextClassLoader() throws Exception {

        // Assemble
        final String resourcePath = "onlyInJar/textfiles/file1.txt";

        // Act
        final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(resourcePath);
        final List<URL> result = Collections.list(resources);

        // Assert
        Assert.assertEquals(1, result.size());
        System.out.println("Got: " + result.get(0).toString());
        Assert.assertTrue(result.get(0).toString().contains(resourcePath));
    }

    @Test
    public void validateExtractingDataFrom() throws MalformedURLException {

        // Assemble
        final String innerFilePart = "/foo/bar/jar1.jar!/onlyInJar/textfiles/file1.txt";
        final String filePart = "file:" + innerFilePart;
        final String pathPart = filePart + "?foo=bar";
        final String completeJarUrl = "jar:" + filePart;
        final URL jarURL = new URL(completeJarUrl);

        // Act & Assert
        Assert.assertEquals("jar", jarURL.getProtocol());
        Assert.assertEquals(filePart, jarURL.getPath());
        Assert.assertEquals(filePart, jarURL.getFile());

        // Act & Assert, part 2
        final URL innerURL = new URL(jarURL.getPath());
        Assert.assertEquals("file", innerURL.getProtocol());
        Assert.assertEquals(innerFilePart, innerURL.getFile());

        try {
            new URL(innerURL.getFile());
            Assert.fail("A MalformedURLException should be thrown when no protocol is supplied.");
        } catch (MalformedURLException e) {
            // Do nothing.
        } catch (Exception e) {
            Assert.fail("Expected MalformedURLException, but got " + e.getClass().getName());
        }
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullUrl() {

        // Act & Assert
        JarExtractor.getJarFileFor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonJarURL() throws MalformedURLException {

        // Assemble
        final URL nonJarURL = new URL("file:/some/file/url");

        // Act & Assert
        JarExtractor.getJarFileFor(nonJarURL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentJarFileURL() throws MalformedURLException {

        // Assemble
        final URL nonexistentJarFile = new URL("jar:file:/some/nonexistent/jarFile.jar!/foo/bar/baz.txt");

        // Act & Assert
        JarExtractor.getJarFileFor(nonexistentJarFile);
    }

    @Test
    public void validateGettingOkJarFile() throws Exception {

        // Assemble
        File targetExtractionDir = null;
        for(int i = 0; true; i++) {
            targetExtractionDir = new File(extractorSubDirectory, "validateGettingOkJarFile_" + i);
            if(!targetExtractionDir.exists()) {
                break;
            }
        }

        final String resourcePath = "onlyInJar/textfiles/file1.txt";
        final URL resourceInJarURL = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        final File expectedExtractedFile = new File(targetExtractionDir, resourcePath);

        // Act
        final JarFile foundJarFile = JarExtractor.getJarFileFor(resourceInJarURL);
        JarExtractor.extractResourcesFrom(foundJarFile, Pattern.compile("onlyInJar/.*"), targetExtractionDir, true);

        // Assert
        Assert.assertNotNull(foundJarFile);
        Assert.assertTrue(expectedExtractedFile.exists() && expectedExtractedFile.isFile());

        final BufferedReader in = new BufferedReader(new FileReader(expectedExtractedFile));
        final String aLine = in.readLine();
        Assert.assertEquals("number1", aLine);
    }
}
