/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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


package se.jguru.nazgul.core.xmlbinding.api.adapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
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
public class AdaptersTest {

    // Constants
    private static final TimeZone SWEDISH_TIMEZONE = TimeZone.getTimeZone("Europe/Stockholm");
    private static final ZoneId SWEDISH_ZONE = SWEDISH_TIMEZONE.toZoneId();

    // Shared state
    private LocalDate lastAdmissionDate = LocalDate.of(2016, Month.FEBRUARY, 5);
    private LocalTime eventEndTime = LocalTime.of(23, 26);
    private LocalDateTime eventStartTime = LocalDateTime.of(
            LocalDate.of(2016, Month.MARCH, 4),
            LocalTime.of(18, 15));
    private ZonedDateTime admissionTime = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.of(2015, Month.FEBRUARY, 2), LocalTime.of(19, 43)), SWEDISH_ZONE);

    private DateExampleVO unitUnderTest = new DateExampleVO(
            lastAdmissionDate,
            eventStartTime,
            eventEndTime,
            admissionTime,
            SWEDISH_TIMEZONE);

    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    @Before
    public void setupSharedState() {

        try {
            final JAXBContext context = JAXBContext.newInstance(DateExampleVO.class);
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();

            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not create JAXB objects", e);
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/date_example_vo.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingWithNullData() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/dateExampleWithNulls.xml");
        unitUnderTest = new DateExampleVO(null, null, null, null, null);

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/date_example_vo.xml");

        // Act
        final DateExampleVO resurrected = unmarshalFromXML(DateExampleVO.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(lastAdmissionDate, resurrected.getLastAdmissionDate());
        Assert.assertEquals(admissionTime, resurrected.getAdmissionTime());
        Assert.assertEquals(eventStartTime, resurrected.getEventStartTime());
        Assert.assertEquals(eventEndTime, resurrected.getEventEndTime());
        Assert.assertEquals(SWEDISH_TIMEZONE, resurrected.getEventTimeZone());
    }

    @Test
    public void validateUnmarshallingWithNullData() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/dateExampleWithNulls.xml");

        // Act
        final DateExampleVO resurrected = unmarshalFromXML(DateExampleVO.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertNull(resurrected.getLastAdmissionDate());
        Assert.assertNull(resurrected.getAdmissionTime());
        Assert.assertNull(resurrected.getEventStartTime());
        Assert.assertNull(resurrected.getEventEndTime());
        Assert.assertNull(resurrected.getEventTimeZone());
    }

    @Test
    public void validateAdaptersHandlingExplicitNulls() {

        // Assemble

    }

    //
    // Private helpers
    //

    private String marshalToXML(final Object object) {

        final StringWriter out = new StringWriter();
        try {
            marshaller.marshal(object, out);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not marshal " + object, e);
        }

        return out.toString();
    }

    private <T> T unmarshalFromXML(final Class<T> expectedResult, final String data) {

        try {
            return (T) unmarshaller.unmarshal(new StringReader(data));
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not unmarshal data", e);
        }
    }
}
