/*
 * #%L
 * Nazgul Project: nazgul-core-messaging-test
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */

package se.jguru.nazgul.test.messaging.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.ConnectionFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    public ActiveMQBroker(@NotNull @Size(min = 1) final String brokerServiceName,
                          @NotNull @Size(min = 1) final String messageServiceURI) {

        // Check sanity
        Validate.notEmpty(brokerServiceName, "brokerServiceName");
        Validate.notEmpty(messageServiceURI, "messageServiceURI");

        // Assign internal state
        this.messageServiceURI = messageServiceURI;
        broker = new BrokerService();
        broker.setBrokerName(brokerServiceName);
    }

    /**
     * @return a broker connection URI, suited for unit tests.
     * Override it to
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
        broker.waitUntilStarted();
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
        broker.waitUntilStopped();
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
