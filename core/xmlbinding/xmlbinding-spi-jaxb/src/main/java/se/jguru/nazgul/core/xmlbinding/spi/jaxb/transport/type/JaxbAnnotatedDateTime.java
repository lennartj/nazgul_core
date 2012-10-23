/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Transport type representing a {@code DateTime} value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"transportForm"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedDateTime extends AbstractJaxbAnnotatedTransportType<DateTime> {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030010L;

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String transportForm;

    @XmlTransient
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    /**
     * {@inheritDoc}
     */
    public JaxbAnnotatedDateTime() {
    }

    /**
     * {@inheritDoc}
     */
    public JaxbAnnotatedDateTime(final DateTime value) {

        // Delegate to superclass
        super();

        // Convert to a String for the transport form.
        super.value = null;
        this.transportForm = DATE_TIME_FORMATTER.print(value);
    }

    /**
     * @return The contained value.
     */
    @Override
    public DateTime getValue() {

        if(value == null) {
            value = DATE_TIME_FORMATTER.parseDateTime(transportForm);
        }

        // Ignore the value in the superclass.
        return super.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object that) {

        if (that instanceof JaxbAnnotatedDateTime) {
            return getValue().compareTo(((JaxbAnnotatedDateTime) that).getValue());
        }

        if (that instanceof DateTime) {
            return getValue().compareTo((DateTime) that);
        }

        throw new ClassCastException("Cannot compare JaxbAnnotatedStrings to [" + that.getClass().getName() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null
                && (obj instanceof JaxbAnnotatedDateTime || obj instanceof DateTime)
                && this.compareTo(obj) == 0;
    }
}