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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalDateTimeAdapterTest {

    private String transportForm = "2015-04-25T15:40:00";
    private LocalDateTime objectForm = LocalDateTime.of(
            LocalDate.of(2015, Month.APRIL, 25),
            LocalTime.of(15, 40, 0));
    private LocalDateTimeAdapter unitUnderTest = new LocalDateTimeAdapter();

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
        final LocalDateTime result = unitUnderTest.unmarshal(transportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(objectForm, result);
    }
}
