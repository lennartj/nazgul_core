package se.jguru.nazgul.core.reflection.api.conversion.registry.helpers;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import java.util.Date;

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

        if(dateTime == null) {
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
}
