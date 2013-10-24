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

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of an AbstractJmsTest which sports a simplified setup
 * for service-side transacted MessageListeners.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class AbstractRequestResponseJmsTest extends AbstractJmsTest {

    public static final String SERVER_SIDE_INBOUND_REQUEST = "service.inbound.request";
    public static final String SERVER_SIDE_OUTBOUND_RESPONSE = "service.outbound.response";
    public static final String CLIENT_SIDE_OUTBOUND_REQUEST = SERVER_SIDE_INBOUND_REQUEST;
    public static final String CLIENT_SIDE_INBOUND_RESPONSE = SERVER_SIDE_OUTBOUND_RESPONSE;

    // Shared state
    protected List<Message> serverSideReceivedMessages;

    /**
     * Creates a new AbstractRequestResponseJmsTest instance, returning transacted JMS objects
     * by default as indicated by the supplied parameter.
     *
     * @param transactedOperation if {@code true}, all retrieved sessions will be transacted by default.
     */
    public AbstractRequestResponseJmsTest(final boolean transactedOperation, final MessageBroker broker) {
        super(transactedOperation, broker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDownServices() throws JMSException {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupServices() throws JMSException {

        serverSideReceivedMessages = new ArrayList<Message>();

        // This is where we set up JMS objects on the server side.
        // These objects are created before any test cases are launched.

        // 1) Get a connection to the JMS broker.
        final Connection serverSideConnection = createConnection();

        // 2) Create a server-side Session, Queue and MessageConsumer reading messages from the broker.
        final Session serverSideRequestSession = createSession(serverSideConnection);
        final Queue serviceSideInboundQueue = serverSideRequestSession.createQueue(SERVER_SIDE_INBOUND_REQUEST);
        final MessageConsumer requestMessageConsumer = serverSideRequestSession.createConsumer(serviceSideInboundQueue);

        // 3) Create a server-side Session, Queue and MessageProducer sending messages to the broker.
        final Session serverSideResponseSession = createSession(serverSideConnection);
        final Queue serviceSideOutboundQueue = serverSideResponseSession.createQueue(SERVER_SIDE_OUTBOUND_RESPONSE);
        final MessageProducer responseMessageProducer = serverSideResponseSession
                .createProducer(serviceSideOutboundQueue);
        responseMessageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // 4) Register a MessageListener to read messages from the requestMessageConsumer
        //    and write messages to the responseMessageProducer.
        //    This completes the server-side setup.
        requestMessageConsumer.setMessageListener(
                getServiceSideListener(
                        serverSideReceivedMessages,
                        serverSideResponseSession,
                        responseMessageProducer));
    }

    /**
     * Creates a new AbstractTransactionalMessageListener instance for service-side use
     * in this AbstractRequestResponseJmsTest.
     *
     * @param serverSideReceivedMessages The non-null List to which this AbstractTransactionalMessageListener will
     *                                   copy all inbound messages for test tracking purposes.
     * @param serverSideResponseSession  The non-null session used to create outbound (i.e. response) messages from
     *                                   this MessageListener. Also used to commit JMS transactions.
     * @param responseMessageProducer    The non-null MessageProducer, created from the supplied
     *                                   serverSideResponseSession, used to send response messages from the
     * @return The service-side listener used to handle inbound messages and send out responses.
     */
    protected abstract AbstractTransactionalMessageListener getServiceSideListener(
            final List<Message> serverSideReceivedMessages,
            final Session serverSideResponseSession,
            final MessageProducer responseMessageProducer);
}
