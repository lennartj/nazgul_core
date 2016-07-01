/*-
 * #%L
 * Nazgul Project: nazgul-core-persistence-model
 * %%
 * Copyright (C) 2010 - 2016 jGuru Europe AB
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
package se.jguru.nazgul.core.persistence.model.converter;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Java8TimeConvertersTest {

    // Shared state
    private static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");
    private static final ZoneId SWEDISH_ZONE = SWEDISH_TIMEZONE.toZoneId();

    // Shared state
    private LocalDate aLocalDate = LocalDate.of(2016, Month.FEBRUARY, 5);
    private LocalTime aLocalTime = LocalTime.of(23, 26);
    private LocalDateTime aLocalDateTime = LocalDateTime.of(
            LocalDate.of(2016, Month.MARCH, 4),
            LocalTime.of(18, 15));
    private ZonedDateTime aZonedDateTime = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.of(2015, Month.FEBRUARY, 2), LocalTime.of(19, 43)), SWEDISH_ZONE);

    @Test
    public void validateConvertingNulls() {

        // Assemble
        final LocalTimeAttributeConverter timeConverter = new LocalTimeAttributeConverter();
        final LocalDateTimeAttributeConverter dateTimeConverter = new LocalDateTimeAttributeConverter();
        final LocalDateAttributeConverter dateConverter = new LocalDateAttributeConverter();

        // Act & Assert
        Assert.assertNull(timeConverter.convertToDatabaseColumn(null));
        Assert.assertNull(dateConverter.convertToDatabaseColumn(null));
        Assert.assertNull(dateTimeConverter.convertToDatabaseColumn(null));

        Assert.assertNull(timeConverter.convertToEntityAttribute(null));
        Assert.assertNull(dateConverter.convertToEntityAttribute(null));
        Assert.assertNull(dateTimeConverter.convertToEntityAttribute(null));
    }

    @Test
    public void validateConverting() {

        // Assemble
        final LocalTimeAttributeConverter timeConverter = new LocalTimeAttributeConverter();
        final LocalDateTimeAttributeConverter dateTimeConverter = new LocalDateTimeAttributeConverter();
        final LocalDateAttributeConverter dateConverter = new LocalDateAttributeConverter();

        // Act
        final Time time = timeConverter.convertToDatabaseColumn(aLocalTime);
        final Timestamp timestamp = dateTimeConverter.convertToDatabaseColumn(aLocalDateTime);
        final Date date = dateConverter.convertToDatabaseColumn(aLocalDate);

        // Assert
        Assert.assertNotNull(time);
        Assert.assertNotNull(timestamp);
        Assert.assertNotNull(date);

        Assert.assertEquals(aLocalDate, dateConverter.convertToEntityAttribute(date));
        Assert.assertEquals(aLocalDateTime, dateTimeConverter.convertToEntityAttribute(timestamp));
        Assert.assertEquals(aLocalTime, timeConverter.convertToEntityAttribute(time));
    }
}
