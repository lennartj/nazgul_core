/*
 * #%L
 * Nazgul Project: nazgul-core-messaging-test
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.test.messaging;

import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Abstract superclass for JMS-related tests, wrapping a MessageBroker
 * to which all calls are delegated.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJmsTest {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(AbstractJmsTest.class);

    // Internal state
    protected MessageBroker broker;
    private ConnectionFactory connectionFactory;
    private boolean transactedOperation = false;

    /**
     * Creates a new AbstractJmsTest instance, returning transacted JMS objects
     * by default as indicated by the supplied parameter.
     *
     * @param transactedOperation if {@code true}, all retrieved sessions will be transacted by default.
     */
    protected AbstractJmsTest(final boolean transactedOperation, final MessageBroker broker) {

        // Check sanity
        Validate.notNull(broker, "Cannot handle null broker argument.");

        // Assign internal state
        this.transactedOperation = transactedOperation;
        this.broker = broker;
    }

    /**
     * @return {@code true} to indicate that this AbstractActiveMQTest is configured
     *         to run in transacted mode (i.e. that all JMS objects requested from this
     *         superclass are configured to use transactions).
     */
    public boolean isTransactedOperation() {
        return transactedOperation;
    }

    /**
     * Starts the JMS BrokerService.
     *
     * @throws JMSException if any of the underlying JMS methods does.
     */
    @Before
    public final void startJmsBroker() throws JMSException {

        try {
            // Start the broker.
            broker.startBroker();

        } catch (Exception e) {
            log.error("Could not start Broker [" + broker.getName() + "]. Bailing out.");
            throw new IllegalStateException(e);
        }

        // Acquire the ConnectionFactory
        this.connectionFactory = broker.getConnectionFactory(broker.getMessageServerURI());

        // Delegate to normal set up.
        afterStartJmsBroker();

        // Now setup JMS service-side clients.
        setupServices();
    }

    /**
     * Stops the JMS BrokerService.
     *
     * @throws JMSException if any of the underlying JMS methods does.
     */
    @After
    public final void stopJmsBroker() throws JMSException {

        // Tear down JMS service-side clients.
        tearDownServices();

        // Delegate to normal teardown.
        beforeStopJmsBroker();

        try {
            broker.stopBroker();
        } catch (final Exception e) {
            log.error("Could not stop Broker [" + broker.getName() + "].", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return a started JMS Connection to the Broker using the known connectionFactory.
     * @throws JMSException if the connection could not be created or started.
     */
    protected Connection createConnection() throws JMSException {

        // Check sanity
        validateInitialized("Connection");

        // Create, start & return.
        Connection toReturn = connectionFactory.createConnection();
        toReturn.start();
        return toReturn;
    }

    /**
     * Creates a JMS session from the supplied connection, and using the value of the {@code transactedOperation}
     * to determine the transaction state.
     *
     * @param connection The connection used to create a session.
     * @return A JMS session from the supplied connection.
     * @throws JMSException if the createSession operation failed.
     */
    protected Session createSession(final Connection connection) throws JMSException {
        return createSession(connection, transactedOperation);
    }

    /**
     * Creates a JMS session from the supplied connection.
     *
     * @param connection The connection used to create a session.
     * @param transacted if {@code true}, the retrieved session is transacted.
     * @return A JMS session from the supplied connection.
     * @throws JMSException if the createSession operation failed.
     */
    protected Session createSession(final Connection connection, final boolean transacted) throws JMSException {

        // Check sanity
        validateInitialized("Session");

        // Apply the appropriate acknowledgement.
        int acknowledge = transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE;

        // All done.
        return connection.createSession(transacted, acknowledge);
    }

    /**
     * Override this method to perform any normal setup
     * after the Broker is launched. For example, any classes
     * which should be registered with the broker should be
     * instantiated here.
     */
    protected void afterStartJmsBroker() {
        // Override this method to create the JMS server
        // implementation and register it into the broker.
    }

    /**
     * Override this method to perform any normal tear-down
     * before the Broker is stopped. You might cleanup any
     * instances which should be de-registered from the broker.
     */
    protected void beforeStopJmsBroker() {
        // Override this method to destroy the JMS server
        // implementation and de-register it from the broker.
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    public abstract void setupServices() throws JMSException;

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    public abstract void tearDownServices() throws JMSException;

    /**
     * Validates that the state of this AbstractJmsTest is initialized,
     * implying that the ConnectionFactory is not null.
     */
    private void validateInitialized(final String description) {
        if (this.connectionFactory == null) {
            throw new IllegalStateException("Cannot acquire " + description

                    + " before initializing the MessageBroker. ConnectionFactory: " + this.connectionFactory);
        }
    }

    /**
     * @return The wrapped MessageBroker instance.
     */
    public final MessageBroker getBroker() {
        return broker;
    }
}
