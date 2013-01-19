/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.test.messaging;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Wrapper specification for a MessageBroker instance,
 * to be controlled for the purposes of automated tests.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MessageBroker {

    /**
     * Starts the MessageBroker.
     *
     * @throws Exception if the broker could not be properly started.
     */
    void startBroker() throws Exception;

    /**
     * Stops the MessageBroker.
     *
     * @throws Exception if the broker could not be properly stopped.
     */
    void stopBroker() throws Exception;

    /**
     * @return a broker connection URI, suited for unit tests or integration tests as required.
     */
    String getMessageServerURI();

    /**
     * Retrieves a fully configured ConnectionFactory from the wrapped MessageBroker.
     *
     * @param configuration A configuration parameter to the broker for creating a ConnectionFactory.
     * @return a fully configured ConnectionFactory from the wrapped MessageBroker.
     */
    ConnectionFactory getConnectionFactory(final String configuration);

    /**
     * Retrieves the human-readable name of this MessageBroker.
     *
     * @return The human-readable name of this MessageBroker.
     */
    String getName();
}
