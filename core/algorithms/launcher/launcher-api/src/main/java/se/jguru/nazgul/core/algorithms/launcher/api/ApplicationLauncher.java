/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.launcher.api;

/**
 * Specification for a standalone JSE application, launched
 * by a main method within the concrete implementation class.
 * Lifecycle methods - other than the entrypoint {@code execute} -
 * are defined within the StandardLifecycle interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see StandardLifecycle
 */
public interface ApplicationLauncher {

    /**
     * Main entrypoint to the application, which executes its lifecycle.
     */
    void execute();

    /**
     * Prints a human-readable usage message for the application, given
     * a status message which can be acquired from an exception or passed
     * by a user given launch requirements (i.e. the user wanting the usage
     * help printed).
     *
     * @param statusMessage The message from an Exception, or status information
     *                      such as "Help Printed.".
     */
    void printUsage(final String statusMessage);
}
