/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */
package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.List;

/**
 * EventProducer/EventGenerator specification, which produces a single
 * type of event intended for a single type of EventConsumer.
 *
 * @param <T> The type of EventConsumer which can be registered to this EventProducer.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventProducer<T extends EventConsumer> extends Clusterable {

    /**
     * @return the type of [Event]Consumer handled by this EventProducer.
     */
    Class<T> getConsumerType();

    /**
     * Adds the provided listener to this EventProducer provided that the listener was not already added.
     * Silently ignores adding an already added EventConsumer instance.
     *
     * @param consumer The EventConsumer instance to add.
     */
    void addConsumer(T consumer);

    /**
     * @return a non-null List containing unique identifiers for all known/added EventConsumers.
     * @throws UnsupportedOperationException if this EventProducer cannot not provide the
     *                                       IDs of all known EventConsumers.
     */
    List<String> getConsumerIDs() throws UnsupportedOperationException;

    /**
     * Removes the EventConsumer with the given consumerID from this EventProducer.
     *
     * @param consumerID The unique identifier of the EventConsumer to remove.
     * @return {@code true} if the EventConsumer with the given ID was properly removed,
     *         and {@code false} otherwise.
     */
    boolean removeConsumer(String consumerID);

    /**
     * Acquires the registered EventConsumer with the provided consumerID.
     *
     * @param consumerID The unique identifier for the EventConsumer to retrieve
     * @return the EventConsumer with the given consumerID or {@code null} if none exists.
     */
    T getConsumer(String consumerID);

    /**
     * Perform a callback/notification to all registered EventConsumers using
     * the provided ConsumerEventCallback instance.
     *
     * @param consumerCallback an EventConsumer callback method, which cannot be  {@code null}.
     */
    void notifyConsumers(final EventConsumerCallback<T> consumerCallback);
}
