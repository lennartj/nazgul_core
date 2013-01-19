/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.activemq;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockActiveMqTest extends AbstractActiveMqTest {

    public List<String> callTrace = new ArrayList<String>();

    /**
     * Creates a default (i.e. unit-test-tailored) ActiveMQBroker using
     * the supplied transactedOperation setting.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public MockActiveMqTest(boolean transactedOperation) {
        super(transactedOperation);
    }

    /**
     * Creates an AbstractActiveMqTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null ActiveMQBroker instance.
     */
    public MockActiveMqTest(boolean transactedOperation, ActiveMQBroker broker) {
        super(transactedOperation, broker);
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     *
     * @throws javax.jms.JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void setupServices() throws JMSException {
        callTrace.add("setupServices");
    }

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     *
     * @throws javax.jms.JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void tearDownServices() throws JMSException {
        callTrace.add("tearDownServices");
    }
}
