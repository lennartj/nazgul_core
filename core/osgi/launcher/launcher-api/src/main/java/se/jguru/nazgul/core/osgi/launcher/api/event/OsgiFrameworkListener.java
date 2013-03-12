/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.osgi.launcher.api.event;

import se.jguru.nazgul.core.algorithms.event.api.consumer.EventConsumer;

/**
 * Listener specification for receiving callbacks when the OSGi framework
 * emits events for BlueprintContainer services.
 *
 * @param <T> The exact subtype of OsgiFrameworkListener in effect
 * @param <S> The OSGi service object type to subscibe to. This might be
 *            a standard {@code ServiceReference} or a {@code BlueprintContainer}
 *            depending on the implementation.
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface OsgiFrameworkListener<T extends EventConsumer<T>, S> extends EventConsumer<T> {

    /**
     * Event callback method invoked after an OSGi service was added to the underlying OSGI Framework.
     *
     * @param serviceObject A reference to the service just added.
     */
    void afterServiceAdded(S serviceObject);

    /**
     * Event callback method invoked before an OSGi service was removed from the underlying OSGI framework.
     *
     * @param serviceObject A reference to the service to be removed.
     */
    void beforeServiceRemoved(S serviceObject);

    /**
     * Event callback method invoked when an OSGi service was modified within the underlying OSGI framework.
     *
     * @param serviceObject A reference to the modified service.
     */
    void onServiceModified(S serviceObject);
}
