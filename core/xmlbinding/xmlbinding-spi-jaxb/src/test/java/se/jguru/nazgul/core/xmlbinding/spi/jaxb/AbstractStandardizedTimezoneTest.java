/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractStandardizedTimezoneTest {

    // Internal state
    private DateTimeZone originalTimeZone;

    @Before
    public final void standardizeTimezone() {

        // Change/Standardize the timezone to simplify comparing test results.
        originalTimeZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);

        // Delegate
        setupSharedState();
    }

    /**
     * Override to setup shared state.
     */
    protected void setupSharedState() {
        // Do nothing.
    }

    @After
    public final void resetTimezone() {

        // Cleanup the synthetic timezone
        DateTimeZone.setDefault(originalTimeZone);

        // Delegate
        cleanupSharedState();
    }

    /**
     * Override to clean up shared state.
     */
    protected void cleanupSharedState() {
        // Do nothing
    }
}
