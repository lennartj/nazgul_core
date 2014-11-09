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

import java.io.Serializable;

/**
 * Service interface definition for a distributed (clustered) cache which
 * provides Destinations leading into a distributed cache.
 *
 * @param <K> The type of Keys used within the DistributedCache.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DestinationProvider<K, V> extends DistributedCache<K, V> {

    /**
     * Retrieves a LightweightTopic with the provided topicId from
     * the DestinationProvider. Depending on the capabilities of the underlying
     * implementation, the topic can be dynamically
     *
     * @param <MessageType> The type of message transmitted by this LightweightTopic.
     * @param topicId       The ID of the LightweightTopic to retrieve.
     * @return The LightweightTopic with the provided topicId.
     */
    <MessageType extends Serializable> LightweightTopic<MessageType> getTopic(String topicId);
}
