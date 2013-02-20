/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
