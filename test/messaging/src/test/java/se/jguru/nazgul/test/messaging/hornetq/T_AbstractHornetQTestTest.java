/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.hornetq;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.Connection;
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
public class T_AbstractHornetQTestTest {

    // Shared state
    private MockHornetQTest unitUnderTest;
    private MessageBroker broker;

    @Before
    public void setupSharedState() {
        unitUnderTest = new MockHornetQTest(false);
        broker = unitUnderTest.getBroker();
    }

    @After
    public void teardownSharedState() throws Exception {
        unitUnderTest.stopJmsBroker();
    }

    @Test
    public void validateBrokerTypeAndLifecycle() throws Exception {

        // Assemble

        // Act
        unitUnderTest.startJmsBroker();
        unitUnderTest.stopJmsBroker();

        // Assert
        Assert.assertNotNull(broker);
        Assert.assertTrue(broker instanceof HornetQBroker);

        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals("setupServices", callTrace.get(0));
        Assert.assertEquals("tearDownServices", callTrace.get(1));
    }

    @Test
    public void validateProperCreationOfNonTransactedJmsObjects() throws Exception {

        // Assemble

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
