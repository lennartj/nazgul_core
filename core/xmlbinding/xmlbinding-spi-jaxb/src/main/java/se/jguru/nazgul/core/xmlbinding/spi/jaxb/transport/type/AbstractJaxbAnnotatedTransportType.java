/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.ClassInformationHolder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Abstract supertype of JAXB-annotated transport types, implying that all subtypes
 * should implement ClassInformationHolder and Serializable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractJaxbAnnotatedTransportType implements ClassInformationHolder, Serializable, Comparable {

    /**
     * Transport types require a serialVersionUID.
     */
    public static final long serialVersionUID = 7085076030001L;
}
