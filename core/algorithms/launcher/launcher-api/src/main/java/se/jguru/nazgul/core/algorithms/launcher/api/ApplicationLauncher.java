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
