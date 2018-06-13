/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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



package se.jguru.nazgul.core.xmlbinding.api.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ZoneDateTimeAdapterTest {

    // Constants
    // Constants
    private static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");
    private static final ZoneId SWEDISH_ZONE = SWEDISH_TIMEZONE.toZoneId();

    private String transportForm = "2015-04-25T15:30:00+02:00[Europe/Stockholm]";
    private ZonedDateTime objectForm = ZonedDateTime.of(
            LocalDate.of(2015, Month.APRIL, 25),
            LocalTime.of(15, 30, 0),
            SWEDISH_ZONE);
    private ZonedDateTimeAdapter unitUnderTest = new ZonedDateTimeAdapter();

    @Test
    public void validateConvertingToTransportForm() throws Exception {

        // Assemble

        // Act
        final String result = unitUnderTest.marshal(objectForm);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNull(unitUnderTest.marshal(null));
        Assert.assertEquals(transportForm, result);
    }

    @Test
    public void validateConvertingFromTransportForm() throws Exception {

        // Assemble

        // Act
        final ZonedDateTime result = unitUnderTest.unmarshal(transportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(objectForm, result);
    }
}
