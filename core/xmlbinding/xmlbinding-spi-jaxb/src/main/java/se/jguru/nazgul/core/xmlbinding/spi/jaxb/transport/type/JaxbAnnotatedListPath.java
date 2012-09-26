/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import se.jguru.nazgul.core.algorithms.api.trees.common.ListPath;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbAnnotatedListPath<T extends Serializable & Comparable<T>>
        extends AbstractJaxbAnnotatedTransportType<ListPath<T>> {

    public JaxbAnnotatedListPath() {

        SortedSet<String> classinfo = new TreeSet<String>();
        classinfo.add(ListPath.class.getName());
        classInformation = Collections.unmodifiableSortedSet(classinfo);
    }

    /**
     * Compound constructor.
     *
     * @param value The String value.
     */
    public JaxbAnnotatedListPath(final ListPath<T> value) {
        this();

        // Assign internal state
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object that) {

        /*
        if (that instanceof JaxbAnnotatedString) {
            return getValue().compareTo(((JaxbAnnotatedString) that).getValue());
        }

        if (that instanceof String) {
            return getValue().compareTo((String) that);
        }
        */

        throw new ClassCastException("Cannot compare JaxbAnnotatedStrings to [" + that.getClass().getName() + "]");
    }
}
