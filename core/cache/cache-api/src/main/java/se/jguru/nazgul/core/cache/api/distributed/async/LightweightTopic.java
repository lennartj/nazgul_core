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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.cache.api.distributed.async;

import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.io.Serializable;

/**
 * Lightweight Topic definition, implying a distributed cluster-wide Topic
 * which exists within the context of a distributed cache.
 *
 * @param <MessageType> T
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LightweightTopic<MessageType extends Serializable> extends Clusterable {

    /**
     * Sends/Publishes the provided message through this LightweightTopic.
     *
     * @param message The message to publish in this Topic.
     */
    void publish(MessageType message);

    /**
     * Adds a new LightweightTopicListener to this Topic.
     * The LightweightTopicListener is invoked whenever a message
     * is sent through this LightweightTopic.
     *
     * @param listener The LightweightTopicListener to register to this Topic.
     */
    void addListener(LightweightTopicListener<MessageType> listener);

    /**
     * Removes the provided LightweightTopicListener from this LightweightTopic.
     *
     * @param listener The listener to remove.
     */
    void removeListener(LightweightTopicListener<MessageType> listener);
}
