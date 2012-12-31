/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.launcher.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract StandardLifecycle implementation, handling command-line arguments, exception printouts and help screens.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractApplicationLauncher implements StandardLifecycle {

    // Our logger
    private static final Logger log = LoggerFactory.getLogger(AbstractApplicationLauncher.class);

    // Constants
    private static final String NAZGUL_PACKAGES_REGEXP = "^se\\.jguru\\.nazgul\\..*";

    private static final String ARG_VERBOSE = "verbose";
    private static final String ARG_HELP = "help";

    // Internal state
    private List<Pattern> verboseLoggerPatternList;
    private Options options;
    private CommandLine commandLine;
    private boolean dontExecuteApplication = false;
    private String briefApplicationDescription;
    private String briefCommandLineSyntax;
    private URL jarfileLocation;
    private String appVersion;

    /**
     * Compound constructor, creating an AbstractApplication instance from the given arguments.
     *
     * @param briefApplicationDescription A brief, human-readable description of the application.
     *                                    <strong>Example:</strong> <code>Performs load tests on
     *                                    the CGW-internal radius server.</code>
     * @param commandLineArguments        The command-line arguments supplied to a main method call.
     * @param appVersion                  A version descriptor for this application.
     */
    protected AbstractApplicationLauncher(final String briefApplicationDescription,
                                          final String[] commandLineArguments,
                                          final String appVersion) {
        this(briefApplicationDescription, commandLineArguments, appVersion, Arrays.asList(NAZGUL_PACKAGES_REGEXP));
    }

    /**
     * Compound constructor, creating an AbstractApplication instance from the given arguments.
     *
     * @param briefApplicationDescription A brief, human-readable description of the application.
     *                                    <strong>Example:</strong> <code>Performs load tests on
     *                                    the CGW-internal radius server.</code>
     * @param commandLineArguments        The command-line arguments supplied to a main method call.
     * @param appVersion                  A version descriptor for this application.
     * @param patternsForVerboseLogging   A List holding java regexp patterns, defining which
     */
    protected AbstractApplicationLauncher(final String briefApplicationDescription,
                                          final String[] commandLineArguments,
                                          final String appVersion,
                                          final List<String> patternsForVerboseLogging) {

        // Check sanity
        Validate.notEmpty(briefApplicationDescription,
                "Cannot handle null or empty briefApplicationDescription argument.");
        Validate.notNull(commandLineArguments, "Cannot handle null commandLineArguments argument.");
        Validate.notEmpty(appVersion, "Cannot handle null or empty appVersion argument.");
        Validate.notEmpty(patternsForVerboseLogging,
                "Cannot handle null or empty patternsForVerboseLogging argument.");

        // Create internal state
        this.appVersion = appVersion;
        this.briefApplicationDescription = briefApplicationDescription;
        jarfileLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
        this.briefCommandLineSyntax = "java -jar " + getJarFile().getName() + " [arguments]";

        // Define the standard options
        options = new Options();
        options.addOption(ARG_VERBOSE, "verbose", false, "Optional. If provided, use verbose logging.");
        options.addOption(ARG_HELP, "help", false, "Optional. Prints the application brief help text.");
        addOptions(options);

        // Create a pattern list for logger verbosity matching
        verboseLoggerPatternList = new ArrayList<Pattern>();
        for (String current : patternsForVerboseLogging) {
            verboseLoggerPatternList.add(Pattern.compile(current));
        }

        log.debug("Got jarfileLocation: " + getJarPath());

        // Create the command line
        final CommandLineParser parser = new PosixParser();

        try {
            // Parse the command line.
            commandLine = parser.parse(options, commandLineArguments);
            adjustLoggingLevel(commandLine);

            if (commandLine.hasOption(ARG_HELP)) {
                dontExecuteApplication = true;
                printUsage("Help printed.");
            }

        } catch (final ParseException e) {

            // Whoops
            dontExecuteApplication = true;
            printUsage(e.getMessage());
        }
    }

    /**
     * @return The File of the executable JAR file where this class executes,
     *         or {@code null} if this class executes outside of a JAR file.
     */
    protected final File getJarFile() {
        return new File(jarfileLocation.getPath());
    }

    /**
     * @return The path of the executing JarFile.
     */
    protected final String getJarPath() {
        try {
            return getJarFile().getCanonicalPath();
        } catch (IOException e) {
            return getJarFile().getAbsolutePath();
        }
    }

    /**
     * Overload this method to add any Command-line Options to be supported by this AbstractApplication.
     *
     * @param options The command-line options to be used by this AbstractApplication instance.
     */
    protected void addOptions(final Options options) {
        // By default, adds nothing.
    }

    /**
     * Main entrypoint to the application, which executes its lifecycle by
     * invoking the following methods (in order):
     * <p/>
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
    public final void execute() {

        if (!dontExecuteApplication) {

            try {
                // First, validate the given CLI arguments.
                validateArguments(commandLine);

                // Second, fire the actual application.
                runApplication();

            } catch (RuntimeException e) {

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(e.getMessage()).append("\n");

                final Throwable cause = e.getCause();
                if (cause != null) {
                    if (cause.getMessage() != null) {
                        messageBuilder.append(cause.getMessage()).append("\n");
                    } else {
                        messageBuilder.append(" ... type: ").append(cause.getClass().getSimpleName()).append("\n");
                    }
                }

                // Whoops.
                printUsage(messageBuilder.toString());
            }
        }
    }

    /**
     * Checks if the provided key is given as an argument on the command line or not.
     *
     * @param argument The argument which should be validated.
     * @return {@code true} if the provided argument was supplied on the command line launching this application.
     */
    protected final boolean isArgumentSupplied(final String argument) {
        try {
            return commandLine.hasOption(argument);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Acquires the value of a command-line argument, or {@code fallbackValue} should
     * the argument not have been supplied.
     *
     * @param argument      the argument whose value should be retrieved.
     * @param fallbackValue a value returned if the given argument had no value or was not supplied
     *                      on the command line launching this application.
     * @return the value of a command-line argument, or {@code fallbackValue} should
     *         the argument not have been supplied.
     */
    protected final String getValue(final String argument, final String fallbackValue) {
        return isArgumentSupplied(argument) ? commandLine.getOptionValue(argument) : fallbackValue;
    }

    /**
     * Retrieves the application description of this AbstractApplicationLauncher instance.
     *
     * @return the application description of this AbstractApplicationLauncher instance.
     */
    public final String getBriefApplicationDescription() {
        return briefApplicationDescription;
    }

    //
    // Private helpers
    //

    private void adjustLoggingLevel(final CommandLine commandLine) {

        // Verbose logging?
        if (commandLine.hasOption(ARG_VERBOSE)) {

            // Lower the logging squelch for all relevant Appenders to DEBUG
            final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger current : context.getLoggerList()) {

                final String name = current.getName();
                for (Pattern currentPattern : verboseLoggerPatternList) {
                    if (currentPattern.matcher(name).matches()) {
                        current.setLevel(Level.DEBUG);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void printUsage(final String errorMessage) {

        StringWriter helpText = new StringWriter();

        helpText.append("\n\n---------------------------------------\n");

        HelpFormatter formatter = new HelpFormatter();

        try {
            formatter.printHelp(new PrintWriter(helpText),
                    80,
                    briefCommandLineSyntax,
                    getBriefApplicationDescription() + "\n\nArguments:",
                    options,
                    2,
                    2,
                    "\nMessage: " + errorMessage);
        } catch (Exception e) {
            helpText.append("\nMessage: " + errorMessage + "\n");
        }

        helpText.append("\n\n-- Version: " + appVersion);
        helpText.append("\n\n---------------------------------------");

        // Write using the logger.
        log.info(helpText.toString());
    }
}
