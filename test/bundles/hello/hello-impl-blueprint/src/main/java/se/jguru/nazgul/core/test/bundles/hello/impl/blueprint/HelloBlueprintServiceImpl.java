/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.test.bundles.hello.impl.blueprint;

import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

/**
 * Constant blueprint greeting implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HelloBlueprintServiceImpl implements Hello {

    /**
     * @return A traditional greeting.
     */
    @Override
    public String sayHello() {
        return "Nice to see you!";
    }
}
