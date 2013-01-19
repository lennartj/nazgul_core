/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.lang.Validate;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.ConnectionFactory;

/**
 * MessageBroker implementation for ActiveMQ.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActiveMQBroker implements MessageBroker {

    /**
     * Default message service URI.
     */
    public static final String LOCALHOST_IN_VM_URI = "vm://localhost?broker.persistent=false";

    /**
     * Default Broker name.
     */
    public static final String DEFAULT_BROKERNAME = "AbstractActiveMQJmsTest_Broker";

    // Internal state
    private BrokerService broker;
    private ActiveMQConnectionFactory connectionFactory;
    private String messageServiceURI;

    /**
     * Creates a new ActiveMQBroker instance using the DEFAULT_BROKERNAME and LOCALHOST_IN_VM_URI.
     */
    public ActiveMQBroker() {
        this(DEFAULT_BROKERNAME, LOCALHOST_IN_VM_URI);
    }

    /**
     * Creates a new ActiveMQBroker instance using the supplied brokerServiceName and messageServiceURI.
     *
     * @param brokerServiceName The name of the ActiveMQ BrokerService.
     * @param messageServiceURI The URI used to connect to the ActiveMQ broker.
     */
    public ActiveMQBroker(final String brokerServiceName, final String messageServiceURI) {

        // Check sanity
        Validate.notEmpty(brokerServiceName, "Cannot handle null or empty brokerServiceName argument.");
        Validate.notEmpty(messageServiceURI, "Cannot handle null or empty messageServiceURI argument.");

        // Assign internal state
        this.messageServiceURI = messageServiceURI;
        broker = new BrokerService();
        broker.setBrokerName(brokerServiceName);
    }

    /**
     * @return a broker connection URI, suited for unit tests.
     *         Override it to
     */
    @Override
    public final String getMessageServerURI() {
        return messageServiceURI;
    }

    /**
     * @return The ActiveMQ BrokerService instance.
     */
    public final BrokerService getBroker() {
        return broker;
    }

    /**
     * Starts the MessageBroker.
     *
     * @throws Exception if the broker could not be properly started.
     */
    @Override
    public void startBroker() throws Exception {

        // Create a connection factory and add the corresponding connector.
        connectionFactory = new ActiveMQConnectionFactory(getMessageServerURI());
        broker.addConnector(getMessageServerURI());

        // Start the broker.
        broker.setPersistent(false);
        broker.start();
    }

    /**
     * Stops the MessageBroker.
     *
     * @throws Exception if the broker could not be properly stopped.
     */
    @Override
    public void stopBroker() throws Exception {

        // Stop the Brokerservice
        broker.stop();
    }

    /**
     * Retrieves a fully configured ConnectionFactory from the wrapped MessageBroker.
     *
     * @param configuration A configuration parameter to the broker for creating a ConnectionFactory.
     * @return a fully configured ConnectionFactory from the wrapped MessageBroker.
     */
    @Override
    public final ConnectionFactory getConnectionFactory(final String configuration) {
        return connectionFactory;
    }

    /**
     * Retrieves the human-readable name of this MessageBroker.
     *
     * @return The human-readable name of this MessageBroker.
     */
    @Override
    public final String getName() {
        return broker.getBrokerName();
    }
}
