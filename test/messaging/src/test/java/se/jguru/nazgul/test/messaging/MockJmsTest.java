/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockJmsTest extends AbstractJmsTest {

    public List<String> callTrace = new ArrayList<String>();
    public boolean throwExceptionOnSetupServices = false;
    public boolean throwExceptionOnTeardownServices = false;
    public boolean throwExceptionOnAfterStartJmsBroker = false;
    public boolean throwExceptionOnBeforeStopJmsBroker = false;

    public MockJmsTest(boolean transactedOperation, MessageBroker broker) {
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

        if(throwExceptionOnSetupServices) {
            throw new JMSException("Exception thrown.");
        }
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

        if(throwExceptionOnTeardownServices) {
            throw new JMSException("Exception thrown.");
        }
    }

    /**
     * Override this method to perform any normal setup
     * after the Broker is launched. For example, any classes
     * which should be registered with the broker should be
     * instantiated here.
     */
    @Override
    protected void afterStartJmsBroker() {
        super.afterStartJmsBroker();
        callTrace.add("afterStartJmsBroker");

        if(throwExceptionOnAfterStartJmsBroker) {
            throw new IllegalStateException("Exception thrown.");
        }
    }

    /**
     * Override this method to perform any normal tear-down
     * before the Broker is stopped. You might cleanup any
     * instances which should be de-registered from the broker.
     */
    @Override
    protected void beforeStopJmsBroker() {
        super.beforeStopJmsBroker();
        callTrace.add("beforeStopJmsBroker");

        if(throwExceptionOnBeforeStopJmsBroker) {
            throw new IllegalStateException("Exception thrown.");
        }
    }
}
