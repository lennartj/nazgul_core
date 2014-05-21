/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-test
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
package se.jguru.nazgul.test.xmlbinding;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.helpers.MockTimezoneTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractStandardizedTimezoneTestTest {

    // Shared state
    private DateTimeZone originalTZ;
    private DateTimeZone eightHoursOffset;

    @Before
    public void setupSharedState() {
        originalTZ = DateTimeZone.getDefault();
        eightHoursOffset = DateTimeZone.forOffsetHours(8);

        // Adjust the timezone
        DateTimeZone.setDefault(eightHoursOffset);
    }

    @After
    public void teardownSharedState() {
        // Reset the timezone
        DateTimeZone.setDefault(originalTZ);
    }

    @Test
    public void validateTimezoneJuggling() {

        // Assemble
        final MockTimezoneTest unitUnderTest = new MockTimezoneTest();

        // Act
        unitUnderTest.standardizeTimezone();
        unitUnderTest.resetTimezone();

        // Assert
        Assert.assertEquals(DateTimeZone.UTC, unitUnderTest.activeTimeZone);
    }
}
