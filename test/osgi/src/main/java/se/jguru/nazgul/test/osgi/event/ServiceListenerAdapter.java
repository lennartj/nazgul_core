/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
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


package se.jguru.nazgul.test.osgi.event;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ServiceListenerAdapter<T extends EventConsumer<T>> extends AbstractEventProducer<T>
        implements ServiceListener, EventConsumer<T> {

    // Internal state
    private ServiceListener delegate;

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID    A cluster-unique Identifier.
     * @param eventConsumerClass The type of EventConsumer handled by this AbstractEventProducer.
     * @param delegate           The ServiceListener to which all events will be delegated.
     */
    public ServiceListenerAdapter(final String clusterUniqueID,
                                  final Class<T> eventConsumerClass,
                                  final ServiceListener delegate) {
        super(clusterUniqueID, eventConsumerClass);
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final T that) {
        return Integer.valueOf(hashCode()).compareTo(that.hashCode());
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public void serviceChanged(final ServiceEvent event) {
        delegate.serviceChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ServiceListenerAdapter && obj.hashCode() == hashCode();
    }
}
