/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
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
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.cache.api.distributed.DistributedCache;

import java.util.concurrent.ExecutorService;

/**
 * Service interface definition for a distributed (clustered) cache which
 * can execute Tasks on Nodes within a distributed cache.
 * <p/>
 * If the underlying cache provides the ExecutorService capability, this is
 * the interface specifying how to interact with it.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DistributedExecutor<K, V> extends DistributedCache<K, V> {

    /**
     * @return The ExecutorService of the underlying cache implementation.
     */
    ExecutorService getExecutorService();
}
