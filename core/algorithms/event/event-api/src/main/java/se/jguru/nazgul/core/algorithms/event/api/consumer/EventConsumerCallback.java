/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;

import java.io.Serializable;

/**
 * Specification for how to perform an event callback on an EventConsumer.
 * Pattern-wise, this is a Visitor.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface EventConsumerCallback<T extends EventConsumer> extends Serializable {

    /**
     * Performs a callback on the provided EventConsumer [subclass] instance.
     *
     * @param eventConsumer The EventConsumer subclass instance.
     */
    void onEvent(T eventConsumer);
}