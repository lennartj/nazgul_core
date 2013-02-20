/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.hornetq;

import se.jguru.nazgul.test.messaging.AbstractJmsTest;

/**
 * HornetQ-flavoured implementation of the AbstractJmsTest.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHornetQTest extends AbstractJmsTest {

    /**
     * Creates a new AbstractHornetQTest with the supplied transactedOperation status,
     * and using a default HornetQBroker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public AbstractHornetQTest(final boolean transactedOperation) {
        this(transactedOperation, new HornetQBroker());
    }

    /**
     * Creates an AbstractHornetQTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null HornetQBroker instance.
     */
    public AbstractHornetQTest(final boolean transactedOperation, final HornetQBroker broker) {
        super(transactedOperation, broker);
    }
}
