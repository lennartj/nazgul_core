/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.apache.commons.lang.Validate;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Abstract supertype of JAXB-annotated transport types, implying that all subtypes
 * should implement ClassInformationHolder and Serializable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"value"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractJaxbAnnotatedTransportType<T>
        implements ClassInformationHolder, Serializable, Comparable {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030001L;

    // Internal state
    protected T value;

    @XmlTransient
    protected SortedSet<String> classInformation;

    /**
     * JAXB-friendly constructor. <strong>This is for framework use only.</strong>
     */
    public AbstractJaxbAnnotatedTransportType() {
    }

    /**
     * Compound constructor, assigning the given classInformation (i.e. class names on
     * string form) to the classInformation member - wrapped into an unmodifiable SortedSet
     * instance.
     *
     * @param classInformation The classInformation data, holding strings received by calls
     *                         to {@code Class.getName()}.
     */
    protected AbstractJaxbAnnotatedTransportType(final List<String> classInformation) {

        // Check sanity
        Validate.notEmpty(classInformation, "Cannot handle null or empty classInformation argument.");

        SortedSet<String> classinfo = new TreeSet<String>();
        for (String current : classInformation) {

            Validate.notEmpty(current, "Given classInformation argument cannot contain null or empty values.");
            classinfo.add(current);
        }

        // Assign internal state.
        this.classInformation = Collections.unmodifiableSortedSet(classinfo);
    }

    /**
     * {@inheritDoc}
     */
    public final SortedSet<String> getClassInformation() {
        return classInformation;
    }

    /**
     * @return The contained value.
     */
    public T getValue() {
        return value;
    }
}
