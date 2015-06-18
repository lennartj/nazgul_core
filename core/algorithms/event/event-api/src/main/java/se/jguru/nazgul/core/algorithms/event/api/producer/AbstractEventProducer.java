/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-api
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
package se.jguru.nazgul.core.algorithms.event.api.producer;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.clustering.api.AbstractClusterable;
import se.jguru.nazgul.core.clustering.api.ConstantIdGenerator;
import se.jguru.nazgul.core.clustering.api.IdGenerator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract implementation of the EventProducer interface, sporting Clusterable behaviour.
 * That is - this AbstractEventProducer implementation is intended for use within a cluster.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://www.jguru.se/nazgul/core", propOrder = {"tClass", "consumers"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEventProducer<T extends EventConsumer>
        extends AbstractClusterable implements EventProducer<T> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractEventProducer.class);

    // Internal state
    @XmlAttribute(required = true)
    private Class<T> tClass;
    private ConcurrentMap<String, T> consumers;

    /**
     * Creates a new AbstractEventProducer with the provided IdGenerator and EventConsumer type.
     *
     * @param idGenerator        The ID generator used to acquire a cluster-unique identifier for
     *                           this AbstractEventProducer instance.
     * @param eventConsumerClass The type of EventConsumer handled by this AbstractEventProducer.
     */
    public AbstractEventProducer(final IdGenerator idGenerator, final Class<T> eventConsumerClass) {
        super(idGenerator);

        // Check sanity
        Validate.notNull(eventConsumerClass, "Cannot handle null eventConsumerClass argument.");

        // Assign internal state
        this.tClass = eventConsumerClass;
        this.consumers = new ConcurrentHashMap<String, T>();
    }

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID    A cluster-unique Identifier.
     * @param eventConsumerClass The type of EventConsumer handled by this AbstractEventProducer.
     */
    public AbstractEventProducer(final String clusterUniqueID, final Class<T> eventConsumerClass) {
        this(new ConstantIdGenerator(clusterUniqueID), eventConsumerClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Class<T> getConsumerType() {
        return tClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String addConsumer(final T consumer) {

        String toReturn = null;

        // Check sanity
        if (consumer != null) {

            // Already registered?
            final String consumerID = consumer.getClusterId();
            if (consumers.containsKey(consumerID)) {
                log.warn("Consumer with id [" + consumerID + "] already registered.");
            } else if (registerConsumerToEventProducers(consumer)) {

                // Register the consumer
                consumers.put(consumerID, consumer);

                // Notify any listeners about the newly added consumer
                try {
                    onConsumerRegistered(getClusterId(), consumer);
                } catch (final Exception e) {

                    log.error("Could not properly notify other AbstractEventProducer about adding an EventConsumer.",
                            e);
                }
            }

            toReturn = consumerID;
        }

        // All done.
        return toReturn;
    }

    /**
     * @return an unmodifiable List holding the IDs of all known EventConsumers.
     * @throws UnsupportedOperationException if this EventProducer cannot not provide the
     *                                       IDs of all known EventConsumers.
     */
    @Override
    public final List<String> getConsumerIDs() throws UnsupportedOperationException {
        return Collections.unmodifiableList(new ArrayList<String>(consumers.keySet()));
    }

    /**
     * Removes the EventConsumer with the given consumerID from this EventProducer.
     *
     * @param consumerID The unique identifier of the EventConsumer to remove.
     */
    @Override
    public final boolean removeConsumer(final String consumerID) {

        // Check sanity
        if (consumerID == null || consumerID.equals("")) {
            log.warn("Ignoring removing EventConsumer with null or empty ID.");
            return false;
        }

        // Does the listener exist locally?
        T removed = consumers.remove(consumerID);
        if (removed != null) {

            // Delegate to de-register the EventConsumer from its EventProducers.
            removeConsumerFromEventProducers(removed);
            return true;
        }

        // Send a removeListener event message to the other cluster EventProducers,
        // to remove the EventConsumer from any of them.
        return onRemoveNonRegisteredConsumer(getClusterId(), consumerID);
    }

    /**
     * Acquires the registered EventConsumer with the provided consumerID.
     *
     * @param consumerID The unique identifier for the EventConsumer to retrieve
     * @return the EventConsumer with the given consumerID or {@code null} if none exists.
     */
    @Override
    public final T getConsumer(final String consumerID) {
        return consumerID == null ? null : consumers.get(consumerID);
    }

    /**
     * Adds the EventConsumer to its originating EventProducers, by connecting it to
     * the underlying EventGenerator.
     * For example, this is the method where you would call
     * <code>aButton.addActionListener(listener)</code> or equivalent.
     *
     * @param validEventConsumer A non-null EventConsumer which has been validated by this
     *                           EventProducer WRT its ID.
     * @return true if the registration process was successful, and false otherwise.
     */
    protected boolean registerConsumerToEventProducers(final T validEventConsumer) {
        return true;
    }

    /**
     * Removes the ListenerType from its proper EventProducers, by removing from the
     * EventGenerator appropriate for the provided ListenerType. For example, this
     * is the method where you would call <code>aButton.removeActionListener(listener)</code>
     * to remove your EventListener from its EventGenerator(s).
     *
     * @param validEventConsumer A non-null EventListener which has been validated by this
     *                           EventProducer WRT its ID.
     * @return true if the removal process was successful, and false otherwise.
     */
    protected boolean removeConsumerFromEventProducers(final T validEventConsumer) {
        return true;
    }

    /**
     * Override this method to provide implementation notifying other AbstractEventProducer working in
     * clustered mode with this one about the newly registered EventConsumer.
     * The default implementation does nothing.
     *
     * @param senderID The ID of this AbstractEventProducer.
     * @param consumer The newly registered EventConsumer.
     */
    protected void onConsumerRegistered(final String senderID, final T consumer) {
        // Do nothing.
    }

    /**
     * <p>Override this method to provide implementation notifying other AbstractEventProducers
     * working in clustered mode with this one about a remove request for the consumer with
     * the provided consumerID. The intended usage caters for the following situation:</p>
     * <pre><code>
     * // Create 2 clustered EventProducers
     * AbstractEventProducer&lt;SomeEventConsumer&gt; clusteredEventProducer1 = ...
     * AbstractEventProducer&lt;SomeEventConsumer&gt; clusteredEventProducer2 = ...
     *
     * // Register an EventConsumer
     * SomeEventConsumer consumer = ...
     * clusteredEventProducer1.addConsumer(anEventConsumer);
     *
     * // Remove the EventConsumer from another node in the cluster
     * clusteredEventProducer2.removeConsumer(anEventConsumer.getClusterId());
     * </code>
     * </pre>
     * <p>Here, clusteredEventProducer2 must notify clusteredEventProducer1 about the removeConsumer
     * call and its corresponding ID. ClusteredEventProducer1 removes anEventConsumer from its internal
     * storage. The default implementation of this method does nothing.</p>
     *
     * @param senderID        The ID of this AbstractEventProducer.
     * @param eventConsumerID The ID of the EventConsumer to remove.
     * @return {@code true} if the EventConsumer with the given ID was removed, and
     * {@code false} otherwise.
     */
    protected boolean onRemoveNonRegisteredConsumer(final String senderID, final String eventConsumerID) {
        // Do nothing
        return false;
    }

    /**
     * If an exception occurs during notification to a listener, an event will be sent to this method.
     *
     * @param eventConsumer the listener that the exception occurred in.
     * @param exception     the exception that occurred.
     */
    protected void onExceptionDuringConsumerNotification(final T eventConsumer, final Exception exception) {
        log.error("Unable to notify EventConsumer '" + eventConsumer.getClusterId() + "'", exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void notifyConsumers(final EventConsumerCallback<T> consumerCallback) {

        // Check sanity
        Validate.notNull(consumerCallback, "Cannot handle null callback argument.");

        // Perform notification
        for (final String current : getConsumerIDs()) {
            final T listener = getConsumer(current);

            try {
                consumerCallback.onEvent(listener);
            } catch (final Exception exception) {
                onExceptionDuringConsumerNotification(listener, exception);
            }
        }
    }
}
