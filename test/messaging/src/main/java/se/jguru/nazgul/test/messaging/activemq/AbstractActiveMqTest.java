/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.activemq;

import se.jguru.nazgul.test.messaging.AbstractJmsTest;

/**
 * ActiveMQ-flavoured implementation of the AbstractJmsTest.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractActiveMqTest extends AbstractJmsTest {

    /**
     * Creates a default (i.e. unit-test-tailored) ActiveMQBroker using
     * the supplied transactedOperation setting.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public AbstractActiveMqTest(final boolean transactedOperation) {
        this(transactedOperation, new ActiveMQBroker());
    }

    /**
     * Creates an AbstractActiveMqTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null ActiveMQBroker instance.
     */
    public AbstractActiveMqTest(final boolean transactedOperation, final ActiveMQBroker broker) {
        super(transactedOperation, broker);
    }
}
