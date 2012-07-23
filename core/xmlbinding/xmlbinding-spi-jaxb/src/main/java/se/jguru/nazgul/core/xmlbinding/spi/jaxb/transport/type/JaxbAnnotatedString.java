/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Transport type representing a {@code String} value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"value"})
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedString extends AbstractJaxbAnnotatedTransportType {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030003L;

    // Internal state
    private String value;

    @XmlTransient
    private SortedSet<String> classInformation;

    /**
     * JAXB-friendly constructor.
     */
    public JaxbAnnotatedString() {

        SortedSet<String> classinfo = new TreeSet<String>();
        classinfo.add(String.class.getName());
        classInformation = Collections.unmodifiableSortedSet(classinfo);
    }

    /**
     * Compound constructor.
     *
     * @param value The String value.
     */
    public JaxbAnnotatedString(final String value) {
        this();

        // Assign internal state
        this.value = value;
    }

    /**
     * @return The String value.
     */
    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SortedSet<String> getClassInformation() {
        return classInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object that) {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
