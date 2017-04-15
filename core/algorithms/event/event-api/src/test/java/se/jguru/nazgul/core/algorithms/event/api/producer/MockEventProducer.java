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

package se.jguru.nazgul.core.algorithms.event.api.producer;

import se.jguru.nazgul.core.algorithms.event.api.consumer.MockEventConsumer;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEventProducer extends AbstractEventProducer<MockEventConsumer> {

    /**
     * Creates a new AbstractEventProducer with the provided IdGenerator and EventConsumer type.
     *
     * @param idGenerator The ID generator used to acquire a cluster-unique identifier for
     *                    this AbstractEventProducer instance.
     */
    public MockEventProducer(final IdGenerator idGenerator) {
        super(idGenerator, MockEventConsumer.class);
    }

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID A cluster-unique Identifier.
     */
    public MockEventProducer(final String clusterUniqueID) {
        super(clusterUniqueID, MockEventConsumer.class);
    }
}
