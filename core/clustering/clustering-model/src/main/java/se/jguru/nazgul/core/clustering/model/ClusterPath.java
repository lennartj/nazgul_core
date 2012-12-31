/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.clustering.model;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"version"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public class ClusterPath extends NazgulEntity {

    // Internal state
    @Version
    @XmlAttribute(required = false)
    private int version;
}
