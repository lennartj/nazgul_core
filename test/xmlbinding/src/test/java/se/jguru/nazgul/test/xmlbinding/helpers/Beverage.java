/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.xmlbinding.helpers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Beverage.NAMESPACE, propOrder = {"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Beverage {

    public static final String NAMESPACE = "http://some/good/beverage";

    private String name;

    public Beverage() {
    }

    public Beverage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
