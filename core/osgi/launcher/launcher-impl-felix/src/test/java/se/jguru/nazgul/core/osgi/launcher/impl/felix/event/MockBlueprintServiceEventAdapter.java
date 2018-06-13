/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-impl-felix
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


package se.jguru.nazgul.core.osgi.launcher.impl.felix.event;

import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockBlueprintServiceEventAdapter implements BlueprintServiceListener {

    public List<BlueprintContainer> callTrace = new ArrayList<BlueprintContainer>();

    // Internal state
    private String id;

    public MockBlueprintServiceEventAdapter(final String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterServiceAdded(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeServiceRemoved(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServiceModified(final BlueprintContainer container) {
        callTrace.add(container);
    }

    /**
     * @return an Identifier, unique within the cluster.
     */
    @Override
    public String getClusterId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(BlueprintServiceListener that) {
        return that == null ? -1 : getClusterId().compareTo(that.getClusterId());
    }
}
