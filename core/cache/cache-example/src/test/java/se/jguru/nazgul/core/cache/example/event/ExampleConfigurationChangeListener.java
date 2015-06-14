/*
 * #%L
 * Nazgul Project: nazgul-core-cache-example
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
package se.jguru.nazgul.core.cache.example.event;

import se.jguru.nazgul.core.cache.api.distributed.async.LightweightTopicListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Example/dummy event listener implementation which stores all received event
 * objects within an internal List, to enable comparison between what was sent
 * and what was received.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExampleConfigurationChangeListener implements LightweightTopicListener<ExampleConfigurationChangeEvent> {

    // Internal state
    private String clusterId;
    private List<ExampleConfigurationChangeEvent<String>> receivedEvents;

    public ExampleConfigurationChangeListener(final String clusterId) {
        this.clusterId = clusterId;
        receivedEvents = new ArrayList<ExampleConfigurationChangeEvent<String>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final ExampleConfigurationChangeEvent message) {

        // Normally, we would implement some kind of callback functionality here.
        //
        //
        // For this example implementation, we simply save all inbound events in
        // a List to compare received events with sent events.
        receivedEvents.add((ExampleConfigurationChangeEvent<String>) message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterId() {
        return clusterId;
    }

    /**
     * @return A List holding all received events.
     */
    public List<ExampleConfigurationChangeEvent<String>> getReceivedEvents() {
        return receivedEvents;
    }
}
