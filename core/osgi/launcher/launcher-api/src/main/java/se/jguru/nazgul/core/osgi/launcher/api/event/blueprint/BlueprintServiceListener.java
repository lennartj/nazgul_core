/*-
 * #%L
 * Nazgul Project: nazgul-core-osgi-launcher-api
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


package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.core.osgi.launcher.api.event.OsgiFrameworkListener;

/**
 * OsgiFrameworkListener specification for listening to BlueprintContainer events.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BlueprintServiceListener extends OsgiFrameworkListener<BlueprintServiceListener, BlueprintContainer> {

    /**
     * Event callback method invoked after a BlueprintContainer service
     * was added to the underlying OSGI Framework.
     *
     * @param container The BlueprintContainer instance just added.
     */
    @Override
    void afterServiceAdded(BlueprintContainer container);

    /**
     * Event callback method invoked before a BlueprintContainer service
     * is removed from the underlying OSGI Framework.
     *
     * @param container A reference to the BlueprintContainer instance to be removed.
     */
    @Override
    void beforeServiceRemoved(BlueprintContainer container);

    /**
     * Event callback method invoked when a BlueprintContainer service
     * is updated within the underlying OSGI Framework.
     *
     * @param container A reference to the modified service.
     */
    @Override
    void onServiceModified(BlueprintContainer container);
}
