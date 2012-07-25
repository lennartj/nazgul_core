/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types;

import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"name", "age"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Person {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlAttribute(required = true)
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if(obj != null && obj instanceof Person) {
            Person that = (Person) obj;

            return this.name.equals(that.name) && this.age == that.age;
        }

        return false;
    }
}