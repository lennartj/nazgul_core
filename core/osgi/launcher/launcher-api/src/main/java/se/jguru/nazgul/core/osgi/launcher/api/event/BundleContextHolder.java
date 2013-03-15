/*
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.osgi.launcher.api.event;

import org.osgi.framework.BundleContext;
import se.jguru.nazgul.core.clustering.api.Clusterable;

/**
 * Event callback specification for managing BundleContext instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BundleContextHolder extends Clusterable {

    /**
     * Registers the provided BundleContext to this BundleContextHolder instance.
     *
     * @param context The BundleContext which should be registered to this BundleContextHolder.
     */
    void register(final BundleContext context);

    /**
     * Un-registers the provided BundleContext from this BundleContextHolder instance.
     *
     * @param context The BundleContext which should be un-registered from this BundleContextHolder.
     */
    void unregister(final BundleContext context);
}
