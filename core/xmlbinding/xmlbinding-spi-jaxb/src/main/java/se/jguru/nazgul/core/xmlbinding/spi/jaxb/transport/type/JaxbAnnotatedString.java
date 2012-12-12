/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Transport type representing a {@code String} value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = "transportForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedString extends AbstractJaxbAnnotatedTransportType<String> {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030003L;

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String transportForm;

    /**
     * {@inheritDoc}
     */
    public JaxbAnnotatedString() {
    }

    /**
     * {@inheritDoc}
     */
    public JaxbAnnotatedString(final String value) {
        super(value);

        // Assign internal state
        this.transportForm = value;
    }

    /**
     * @return The contained value.
     */
    @Override
    public String getValue() {

        if (super.value == null) {
            super.value = transportForm;
        }

        return super.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object that) {

        if (that instanceof JaxbAnnotatedString) {
            return getValue().compareTo(((JaxbAnnotatedString) that).getValue());
        }

        if (that instanceof String) {
            return getValue().compareTo((String) that);
        }

        throw new ClassCastException("Cannot compare JaxbAnnotatedStrings to [" + that.getClass().getName() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj != null && (obj instanceof JaxbAnnotatedString || obj instanceof String) && this.compareTo(obj) == 0;
    }
}
