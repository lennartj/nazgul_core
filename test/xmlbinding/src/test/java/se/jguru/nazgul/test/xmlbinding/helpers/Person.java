/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.xmlbinding.helpers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://foo/bar", propOrder = {"name", "age", "beverage"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Person {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlAttribute(required = true)
    private int age;

    @XmlElement(required = true)
    private Beverage beverage;

    public Person() {
    }

    public Person(String name, int age, Beverage beverage) {
        this.name = name;
        this.age = age;
        this.beverage = beverage;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Beverage getBeverage() {
        return beverage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj != null && obj instanceof Person) {
            Person that = (Person) obj;

            return this.name.equals(that.name) && this.age == that.age;
        }

        return false;
    }
}
