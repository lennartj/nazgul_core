/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-test
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

package se.jguru.nazgul.test.osgi.event;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;
import se.jguru.nazgul.core.algorithms.event.api.producer.AbstractEventProducer;

/**
 * Simple delegating BundleListener, delegating all received events to a
 * wrapped EventConsumer.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BundleListenerAdapter<T extends EventConsumer<T>>
        extends AbstractEventProducer<T> implements BundleListener, EventConsumer<T> {

    // Internal state
    private BundleListener delegate;

    /**
     * Creates a new AbstractIdentifiable and assigns the provided
     * cluster-unique ID to this AbstractClusterable instance.
     *
     * @param clusterUniqueID   A cluster-unique Identifier.
     * @param delegate          The BundleListener to which all event handling should be delegated.
     * @param eventConsumerType The type of EventConsumer handled by this AbstractEventProducer.
     */
    public BundleListenerAdapter(final String clusterUniqueID,
                                 final Class<T> eventConsumerType,
                                 final BundleListener delegate) {
        super(clusterUniqueID, eventConsumerType);

        Validate.notNull(delegate, "Cannot handle null delegate argument.");
        this.delegate = delegate;
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The {@code BundleEvent}.
     */
    @Override
    public void bundleChanged(final BundleEvent event) {
        delegate.bundleChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final T o) {
        return Integer.valueOf(hashCode()).compareTo(o.hashCode());
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
        return obj instanceof BundleListenerAdapter && obj.hashCode() == hashCode();
    }
}
