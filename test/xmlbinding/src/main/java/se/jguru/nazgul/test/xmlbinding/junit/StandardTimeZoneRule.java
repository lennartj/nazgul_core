/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-test
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.test.xmlbinding.junit;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTimeZone;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * jUnit rule implementation to manage DateTimeZone during a Test Case.
 * This has significance for all tests dealing with dates and time zones.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardTimeZoneRule extends TestWatcher {

    // Internal state
    private DateTimeZone originalTimeZone;
    private DateTimeZone desiredTimeZone;

    /**
     * Constructor creating a new StandardTimeZoneRule which will set the provided desiredTimeZone
     * for use during the jUnit test.
     *
     * @param desiredTimeZone The non-null DateTimeZone which should be used during the test.
     */
    public StandardTimeZoneRule(final DateTimeZone desiredTimeZone) {

        // Check sanity
        Validate.notNull(desiredTimeZone, "Cannot handle null desiredTimeZone argument.");

        // Stash the default DateTimeZone, and reassign the default one.
        originalTimeZone = DateTimeZone.getDefault();
        this.desiredTimeZone = desiredTimeZone;
    }

    /**
     * Convenience constructor, using the {@code DateTimeZone}.
     */
    public StandardTimeZoneRule() {
        this(DateTimeZone.UTC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void starting(final Description description) {
        DateTimeZone.setDefault(desiredTimeZone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finished(final Description description) {
        DateTimeZone.setDefault(originalTimeZone);
    }
}
