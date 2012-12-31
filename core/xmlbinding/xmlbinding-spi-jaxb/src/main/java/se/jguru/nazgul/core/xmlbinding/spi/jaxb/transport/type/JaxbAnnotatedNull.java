/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Transport type representing a {@code null} value.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedNull implements ClassInformationHolder, Serializable, Comparable {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030002L;

    @XmlTransient
    private static final JaxbAnnotatedNull INSTANCE = new JaxbAnnotatedNull();

    @XmlTransient
    private static final SortedSet<String> CLASS_INFO = Collections.unmodifiableSortedSet(new TreeSet<String>());

    /**
     * JAXB-friendly constructor.
     */
    public JaxbAnnotatedNull() {
        super();
    }

    /**
     * @return The singleton JaxbAnnotatedNull INSTANCE.
     */
    public static JaxbAnnotatedNull getInstance() {
        return INSTANCE;
    }

    /**
     * @return The fully qualified class names of all classes held within this ClassInformationHolder.
     */
    @Override
    public SortedSet<String> getClassInformation() {
        return CLASS_INFO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 882999001;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return compareTo(obj) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Object that) {
        return that == null || that instanceof JaxbAnnotatedNull ? 0 : -1;
    }
}
