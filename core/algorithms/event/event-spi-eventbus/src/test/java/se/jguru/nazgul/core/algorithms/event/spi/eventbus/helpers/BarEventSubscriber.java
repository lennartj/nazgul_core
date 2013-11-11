/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-event-spi-eventbus
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
package se.jguru.nazgul.core.algorithms.event.spi.eventbus.helpers;

import com.google.common.eventbus.Subscribe;
import se.jguru.nazgul.core.clustering.api.Clusterable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BarEventSubscriber implements Clusterable {

    // Internal state
    private String clusterID;
    public List<String> callTrace = new ArrayList<String>();

    public BarEventSubscriber(final String clusterID) {
        this.clusterID = clusterID;
    }

    @Subscribe
    public void onBarEvent(final BarEvent event) {
        final String name = event == null ? "<nothing>" : event.getName();
        callTrace.add("[" + name + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterId() {
        return clusterID;
    }
}
