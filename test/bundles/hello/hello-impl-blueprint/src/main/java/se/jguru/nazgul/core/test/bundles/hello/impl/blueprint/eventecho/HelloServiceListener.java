/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.test.bundles.hello.impl.blueprint.eventecho;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.test.bundles.hello.api.Hello;

import java.util.Map;

/**
 * Blueprint-style service listener.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HelloServiceListener {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(HelloServiceListener.class);

    /**
     * Callback method to invoke whenever a Hello service appears.
     *
     * @param helloService The Hello service instance which appeared.
     * @param properties   The ServiceRegistration metadata properties.
     */
    public void onServiceAppeared(final Hello helloService, final Map properties) {

        // Log the appearance of a Hello service instance.
        log.info("Hello service appeared. [" + helloService.sayHello() + "]", properties);
    }

    /**
     *Callback method to invoke whenever a Hello service disappears.
     *
     * @param helloService The Hello service instance which appeared.
     * @param properties   The ServiceRegistration metadata properties.
     */
    public void onServiceDisappeared(final Hello helloService, final Map properties) {

        // Log the disappearance of a Hello service instance.
        log.info("Hello service disappeared. [" + helloService.sayHello() + "]", properties);
    }
}
