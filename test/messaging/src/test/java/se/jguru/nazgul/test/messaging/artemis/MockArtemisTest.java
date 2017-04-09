/*
 * #%L
 * Nazgul Project: nazgul-core-messaging-test
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.test.messaging.artemis;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockArtemisTest extends AbstractArtemisTest {

    public List<String> callTrace = new ArrayList<String>();

    /**
     * Creates a new AbstractHornetQTest with the supplied transactedOperation status,
     * and using a default HornetQBroker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public MockArtemisTest(boolean transactedOperation) {
        super(transactedOperation);
    }

    /**
     * Creates an AbstractHornetQTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null HornetQBroker instance.
     */
    public MockArtemisTest(boolean transactedOperation, ArtemisBroker broker) {
        super(transactedOperation, broker);
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void setupServices() throws JMSException {
        callTrace.add("setupServices");
    }

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     *
     * @throws JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void tearDownServices() throws JMSException {
        callTrace.add("tearDownServices");
    }

    /**
     * @return a started JMS Connection to the Broker using the known connectionFactory.
     * @throws JMSException if the connection could not be created or started.
     */
    @Override
    public Connection createConnection() throws JMSException {
        return super.createConnection();
    }

    /**
     * Creates a JMS session from the supplied connection, and using the value of the {@code transactedOperation}
     * to determine the transaction state.
     *
     * @param connection The connection used to create a session.
     * @return A JMS session from the supplied connection.
     * @throws JMSException if the createSession operation failed.
     */
    @Override
    public Session createSession(Connection connection) throws JMSException {
        return super.createSession(connection);
    }

    /**
     * Creates a JMS session from the supplied connection.
     *
     * @param connection The connection used to create a session.
     * @param transacted if {@code true}, the retrieved session is transacted.
     * @return A JMS session from the supplied connection.
     * @throws JMSException if the createSession operation failed.
     */
    @Override
    public Session createSession(Connection connection, boolean transacted) throws JMSException {
        return super.createSession(connection, transacted);
    }
}
