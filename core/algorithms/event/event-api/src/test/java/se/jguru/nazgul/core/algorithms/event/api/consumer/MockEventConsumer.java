/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.event.api.consumer;

import se.jguru.nazgul.core.clustering.api.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockEventConsumer extends AbstractEventConsumer<MockEventConsumer> {

    public List<String> callTrace = new ArrayList<String>();

    public MockEventConsumer(final String id) {
        super(id);
    }

    /**
     * {@inheritDoc}
     */
    public MockEventConsumer(IdGenerator idGenerator) {
        super(idGenerator);
    }

    public void consume(final MockEvent event) {
        callTrace.add("consume [" + event.getSource() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MockEventConsumer that) {
        return that == null ? -1 : getClusterId().compareTo(that.getClusterId());
    }
}
