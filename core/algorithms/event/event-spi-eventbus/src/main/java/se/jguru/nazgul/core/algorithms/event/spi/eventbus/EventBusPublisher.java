/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-spi-eventbus
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.algorithms.event.spi.eventbus;

import com.google.common.eventbus.EventBus;
import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.algorithms.event.api.publisher.EventPublisher;
import se.jguru.nazgul.core.clustering.api.Clusterable;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.clustering.api.UUIDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventPublisher implementation using the
 * <a href="https://code.google.com/p/guava-libraries/wiki/EventBusExplained">Google Guava EventBus</a> to publish
 * events to listeners. This EventPublisher implementation should only be used in-process.
 * (For out-of process publishing, simply use Messaging such as JMS instead).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventBusPublisher implements EventPublisher<Object, Object> {

    // Internal state
    private IdGenerator idGenerator;
    private EventBus eventBus;
    private Map<String, Object> registeredListeners;

    /**
     * Convenience constructor creating a new EventBusPublisher wrapping the supplied EventBus instance,
     * and using a UUIDGenerator which generates IDs.
     *
     * @param eventBus The EventBus wrapped by this EventPublisher instance.
     */
    public EventBusPublisher(final EventBus eventBus) {
        this(new UUIDGenerator(), eventBus);
    }

    /**
     * Compound constructor, creating a new EventBusPublisher instance wrapping the supplied data.
     *
     * @param idGenerator The IdGenerator used to generate cluster-unique IDs for each added consumer.
     * @param eventBus    The EventBus wrapped by this EventPublisher instance.
     */
    public EventBusPublisher(final IdGenerator idGenerator,
                             final EventBus eventBus) {

        // Check sanity
        Validate.notNull(idGenerator, "Cannot handle null idGenerator argument.");
        Validate.notNull(eventBus, "Cannot handle null eventBus argument.");

        // Assign internal state
        this.idGenerator = idGenerator;
        this.eventBus = eventBus;
        this.registeredListeners = new HashMap<String, Object>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final Object event) {

        // Check sanity
        Validate.notNull(event, "Cannot handle null event argument.");

        // Publish the event.
        eventBus.post(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addConsumer(final Object consumer) {

        // Check sanity
        Validate.notNull(consumer, "Cannot handle null consumer argument.");

        // Register the consumer, unless already registered.
        String consumerID = null;
        for (Map.Entry<String, Object> current : registeredListeners.entrySet()) {
            if (consumer == current.getValue()) {

                // Cannot register a consumer twice.
                consumerID = current.getKey();
            }
        }

        if (consumerID == null) {

            // Retrieve the ConsumerID.
            consumerID = extractConsumerID(consumer);

            try {
                registeredListeners.put(consumerID, consumer);
                eventBus.register(consumer);
            } catch (Exception e) {

                // Could not register the consumer?
                eventBus.unregister(consumer);
                registeredListeners.remove(consumerID);

                // Re-throw
                throw new IllegalStateException("Could not register consumer of type ["
                        + consumer.getClass().getSimpleName() + "]", e);
            }
        }

        // All done.
        return consumerID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getConsumerIDs() throws UnsupportedOperationException {
        return new ArrayList<String>(registeredListeners.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeConsumer(final String consumerID) {

        // Check sanity
        Validate.notNull(consumerID, "Cannot handle null consumerID argument.");

        // Remove the consumer.
        final Object justRemoved = registeredListeners.remove(consumerID);
        if (justRemoved == null) {
            return false;
        }

        // Un-register and return
        eventBus.unregister(justRemoved);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getConsumer(final String consumerID) {

        // Check sanity
        Validate.notNull(consumerID, "Cannot handle null consumerID argument.");

        // All done.
        return registeredListeners.get(consumerID);
    }

    /**
     * Extracts the consumerID from the supplied Event consumer.
     * The default implementation uses the clusterID if the consumer is {@code Clusterable},
     * and otherwise simply generates a new ID using the internal IdGenerator.
     *
     * @param consumer The non-null consumer object from which the ID should be extracted.
     * @return The extracted ConsumerID.
     * @see Clusterable
     */
    protected String extractConsumerID(final Object consumer) {

        // Check sanity
        Validate.notNull(consumer, "Cannot handle null consumer argument.");

        // Extract the ID in any way possible.
        String toReturn = null;
        if (consumer instanceof Clusterable) {
            toReturn = ((Clusterable) consumer).getClusterId();
        } else {
            toReturn = idGenerator.getIdentifier();
        }

        // All done.
        return toReturn;
    }
}
