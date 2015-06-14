/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-ehcache
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
     * at the time of this method being called, and {@code false}
     * otherwise.
     */
    @Override
    public boolean isIdentifierAvailable() {
        return cacheManager != null;
    }
}
