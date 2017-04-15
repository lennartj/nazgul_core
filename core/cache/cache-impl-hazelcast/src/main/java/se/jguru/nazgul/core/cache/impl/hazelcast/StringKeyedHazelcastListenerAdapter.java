/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import se.jguru.nazgul.core.cache.api.CacheListener;

/**
 * Adapter delegating Hazelcast event calls to the CacheListener interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StringKeyedHazelcastListenerAdapter<T> extends AbstractHazelcastCacheListenerAdapter<String, T> {

    /**
     * Creates a new StringKeyedHazelcastListenerAdapter for a Cache instance.
     *
     * @param listener A cacheListener to which we the events of this HazelcastCacheListenerAdapter will be delegated.
     * @throws IllegalArgumentException if the listener argument is null.
     */
    public StringKeyedHazelcastListenerAdapter(final CacheListener<String, T> listener)
            throws IllegalArgumentException {
        super(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String convertFrom(final String distributedObjectId) {
        return "" + distributedObjectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T createFrom(final Object source) {
        return (T) source;
    }
}
