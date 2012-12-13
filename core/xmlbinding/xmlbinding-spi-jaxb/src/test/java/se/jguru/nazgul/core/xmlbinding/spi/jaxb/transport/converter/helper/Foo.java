/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter.helper;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Foo implements Comparable<Foo> {

    // Internal state
    private String value;

    public Foo(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Foo o) {
        return value.compareTo(o.getValue());
    }
}
