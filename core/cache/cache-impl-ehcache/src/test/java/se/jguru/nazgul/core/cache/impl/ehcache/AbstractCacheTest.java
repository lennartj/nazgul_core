/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import junit.framework.Assert;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import se.jguru.nazgul.core.cache.impl.ehcache.helpers.DirectoryParserAgent;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;
import se.jguru.nazgul.core.parser.api.agent.DefaultParserAgent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCacheTest {

    private NonDistributedEhCache cache;

    /**
     * @return The LocalEhCache instance.
     */
    protected final NonDistributedEhCache getCache() {
        return cache;
    }

    /**
     * @return The classpath-relative configuration file for EhCache to be used within this TestCase.
     */
    protected abstract String getEhCacheConfiguration();

    @Before
    public void createEhCache() {

        final CacheManager cacheManager = getTokenizedCacheManager(getEhCacheConfiguration());
        cache = new NonDistributedEhCache(cacheManager);
        Assert.assertNotNull("Created null cache.", cache);
    }

    @After
    public void shutdownLocalEhCache() {
        NonDistributedEhCache.shutdownCache(cache);
    }

    //
    // Private helpers
    //

    public static CacheManager getTokenizedCacheManager(final String configuration) {

        // Read and parse the configuration data
        DefaultTokenParser parser = new DefaultTokenParser();
        parser.addAgent(new DirectoryParserAgent());

        final String ehCacheConfiguration = parser.substituteTokens(readFully(configuration));
        return new CacheManager(new ByteArrayInputStream(ehCacheConfiguration.getBytes()));
    }

    private static String readFully(final String path) {

        final InputStream resource = AbstractCacheTest.class.getClassLoader().getResourceAsStream(path);

        final BufferedReader tmp = new BufferedReader(new InputStreamReader(resource));
        final StringBuilder toReturn = new StringBuilder(50);

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }
}
