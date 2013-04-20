/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
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

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import se.jguru.nazgul.core.cache.impl.ehcache.helpers.DirectoryParserAgent;
import se.jguru.nazgul.core.parser.api.DefaultTokenParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

        // Delegate
        performCustomSetup();
    }

    protected void performCustomSetup() {}

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
