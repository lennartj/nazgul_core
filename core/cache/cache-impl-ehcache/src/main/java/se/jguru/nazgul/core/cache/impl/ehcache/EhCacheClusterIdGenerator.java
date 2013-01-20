/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.ehcache;

import net.sf.ehcache.CacheManager;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

/**
 * An IdGenerator retrieving the clusterUUID or name from a wrapped
 * EhCache CacheManager instance.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EhCacheClusterIdGenerator implements IdGenerator {

    // Internal state
    private CacheManager cacheManager;

    /**
     * Assigns the cacheManager of this EhCacheClusterIdGenerator.
     *
     * @param cacheManager The EhCache CacheManager instance.
     */
    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * @return A (cluster-)unique identifier for each call.
     */
    @Override
    public String getIdentifier() {

        // Initialize our ID part.
        String id = cacheManager.getClusterUUID();
        if ("".equals(id)) {

            // We are not running within an EhCache/Terracotta Cluster.
            // Revert to the name of the cacheManager, as
            // the cluster-wide UUID is not applicable.
            id = cacheManager.getName();
        }

        // All done.
        return id;
    }

    /**
     * @return {@code true} if this IdGenerator can deliver an identifier
     *         at the time of this method being called, and {@code false}
     *         otherwise.
     */
    @Override
    public boolean isIdentifierAvailable() {
        return cacheManager != null;
    }
}
