/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import org.junit.After;
import org.junit.Before;

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
        cache = new NonDistributedEhCache(getEhCacheConfiguration());
    }

    @After
    public void shutdownLocalEhCache() {
        NonDistributedEhCache.shutdownCache(cache);
    }
}
