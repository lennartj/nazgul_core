/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-test
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.test.persistence.classloading;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.persistence.classloader.PersistenceRedirectionClassLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PersistenceRedirectionClassLoaderTest {

    // Shared state
    public static final String REDIRECTED = "testdata/classloading/redirectedPersistence.xml";
    public static final String NON_REDIRECTED = "testdata/classloading/standard.properties";
    private ClassLoader originalClassLoader;

    @Before
    public void setupSharedState() {

        // Stash the original ClassLoader
        final Thread activeThread = Thread.currentThread();
        originalClassLoader = activeThread.getContextClassLoader();
    }

    @After
    public void teardownSharedState() {

        // Restore the original classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullParentClassLoader() {

        // Act & Assert
        new PersistenceRedirectionClassLoader(null, "irrelevant");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyRedirectionTarget() {

        // Act & Assert
        new PersistenceRedirectionClassLoader(originalClassLoader, "");
    }

    @Test
    public void validateStandardThreadContextClassloaderTypeIsNotPersistenceRedirectionClassLoader() {

        // Assemble
        final ClassLoader activeClassloader = Thread.currentThread().getContextClassLoader();

        // Assert
        Assert.assertFalse(activeClassloader instanceof PersistenceRedirectionClassLoader);
    }

    @Test
    public void validateRedirectionOnlyHappensForPersistenceXml() throws Exception {

        // Assemble
        final PersistenceRedirectionClassLoader unitUnderTest = new PersistenceRedirectionClassLoader(
                originalClassLoader, REDIRECTED);

        Thread.currentThread().setContextClassLoader(unitUnderTest);

        // Act
        final Enumeration<URL> redirectedResources = unitUnderTest.getResources(
                PersistenceRedirectionClassLoader.PERSISTENCE_XML);
        final Enumeration<URL> nonRedirectedResources = unitUnderTest.getResources(NON_REDIRECTED);

        final ArrayList<URL> redirectedURLs = Collections.list(redirectedResources);
        final ArrayList<URL> nonRedirectedURLs = Collections.list(nonRedirectedResources);

        // Assert
        Assert.assertEquals(1, redirectedURLs.size());
        Assert.assertEquals(1, nonRedirectedURLs.size());

        Assert.assertTrue(redirectedURLs.get(0).getPath().endsWith(REDIRECTED));
        Assert.assertTrue(nonRedirectedURLs.get(0).getPath().endsWith(NON_REDIRECTED));
    }

    @Test
    public void validateRedirectionForSingleResource() throws Exception {

        // Assemble
        final PersistenceRedirectionClassLoader unitUnderTest = new PersistenceRedirectionClassLoader(
                originalClassLoader, REDIRECTED);

        Thread.currentThread().setContextClassLoader(unitUnderTest);

        // Act
        final URL redirectedResource = unitUnderTest.getResource(PersistenceRedirectionClassLoader.PERSISTENCE_XML);
        final URL nonRedirectedResource = unitUnderTest.getResource(NON_REDIRECTED);

        // Assert
        Assert.assertNotNull(redirectedResource);
        Assert.assertNotNull(nonRedirectedResource);

        Assert.assertTrue(redirectedResource.getPath().endsWith(REDIRECTED));
        Assert.assertTrue(nonRedirectedResource.getPath().endsWith(NON_REDIRECTED));
    }

    @Test
    public void validateRedirectionOnlyHappensForPersistenceXmlWithQuietLogging() throws Exception {

        // Assemble
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        // Call context.reset() to clear any previous configuration, e.g. default configuration.
        // For multi-step configuration, omit calling context.reset().
        context.reset();
        final URL quietLogbackConfig = getClass().getClassLoader().getResource("logback-test-quiet.xml");
        configurator.doConfigure(quietLogbackConfig);

        final PersistenceRedirectionClassLoader unitUnderTest = new PersistenceRedirectionClassLoader(
                originalClassLoader, REDIRECTED);

        Thread.currentThread().setContextClassLoader(unitUnderTest);

        // Act
        final Enumeration<URL> redirectedResources = unitUnderTest.getResources(
                PersistenceRedirectionClassLoader.PERSISTENCE_XML);
        final Enumeration<URL> nonRedirectedResources = unitUnderTest.getResources(NON_REDIRECTED);

        final ArrayList<URL> redirectedURLs = Collections.list(redirectedResources);
        final ArrayList<URL> nonRedirectedURLs = Collections.list(nonRedirectedResources);

        // Assert
        Assert.assertEquals(1, redirectedURLs.size());
        Assert.assertEquals(1, nonRedirectedURLs.size());

        Assert.assertTrue(redirectedURLs.get(0).getPath().endsWith(REDIRECTED));
        Assert.assertTrue(nonRedirectedURLs.get(0).getPath().endsWith(NON_REDIRECTED));
    }
}
