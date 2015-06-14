/*
 * #%L
 * Nazgul Project: nazgul-core-blueprint-test
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
package se.jguru.nazgul.test.blueprint;

import org.osgi.framework.Bundle;

/**
 * Simple wrapper enum for OSGi BundleState, implying the OSGi constants
 * {@code UNINSTALLED},{@code INSTALLED}, {@code RESOLVED},
 * {@code STARTING}, {@code STOPPING}, {@code ACTIVE} from interface Bundle.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see Bundle
 * @see org.osgi.framework.Bundle#getState()
 */
public enum BundleState {

    /**
     * The bundle is uninstalled and may not be used.
     *
     * @see org.osgi.framework.Bundle#UNINSTALLED
     */
    UNINSTALLED(Bundle.UNINSTALLED),

    /**
     * The bundle is installed but not yet resolved.
     *
     * @see org.osgi.framework.Bundle#INSTALLED
     */
    INSTALLED(Bundle.INSTALLED),

    /**
     * The bundle is resolved and is able to be started.
     *
     * @see org.osgi.framework.Bundle#RESOLVED
     */
    RESOLVED(Bundle.RESOLVED),

    /**
     * The bundle is in the process of starting.
     *
     * @see org.osgi.framework.Bundle#STARTING
     */
    STARTING(Bundle.STARTING),

    /**
     * The bundle is now running.
     *
     * @see org.osgi.framework.Bundle#ACTIVE
     */
    ACTIVE(Bundle.ACTIVE),

    /**
     * The bundle is in the process of stopping.
     *
     * @see org.osgi.framework.Bundle#STOPPING
     */
    STOPPING(Bundle.STOPPING);

    // Internal state
    private int bundleState;

    private BundleState(final int bundleState) {
        this.bundleState = bundleState;
    }

    /**
     * @return The OSGi Bundle constant defining the BundleState.
     */
    public int getOsgiBundleStateValue() {
        return bundleState;
    }

    /**
     * Parses the supplied value retrieved from an OSGi {@code Bundle.getOsgiBundleStateValue()} method
     * call to a BundleState instance.
     *
     * @param bundleState The OSGi {@code Bundle.getOsgiBundleStateValue()} result.
     * @return The corresponding BundleState instance.
     */
    public static BundleState convert(final int bundleState) {

        for (BundleState current : values()) {
            if (current.bundleState == bundleState) {
                return current;
            }
        }

        throw new IllegalArgumentException("No BundleState found for value [" + bundleState + "]");
    }

    /**
     * {@inheritDoc}
     *
     * @return The lower case name, i.e. {@code name().toLowerCase()}
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
