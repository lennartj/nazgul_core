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
package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.EventConsumerWrapper;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.clustering.api.Clusterable;

/**
 * EventProducer/EventGenerator specification, which produces a single
 * type of event intended for a single type of EventConsumer.
 * EventProducer instances should function correctly in a clustered environment.
 *
 * @param <T> The type of EventConsumer which can be registered to this EventProducer.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventProducer<T extends EventConsumer> extends Clusterable, EventConsumerWrapper<T> {

    /**
     * @return the type of [Event]Consumer handled by this EventProducer.
     */
    Class<T> getConsumerType();

    /**
     * Perform a callback/notification to all registered EventConsumers using
     * the provided ConsumerEventCallback instance.
     *
     * @param consumerCallback an EventConsumer callback method, which cannot be  {@code null}.
     */
    void notifyConsumers(final EventConsumerCallback<T> consumerCallback);
}
