/*-
 * #%L
 * Nazgul Project: nazgul-core-launcher-api
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


package se.jguru.nazgul.core.algorithms.launcher.api;

import org.apache.commons.cli.CommandLine;

/**
 * ApplicationLauncher augmentation specification, defining a known
 * set of lifecycle methods for implementation by the concrete application.
 * The StandardLifecycle uses apache's commons CLI to define, parse and
 * print help for command-line arguments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface StandardLifecycle extends ApplicationLauncher {

    /**
     * Implement this method to perform any application-specific validation of the supplied CommandLine arguments.
     * Such validation should focus on argument semantics, since the actual CLI argument parsing is already
     * performed before invoking this method.
     *
     * @param commandLine The application command line.
     * @throws IllegalArgumentException if any argument was invalid. Supply a human-readable error message
     *                                  within the IllegalArgumentException, as that argument will be read
     *                                  by the application launcher.
     */
    void validateArguments(final CommandLine commandLine) throws IllegalArgumentException;

    /**
     * Main entrypoint to the application, which should execute the standard lifecycle
     * methods in the following order:
     * <pre>
     *   try {
     *      // First, validate the given CLI arguments.
     *      validateArguments(commandLine);
     *
     *      // Second, fire the actual application.
     *      runApplication(commandLine);
     *   } catch (RuntimeException e) {
     *
     *      // Whoops.
     *      printUsage(e.getMessage());
     *   }
     * </pre>
     */
    void execute();

    /**
     * Implement this method to execute the actual application.
     *
     * Throw a RuntimeException (or any subclass thereof) to indicate
     * that the application was terminated improperly. A thrown
     * RuntimeException is caught and handled by means of running
     * {@code printUsage} with the exception message passed as message.
     *
     * @see #execute()
     * @see #printUsage(String)
     */
    void runApplication();
}
