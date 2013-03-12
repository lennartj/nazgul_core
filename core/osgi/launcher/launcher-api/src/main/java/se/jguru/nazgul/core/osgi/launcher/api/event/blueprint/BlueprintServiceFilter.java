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

package se.jguru.nazgul.core.osgi.launcher.api.event.blueprint;

import org.apache.commons.lang3.Validate;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;

/**
 * Filter implementation accepting OSGI BlueprintService instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BlueprintServiceFilter implements Filter<ServiceReference> {

    /**
     * The symbolic name osgi property of a BlueprintContainer service.
     */
    public static final String BLUEPRINT_NAME_KEY = "osgi.blueprint.container.symbolicname";

    /**
     * Accepts BlueprintContainer serviceReference instances.
     *
     * @param candidate The ServiceReference candidate to be tested.
     * @return {@code true} if the provided candidate was a BlueprintContainer service.
     */
    @Override
    public boolean accept(final ServiceReference candidate) {
        return getBlueprintName(candidate) != null;
    }

    /**
     * Retrieves the BlueprintContainer service name of the provided ServiceReference.
     *
     * @param serviceReference A non-null OSGi ServiceReference.
     * @return The BlueprintContainer name, should the provided serviceReference be a BlueprintContainer,
     *         or {@code null} should the given ServiceReference not be a BlueprintContainer.
     */
    public static String getBlueprintName(final ServiceReference serviceReference) {

        // Check sanity
        Validate.notNull(serviceReference, "Cannot handle null serviceReference argument.");

        // All done.
        return (String) serviceReference.getProperty(BLUEPRINT_NAME_KEY);
    }

    /**
     * @return An LDAP filter highlighting BlueprintContainer service instances.
     */
    public static String getBlueprintContainerServiceLDAPFilter() {
        return "(" + Constants.OBJECTCLASS + "=" + BlueprintContainer.class.getName() + ")";
    }
}
