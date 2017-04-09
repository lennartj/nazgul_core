package se.jguru.nazgul.test.messaging.artemis;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.ConnectionFactory;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArtemisBrokerTest {

    // Shared state
    private ArtemisBroker unitUnderTest;
    private EmbeddedActiveMQ jmsServer;

    @Before
    public void createBroker() {
        unitUnderTest = new ArtemisBroker();
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
