/*
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
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
package se.jguru.nazgul.core.persistence.model.converter;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JodaTimeConvertersTest {

    // Shared state
    private static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");
    private static final Chronology SWEDISH_ISO_CHRONOLOGY = ISOChronology
            .getInstance(DateTimeZone.forTimeZone(SWEDISH_TIMEZONE));

    // Shared state
    private DateTime aDateTime;

    private Chronology originalChronology;

    @Before
    public void setupSharedState() {

        // Setup Chronology
        originalChronology = DateTimeTimestampConverter.getJodaCronology();
        DateTimeTimestampConverter.setChronology(SWEDISH_ISO_CHRONOLOGY);

        // Create shared state
        aDateTime = new DateTime(2016, 3, 15, 2, 4, 3, DateTimeTimestampConverter.getJodaCronology());
    }

    @After
    public void teardownSharedState() {
        DateTimeTimestampConverter.setChronology(originalChronology);
    }

    @Test
    public void validateConvertingNulls() {

        // Assemble
        final DateTimeDateConverter dateConverter = new DateTimeDateConverter();
        final DateTimeTimestampConverter timestampConverter = new DateTimeTimestampConverter();

        // Act & Assert
        Assert.assertNull(dateConverter.convertToDatabaseColumn(null));
        Assert.assertNull(timestampConverter.convertToDatabaseColumn(null));

        Assert.assertNull(dateConverter.convertToEntityAttribute(null));
        Assert.assertNull(timestampConverter.convertToEntityAttribute(null));
    }

    @Test
    public void validateConverting() {

        // Assemble
        final DateTimeDateConverter dateConverter = new DateTimeDateConverter();
        final DateTimeTimestampConverter timestampConverter = new DateTimeTimestampConverter();

        // Act
        final Timestamp timestamp = timestampConverter.convertToDatabaseColumn(aDateTime);
        final Date date = dateConverter.convertToDatabaseColumn(aDateTime);

        // Assert
        Assert.assertNotNull(timestamp);
        Assert.assertNotNull(date);

        Assert.assertEquals(aDateTime, dateConverter.convertToEntityAttribute(date));
        Assert.assertEquals(aDateTime, timestampConverter.convertToEntityAttribute(timestamp));
    }
}
