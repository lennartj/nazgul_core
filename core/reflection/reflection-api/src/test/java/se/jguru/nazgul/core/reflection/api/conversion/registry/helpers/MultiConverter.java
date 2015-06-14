/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class MultiConverter {

    // Internal state
    private String value;

    @Converter
    public MultiConverter(final String aString) {
        this.value = aString;
    }

    public String getValue() {
        return value;
    }

    @Converter(acceptsNullValues = true)
    public StringBuffer convert(final String aString) {
        final String bufferValue = aString == null ? "nothing!" : aString;
        return new StringBuffer(bufferValue);
    }

    @Converter(acceptsNullValues = true)
    public Date convertToDate(final DateTime dateTime) {

        if (dateTime == null) {
            return DateTime.parse("2012-03-04T05:06", ISODateTimeFormat.dateHourMinute()).toDate();
        }

        // Convert the supplied calendarDate
        return dateTime.toDate();
    }

    @Converter(conditionalConversionMethod = "isBasicDateTimeNoMillisFormat")
    public DateTime convertToDateTime(final String aString) {
        return DateTime.parse(aString, ISODateTimeFormat.basicDateTimeNoMillis());
    }

    public boolean isBasicDateTimeNoMillisFormat(final String aString) {

        try {
            DateTime.parse(aString, ISODateTimeFormat.basicDateTimeNoMillis());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public DateTime nonAnnotatedConverterMethod(final String aString) {
        return DateTime.parse(aString, ISODateTimeFormat.basicDateTimeNoMillis());
    }

    @Converter(priority = 200, conditionalConversionMethod = "isDateHourMinuteFormat")
    public DateTime lowerPriorityDateTimeConverter(final String aString) {
        return DateTime.parse(aString, ISODateTimeFormat.dateHourMinute());
    }

    public boolean isDateHourMinuteFormat(final String aString) {

        try {
            DateTime.parse(aString, ISODateTimeFormat.dateHourMinute());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean canConvertArrayToCollection(final Object object) {
        return object != null && object.getClass().isArray();
    }

    @Converter(conditionalConversionMethod = "canConvertArrayToCollection")
    public Collection<?> convertToCollection(final Object anArray) {

        // Copy all objects in the array to the collection
        final List<Object> toReturn = new ArrayList<Object>();
        for (int i = 0; i < Array.getLength(anArray); i++) {
            toReturn.add(Array.get(anArray, i));
        }
        return toReturn;
    }
}
