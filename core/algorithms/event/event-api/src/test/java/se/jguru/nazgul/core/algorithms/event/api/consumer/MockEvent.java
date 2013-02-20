/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.event.api.consumer;

import java.util.EventObject;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEvent extends EventObject {

    /**
     * {@inheritDoc}
     */
    public MockEvent(Object source) {
        super(source);
    }
}
