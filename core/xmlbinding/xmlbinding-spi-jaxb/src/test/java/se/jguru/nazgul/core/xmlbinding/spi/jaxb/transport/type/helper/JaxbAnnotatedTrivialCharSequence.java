/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.AbstractJaxbAnnotatedTransportType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"transportForm"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedTrivialCharSequence extends AbstractJaxbAnnotatedTransportType<TrivialCharSequence> {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String transportForm;

    public JaxbAnnotatedTrivialCharSequence() {
    }

    public JaxbAnnotatedTrivialCharSequence(final TrivialCharSequence value) {
        super(value);

        // Assign internal state
        this.transportForm = value.toString();
    }

    @Override
    public TrivialCharSequence getValue() {

        if (value == null) {
            value = new TrivialCharSequence(new StringBuffer(transportForm));
        }

        // Ignore the value in the superclass.
        return super.getValue();
    }

    @Override
    public int compareTo(final Object o) {
        return 0;
    }
}
