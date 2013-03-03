/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.test.blueprint;

import org.osgi.framework.Bundle;

/**
 * Simple wrapper enum for OSGi BundleState.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum BundleState {

    /**
     * The bundle is uninstalled and may not be used.
     *
     * @see Bundle#UNINSTALLED
     */
    UNINSTALLED(Bundle.UNINSTALLED),

    /**
     * The bundle is installed but not yet resolved.
     *
     * @see Bundle#INSTALLED
     */
    INSTALLED(Bundle.INSTALLED),

    /**
     * The bundle is resolved and is able to be started.
     *
     * @see Bundle#RESOLVED
     */
    RESOLVED(Bundle.RESOLVED),

    /**
     * The bundle is now running.
     *
     * @see Bundle#ACTIVE
     */
    ACTIVE(Bundle.ACTIVE);

    // Internal state
    private int bundleState;

    private BundleState(final int bundleState) {
        this.bundleState = bundleState;
    }

    /**
     * @return The OSGi Bundle constant defining the BundleState.
     */
    public int getBundleState() {
        return bundleState;
    }



    /**
     * Parses the supplied value retrieved from an OSGi {@code Bundle.getBundleState()} method
     * call to a BundleState instance.
     *
     * @param bundleState The OSGi {@code Bundle.getBundleState()} result.
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
