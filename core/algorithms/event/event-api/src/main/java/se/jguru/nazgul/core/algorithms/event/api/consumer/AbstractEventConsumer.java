/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.event.api.consumer;

import org.apache.commons.lang3.Validate;

/**
 * Abstract implementation of the EventConsumer interface, using Strings for identifiers.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractEventConsumer<E extends SimpleEventConsumer<E>> implements SimpleEventConsumer<E> {

    // Internal state
    private String id;

    /**
     * Creates a new AbstractEventConsumer instance, using the provided identifier.
     * @param id The non-null identifier of this AbstractEventConsumer instance.
     */
    public AbstractEventConsumer(final String id) {

        Validate.notEmpty(id, "Cannot handle null or empty id argument.");
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getID() {
        return id;
    }
}
