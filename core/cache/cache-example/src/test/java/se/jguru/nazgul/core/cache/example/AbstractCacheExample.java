/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.example;

import com.hazelcast.core.Hazelcast;
import org.junit.AfterClass;
import se.jguru.nazgul.core.cache.api.distributed.async.DestinationProvider;
import se.jguru.nazgul.core.cache.impl.hazelcast.clients.HazelcastCacheMember;

/**
 * Common functionality for cache examples.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCacheExample {

    // Shared state
    private DestinationProvider<String> cache;

    /**
     * Shutdown the hazelcast cache instance.
     */
    @AfterClass
    public static void teardownHazelcastCacheInstance() {
        Hazelcast.shutdownAll();
        Hazelcast.shutdownAll();
    }

    /**
     * @return The created DestinationProvider instance. If none is available, one is created.
     */
    protected DestinationProvider<String> getCache() {

        if(cache == null) {
            cache = getCache("config/hazelcast/StandaloneConfig.xml");
        }

        // All done.
        return cache;
    }

    //
    // Private helpers
    //

    /**
     * Acquires a HazelcastCacheMember instance, using the supplied configuration file.
     *
     * @param configFile The classpath-relative resource path to a Hazelcast configuration file.
     * @return A HazelcastCacheMember instance, configured by the supplied configuration file.
     */
    private static HazelcastCacheMember getCache(final String configFile) {

        // Add a local, non-loopback interface.
        return new HazelcastCacheMember(HazelcastCacheMember.readConfigFile(configFile));
    }
}
