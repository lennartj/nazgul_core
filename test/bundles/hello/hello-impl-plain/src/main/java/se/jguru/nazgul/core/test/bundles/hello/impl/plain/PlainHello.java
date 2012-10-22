/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.test.bundles.hello.impl.plain;

import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

/**
 * Plain service implementation of the Hello interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PlainHello implements Hello {

    // Internal state
    private String name;

    public PlainHello(final String name) {

        // Check sanity
        if(name == null || name.equals("")) {
            throw new IllegalArgumentException("Cannot handle null or empty name argument.");
        }

        // Assign internal state
        this.name = name;
    }

    /**
     * @return A traditional greeting.
     */
    @Override
    public String sayHello() {
        return "Hello, " + name;
    }
}
