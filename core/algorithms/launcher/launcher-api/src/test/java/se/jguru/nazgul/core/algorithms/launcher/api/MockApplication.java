/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.launcher.api;

import org.apache.commons.cli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockApplication extends AbstractApplicationLauncher {

    // State
    public List<String> callTrace = new ArrayList<String>();
    public String exceptionMessageOnApplicationRun = null;
    // public String exceptionMessageOnApplicationRun = null;

    /**
     * {@inheritDoc}
     */
    public MockApplication(final String briefApplicationDescription,
                           final String[] commandLineArguments,
                           final String appVersion) {
        super(briefApplicationDescription, commandLineArguments, appVersion);
    }

    /**
     * {@inheritDoc}
     */
    public MockApplication(final String briefApplicationDescription,
                           final String[] commandLineArguments,
                           final String appVersion,
                           final List<String> patternsForVerboseLogging) {
        super(briefApplicationDescription, commandLineArguments, appVersion, patternsForVerboseLogging);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateArguments(final CommandLine commandLine) throws IllegalArgumentException {
        callTrace.add("validateArguments");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runApplication() {
        callTrace.add("runApplication");

        if(exceptionMessageOnApplicationRun != null) {
            throw new IllegalArgumentException(exceptionMessageOnApplicationRun);
        }
    }
}
