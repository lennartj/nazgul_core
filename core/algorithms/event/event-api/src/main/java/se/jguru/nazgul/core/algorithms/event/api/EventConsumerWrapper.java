/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
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
package se.jguru.nazgul.core.algorithms.event.api;

import java.util.List;

/**
 * Specification for how to add, remove and list event consumers of a certain type.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventConsumerWrapper<T> {

    /**
     * Adds the provided listener to this EventProducer provided that the listener was not already added.
     * Silently ignores adding an already added EventConsumer instance.
     *
     * @param consumer The EventConsumer instance to add.
     * @return The consumerID of the registered consumer.
     */
    String addConsumer(T consumer);

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
     * and {@code false} otherwise.
     */
    boolean removeConsumer(String consumerID);

    /**
     * Acquires the registered EventConsumer with the provided consumerID.
     *
     * @param consumerID The unique identifier for the EventConsumer to retrieve
     * @return the EventConsumer with the given consumerID or {@code null} if none exists.
     */
    T getConsumer(String consumerID);
}
