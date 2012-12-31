/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.launcher.api;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractApplicationLauncherTest {

    // Our logger
    private static final Logger log = LoggerFactory.getLogger(T_AbstractApplicationLauncherTest.class);
    private static final Logger appLog = LoggerFactory.getLogger(AbstractApplicationLauncher.class);

    // Shared state
    private String description = "fooBar";
    private String[] args = {"foo", "bar"};
    private String version = "1.0.1";

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullArgs() {

        // Act & Assert
        new MockApplication(description, null, version);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullDescription() {

        // Act & Assert
        new MockApplication(null, args, version);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullVersion() {

        // Act & Assert
        new MockApplication(description, args, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyPatternsList() {

        // Act & Assert
        new MockApplication(description, args, version, new ArrayList<String>());
    }

    @Test
    public void validateApplicationLaunch() {

        // Assemble
        final MockApplication unitUnderTest = new MockApplication(description, args, version);

        // Act
        unitUnderTest.execute();

        // Assert
        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(2, callTrace.size());

        Assert.assertEquals("validateArguments", callTrace.get(0));
        Assert.assertEquals("runApplication", callTrace.get(1));
    }

    @Test
    public void validateVerbosePrintHelp() {

        // Assemble
        final String[] helpArgs = {"-help", "-verbose"};
        final List<ILoggingEvent> loggingEventList = new ArrayList<ILoggingEvent>();
        final AppenderBase<ILoggingEvent> appenderBase = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                loggingEventList.add(eventObject);
            }
        };
        addAppenderToLogger(AbstractApplicationLauncher.class.getName(), appenderBase);
        final MockApplication unitUnderTest = new MockApplication(description, helpArgs, version);

        // Act
        unitUnderTest.execute();

        // Assert
        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(0, callTrace.size());
        Assert.assertEquals(2, loggingEventList.size());

        Assert.assertTrue(loggingEventList.get(1).getMessage().contains("Help printed."));
    }

    @Test
    public void validateHelpPrintedOnApplicationException() {

        // Assemble
        final String exceptionMessage = "The Foo was not a Bar";
        final String[] helpArgs = {"-verbose", "foo"};
        final List<ILoggingEvent> loggingEventList = new ArrayList<ILoggingEvent>();
        final AppenderBase<ILoggingEvent> appenderBase = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                loggingEventList.add(eventObject);
            }
        };
        addAppenderToLogger(AbstractApplicationLauncher.class.getName(), appenderBase);
        final MockApplication unitUnderTest = new MockApplication(description, helpArgs, version);
        unitUnderTest.exceptionMessageOnApplicationRun = exceptionMessage;

        // Act
        unitUnderTest.execute();

        // Assert
        Assert.assertTrue(loggingEventList.get(1).getMessage().contains(exceptionMessage));
    }


    //
    // Private helpers
    //

    private void addAppenderToLogger(final String name, final AppenderBase<ILoggingEvent> appender) {

        boolean added = false;

        // Lower the logging squelch for all relevant Appenders to DEBUG
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger current : context.getLoggerList()) {

            if(current.getName().equalsIgnoreCase(name)) {
                current.addAppender(appender);
                appender.start();
                added = true;
            }
        }

        if(!added) {
            throw new IllegalStateException("Appender not added to Logger [" + name + "].");
        }
    }
}
