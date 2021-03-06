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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockApplication extends AbstractApplicationLauncher {

    // State
    public List<String> callTrace = new ArrayList<String>();
    public String exceptionMessageOnApplicationRun = null;

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

        if (exceptionMessageOnApplicationRun != null) {
            throw new IllegalArgumentException(exceptionMessageOnApplicationRun);
        }
    }
}
