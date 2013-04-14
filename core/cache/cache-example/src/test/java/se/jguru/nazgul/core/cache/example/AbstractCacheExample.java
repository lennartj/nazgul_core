/*
 * #%L
 * Nazgul Project: nazgul-core-cache-example
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
