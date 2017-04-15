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
package se.jguru.nazgul.test.xmlbinding;

import org.joda.time.DateTimeZone;
import org.junit.Rule;
import se.jguru.nazgul.test.xmlbinding.junit.StandardTimeZoneRule;

/**
 * Extend this testcase for a convenient way to assign a standard/well-defined DateTimeZone which will be
 * assigned for the remainder of the tests. The default DateTimeZone is restored after each test is complete.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see se.jguru.nazgul.test.xmlbinding.junit.StandardTimeZoneRule
 */
public abstract class AbstractStandardizedTimezoneTest {

    /**
     * Active rule which assigns a standard DateTimeZone for the remainder of the tests,
     * executed within this AbstractStandardizedTimezoneTest.
     */
    @Rule
    public StandardTimeZoneRule standardTimeZoneRule;

    /**
     * Default constructor, which creates an AbstractStandardizedTimezoneTest that assigns the supplied
     * desiredTimeZone for the remainder of the tests.
     *
     * @param desiredTimeZone The DateTimeZone to be used when the test cases are executed.
     */
    protected AbstractStandardizedTimezoneTest(final DateTimeZone desiredTimeZone) {
        this.standardTimeZoneRule = new StandardTimeZoneRule(desiredTimeZone);
    }

    /**
     * Default constructor, which creates an AbstractStandardizedTimezoneTest that assigns the
     * {@code DateTimeZone.UTC} timezone for the remainder of the tests.
     */
    protected AbstractStandardizedTimezoneTest() {
        this(DateTimeZone.UTC);
    }
}
