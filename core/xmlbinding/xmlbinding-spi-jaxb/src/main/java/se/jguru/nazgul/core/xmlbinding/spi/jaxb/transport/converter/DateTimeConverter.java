/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import org.joda.time.DateTime;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * AbstractXmlAdapterTypeConverter instance converting Joda-Time
 * {@code DateTime} instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DateTimeConverter extends AbstractXmlAdapterTypeConverter<JaxbAnnotatedDateTime, DateTime> {

    // Internal state
    private XmlAdapter<JaxbAnnotatedDateTime, DateTime> adapter;

    /**
     * Default constructor.
     */
    public DateTimeConverter() {
        super(JaxbAnnotatedDateTime.class, DateTime.class);

        // Create internal state
        this.adapter = new DateTimeAdapter();
    }

    /**
     * @return An XmlAdapter instance converting between the TransportType and OriginalType.
     *         Cannot be {@code null}.
     */
    @Override
    protected XmlAdapter<JaxbAnnotatedDateTime, DateTime> getAdapter() {
        return adapter;
    }

    /**
     * XmlAdapter implementation for DateTime conversions.
     * <p/>
     * {@inheritDoc}
     */
    class DateTimeAdapter extends XmlAdapter<JaxbAnnotatedDateTime, DateTime> {

        /**
         * {@inheritDoc}
         */
        @Override
        public DateTime unmarshal(final JaxbAnnotatedDateTime value) throws Exception {
            return value.getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JaxbAnnotatedDateTime marshal(final DateTime value) throws Exception {
            return new JaxbAnnotatedDateTime(value);
        }
    }
}
