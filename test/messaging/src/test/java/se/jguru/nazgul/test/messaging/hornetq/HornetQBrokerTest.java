/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.hornetq;

import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.ConnectionFactory;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HornetQBrokerTest {

    // Shared state
    private HornetQBroker unitUnderTest;
    private EmbeddedJMS jmsServer;

    @Before
    public void createBroker() {
        unitUnderTest = new HornetQBroker();
        jmsServer = unitUnderTest.getJmsServer();
    }

    @After
    public void teardownBroker() throws Exception{
        unitUnderTest.stopBroker();
        Assert.assertNotNull(jmsServer);
    }


    @Test
    public void validateConfiguration() throws Exception {

        // Assemble

        // Act
        unitUnderTest.startBroker();

        final ConnectionFactory connectionFactory = unitUnderTest.getConnectionFactory("UnitTestConnectionFactory");

        // Assert
        Assert.assertNotNull(connectionFactory);
    }
}
