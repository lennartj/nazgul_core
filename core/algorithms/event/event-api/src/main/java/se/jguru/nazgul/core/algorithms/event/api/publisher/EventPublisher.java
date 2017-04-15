/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
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
package se.jguru.nazgul.core.algorithms.event.api.publisher;

import se.jguru.nazgul.core.algorithms.event.api.EventConsumerWrapper;

/**
 * Specification for an object publishing event objects (of any arbitrary class)
 * to a bus-like structure similar to in-process MessageProducers.
 * The specification for in-process MessageConsumers, as well as the registration
 * process between EventPublisher and -Consumer is left to concrete implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventPublisher<E, T> extends EventConsumerWrapper<T> {

    /**
     * Publishes an event (object) for consumption by registered consumers.
     *
     * @param event The event to publish. Should not be {@code null}.
     */
    void publish(E event);
}
