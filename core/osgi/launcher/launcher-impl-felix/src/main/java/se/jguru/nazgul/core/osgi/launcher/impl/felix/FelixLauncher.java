/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-impl-felix
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

package se.jguru.nazgul.core.osgi.launcher.impl.felix;

import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.osgi.launcher.api.AbstractFrameworkLauncher;
import se.jguru.nazgul.core.osgi.launcher.api.event.BundleContextHolder;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceEventAdapter;
import se.jguru.nazgul.core.osgi.launcher.api.event.blueprint.BlueprintServiceListener;

import java.util.Map;

/**
 * FrameworkLauncher implementation for the Apache Felix OSGi container.
 * Uses BlueprintServiceListener instances, implying that the DependencyInjection
 * and event mechanism is the OSGi [Aries] Blueprint framework.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FelixLauncher extends AbstractFrameworkLauncher<BlueprintServiceListener> {

    /**
     * {@inheritDoc}
     */
    public FelixLauncher(final IdGenerator idGenerator) {
        super(idGenerator, BlueprintServiceListener.class);
    }

    /**
     * {@inheritDoc}
     */
    public FelixLauncher(final String clusterUniqueID) {
        super(clusterUniqueID, BlueprintServiceListener.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Framework createFramework(final Map<String, String> configuration) throws IllegalArgumentException {
        final FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
        return factory.newFramework(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInitialize(final Map<String, String> configuration, final Framework framework) {

        // Fire the autoProcessor, to process bundles within the bundle directory.
        AutoProcessor.process(configuration, framework.getBundleContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BundleContextHolder makeBundleContextHolder(final BlueprintServiceListener validEventConsumer) {
        return new BlueprintServiceEventAdapter(validEventConsumer);
    }
}
