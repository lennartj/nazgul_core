/*-
 * #%L
 * Nazgul Project: nazgul-core-messaging-test
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import javax.jms.ConnectionFactory;

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
