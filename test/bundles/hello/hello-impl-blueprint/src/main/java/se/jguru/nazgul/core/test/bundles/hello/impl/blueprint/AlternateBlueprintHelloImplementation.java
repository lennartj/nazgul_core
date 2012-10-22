/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.test.bundles.hello.impl.blueprint;

import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

/**
 * Alternative Blueprint greeting implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AlternateBlueprintHelloImplementation implements Hello {

    // Internal state
    private String name;

    public AlternateBlueprintHelloImplementation(final String name) {
        this.name = name;
    }

    /**
     * @return A traditional greeting.
     */
    @Override
    public String sayHello() {
        return "Yo, " + name;
    }
}
