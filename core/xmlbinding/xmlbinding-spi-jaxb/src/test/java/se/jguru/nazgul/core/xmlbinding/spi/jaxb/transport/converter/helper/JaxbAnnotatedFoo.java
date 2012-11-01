/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.AbstractJaxbAnnotatedTransportType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"transportForm"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedFoo extends AbstractJaxbAnnotatedTransportType<Foo> {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String transportForm;

    /**
     * JAXB-friendly constructor.
     * <strong>This is for framework use only.</strong>
     */
    public JaxbAnnotatedFoo() {
    }

    /**
     * Compound default constructor, wrapping the provided value within itself.
     *
     * @param value The original type value to wrap.
     */
    public JaxbAnnotatedFoo(Foo value) {

        // Delegate to superclass
        super();

        // Convert to a String for the transport form.
        super.value = null;
        transportForm = value.getValue();
    }

    /**
     * Compound constructor, assigning the given classInformation (i.e. class names on
     * string form) to the classInformation member - wrapped into an unmodifiable SortedSet
     * instance.
     *
     * @param classInformation The classInformation data, holding strings received by calls
     *                         to {@code Class.getName()}.
     */
    public JaxbAnnotatedFoo(final List<String> classInformation) {
        super(classInformation);
    }

    /**
     * @return The contained value.
     */
    @Override
    public Foo getValue() {

        if (super.value == null) {
            super.value = new Foo(transportForm);
        }

        return super.getValue();
    }

    public void setTransportForm(final String transportForm) {
        this.transportForm = transportForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object that) {

        if (that instanceof JaxbAnnotatedFoo) {
            return getValue().compareTo(((JaxbAnnotatedFoo) that).getValue());
        }

        if (that instanceof Foo) {
            return getValue().compareTo((Foo) that);
        }

        throw new ClassCastException("Cannot compare JaxbAnnotatedFoos to [" + that.getClass().getName() + "]");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null
                && (obj instanceof JaxbAnnotatedFoo || obj instanceof Foo)
                && this.compareTo(obj) == 0;
    }
}
