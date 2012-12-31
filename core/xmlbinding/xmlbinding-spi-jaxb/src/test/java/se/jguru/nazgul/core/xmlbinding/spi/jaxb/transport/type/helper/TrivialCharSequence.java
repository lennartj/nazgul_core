/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.helper;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TrivialCharSequence implements CharSequence {

    // Internal state
    private StringBuffer buffer;

    public TrivialCharSequence(final StringBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return buffer.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char charAt(final int index) {
        return buffer.charAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return buffer.subSequence(start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return buffer.toString();
    }
}
