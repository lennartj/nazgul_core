/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging;

import junit.framework.Assert;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.activemq.ActiveMQBroker;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractJmsTestTest {

    // Shared state
    private ActiveMQBroker activeMQBroker;
    private BrokerService brokerService;

    @Before
    public void setupActiveMqBroker() {
        activeMQBroker = new ActiveMQBroker();
        brokerService = activeMQBroker.getBroker();
    }

    @After
    public void teardownMessageBroker() throws Exception {
        if(activeMQBroker != null) {
            activeMQBroker.stopBroker();
        }
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullMessageBroker() {

        // Act & Assert
        new MockJmsTest(false, null);
    }

    @Test
    public void validateLifecycle() throws Exception {

        // Assemble
        final boolean transactedOperation = false;
        final String[] expectedLifecycleCalls = {
                "afterStartJmsBroker",
                "setupServices",
                "tearDownServices",
                "beforeStopJmsBroker"};

        final MockJmsTest unitUnderTest = new MockJmsTest(transactedOperation, activeMQBroker);

        // Act
        unitUnderTest.startJmsBroker();
        Assert.assertTrue(brokerService.isStarted());
        unitUnderTest.stopJmsBroker();
        Assert.assertFalse(brokerService.isStarted());

        // Assert
        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(expectedLifecycleCalls.length, callTrace.size());

        for(int i = 0; i < expectedLifecycleCalls.length; i++) {
            Assert.assertEquals(expectedLifecycleCalls[i], callTrace.get(i));
        }
        Assert.assertEquals(transactedOperation, unitUnderTest.isTransactedOperation());
    }

    @Test(expected = IllegalStateException.class)
    public void validateLifecycleWithExceptionDuringStartJmsBroker() throws Exception {

        // Assemble
        final MockJmsTest unitUnderTest = new MockJmsTest(false, activeMQBroker);
        unitUnderTest.throwExceptionOnAfterStartJmsBroker = true;

        // Act & Assert
        unitUnderTest.startJmsBroker();
    }

    @Test(expected = IllegalStateException.class)
    public void validateLifecycleWithExceptionDuringStopJmsBroker() throws Exception {

        // Assemble
        final MockJmsTest unitUnderTest = new MockJmsTest(false, activeMQBroker);
        unitUnderTest.throwExceptionOnBeforeStopJmsBroker = true;

        // Act & Assert
        unitUnderTest.startJmsBroker();
        unitUnderTest.stopJmsBroker();
    }

    @Test(expected = JMSException.class)
    public void validateLifecycleWithExceptionDuringSetupServices() throws Exception {

        // Assemble
        final MockJmsTest unitUnderTest = new MockJmsTest(false, activeMQBroker);
        unitUnderTest.throwExceptionOnSetupServices = true;

        // Act & Assert
        unitUnderTest.startJmsBroker();
    }

    @Test(expected = JMSException.class)
    public void validateLifecycleWithExceptionDuringTeardownServices() throws Exception {

        // Assemble
        final MockJmsTest unitUnderTest = new MockJmsTest(false, activeMQBroker);
        unitUnderTest.throwExceptionOnTeardownServices = true;

        // Act & Assert
        unitUnderTest.startJmsBroker();
        unitUnderTest.stopJmsBroker();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnAcquiringJmsObjectsBeforeInitializingBroker() throws Exception {

        // Assemble
        final MockJmsTest unitUnderTest = new MockJmsTest(false, activeMQBroker);

        // Act & Assert
        unitUnderTest.createConnection();
    }

    @Test
    public void validateProperCreationOfNonTransactedJmsObjects() throws Exception {

        // Assemble
        final boolean transactedOperation = false;
        final MockJmsTest unitUnderTest = new MockJmsTest(transactedOperation, activeMQBroker);

        // Assert
        unitUnderTest.startJmsBroker();
        final Connection connection = unitUnderTest.createConnection();
        final Session session = unitUnderTest.createSession(connection);
        final Queue clientSideRequestQueue = session.createTemporaryQueue();
        final MessageProducer requestProducer = session.createProducer(clientSideRequestQueue);
        final TextMessage message = session.createTextMessage("fooBar!");
        requestProducer.send(message);
        unitUnderTest.stopJmsBroker();

        // Act
        Assert.assertNotNull(connection);
        Assert.assertNotNull(session);
        Assert.assertNotNull(requestProducer);
        Assert.assertNotNull(message);

        for(String current : Collections.list(((Enumeration<String>) message.getPropertyNames()))) {
            System.out.println("[" + current + "]");
        }
    }
}
