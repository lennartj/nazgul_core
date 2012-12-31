/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.clustering.api.AbstractClusterable;
import se.jguru.nazgul.core.clustering.api.ConstantIdGenerator;
import se.jguru.nazgul.core.clustering.api.IdGenerator;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract implementation of the EventConsumer interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = XmlBinder.CORE_NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEventConsumer<E extends EventConsumer<E>>
        extends AbstractClusterable implements EventConsumer<E> {

    /**
     * {@inheritDoc}
     */
    protected AbstractEventConsumer(final String id) {
        this(new ConstantIdGenerator(id));
    }

    /**
     * {@inheritDoc}
     */
    protected AbstractEventConsumer(final IdGenerator idGenerator) {
        super(idGenerator);
    }
}
