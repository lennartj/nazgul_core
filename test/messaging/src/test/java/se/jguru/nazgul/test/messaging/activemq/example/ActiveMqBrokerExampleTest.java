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
package se.jguru.nazgul.test.messaging.activemq.example;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.activemq.AbstractActiveMqTest;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Example JMS test example.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActiveMqBrokerExampleTest extends AbstractActiveMqTest {

    public static final String SERVER_SIDE_INBOUND_REQUEST = "service.inbound.request";
    public static final String SERVER_SIDE_OUTBOUND_RESPONSE = "service.outbound.response";
    public static final String CLIENT_SIDE_OUTBOUND_REQUEST = SERVER_SIDE_INBOUND_REQUEST;
    public static final String CLIENT_SIDE_INBOUND_RESPONSE = SERVER_SIDE_OUTBOUND_RESPONSE;

    // Shared state
    private List<Message> serverSideReceivedMessages;

    /**
     * Use transacted operation.
     */
    public ActiveMqBrokerExampleTest() {
        super(true);
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
        requestMessageConsumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(final Message message) {

                // Stash the received message for test purposes
                serverSideReceivedMessages.add(message);

                try {

                    // Define the outbound message
                    final TextMessage toReturn = serverSideResponseSession.createTextMessage();
                    toReturn.setJMSCorrelationID(message.getJMSMessageID());

                    // This test service is designed only to properly accept incoming TextMessages.
                    if (!(message instanceof TextMessage)) {

                        // Create an error message.
                        toReturn.setText("Only text messages are handled. Received ["
                                + message.getClass().getSimpleName() + "]");
                    } else {

                        // Create a 'proper' response holding the body of the
                        // inbound TextMessage + some extra text.
                        final TextMessage msg = (TextMessage) message;
                        toReturn.setText("Received inbound: " + msg.getText());
                    }

                    // Send the error message back to the client.
                    responseMessageProducer.send(toReturn);
                    serverSideResponseSession.commit();

                } catch (JMSException e) {
                    throw new IllegalStateException("Could not send message.", e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDownServices() throws JMSException {
    }

    @Test
    public void validateMapMessageYieldsErrorTypeResponse() throws Exception {

        // Assemble
        final List<Message> receivedClientResponses = new ArrayList<Message>();

        final Connection clientConnection = createConnection();
        final Session clientRequestSession = createSession(clientConnection);
        final Queue clientRequestQueue = clientRequestSession.createQueue(CLIENT_SIDE_OUTBOUND_REQUEST);

        final Session clientResponseSession = createSession(clientConnection);
        final Queue clientResponseQueue = clientResponseSession.createQueue(CLIENT_SIDE_INBOUND_RESPONSE);

        final MessageProducer clientRequestProducer = clientRequestSession.createProducer(clientRequestQueue);
        clientRequestProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        final CountDownLatch receivedMessagesLatch = new CountDownLatch(1);

        final MessageConsumer clientResponseConsumer = clientResponseSession.createConsumer(clientResponseQueue);
        final MessageListener clientResponseListener = new MessageListener() {
            @Override
            public void onMessage(final Message message) {
                receivedClientResponses.add(message);
                receivedMessagesLatch.countDown();
            }
        };
        clientResponseConsumer.setMessageListener(clientResponseListener);

        // Act
        final MapMessage toSend = clientRequestSession.createMapMessage();
        toSend.setStringProperty("foo", "bar");
        toSend.setString("gnat", "gnu");
        clientRequestProducer.send(toSend);
        clientRequestSession.commit();

        final boolean correctlyReceivedMessage = receivedMessagesLatch.await(2, TimeUnit.SECONDS);

        // Assert
        Assert.assertTrue(correctlyReceivedMessage);
        Assert.assertEquals(1, receivedClientResponses.size());
        Assert.assertEquals(1, serverSideReceivedMessages.size());

        final TextMessage response = (TextMessage) receivedClientResponses.get(0);
        Assert.assertTrue(response.getText().startsWith("Only text messages are handled."));

        Assert.assertTrue(serverSideReceivedMessages.get(0) instanceof MapMessage);
    }

    @Test
    public void validateTextMessageYieldsCorrectResponse() throws Exception {

        // Assemble
        final String clientMessage = "This is a client-side originated message.";
        final List<Message> receivedClientResponses = new ArrayList<Message>();

        final Connection clientConnection = createConnection();
        final Session clientRequestSession = createSession(clientConnection);
        final Queue clientRequestQueue = clientRequestSession.createQueue(CLIENT_SIDE_OUTBOUND_REQUEST);

        final Session clientResponseSession = createSession(clientConnection);
        final Queue clientResponseQueue = clientResponseSession.createQueue(CLIENT_SIDE_INBOUND_RESPONSE);

        final MessageProducer clientRequestProducer = clientRequestSession.createProducer(clientRequestQueue);
        clientRequestProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        final CountDownLatch receivedMessagesLatch = new CountDownLatch(1);

        final MessageConsumer clientResponseConsumer = clientResponseSession.createConsumer(clientResponseQueue);
        final MessageListener clientResponseListener = new MessageListener() {
            @Override
            public void onMessage(final Message message) {
                receivedClientResponses.add(message);
                receivedMessagesLatch.countDown();
            }
        };
        clientResponseConsumer.setMessageListener(clientResponseListener);

        // Act
        final TextMessage toSend = clientRequestSession.createTextMessage();
        toSend.setStringProperty("foo", "bar");
        toSend.setText(clientMessage);
        clientRequestProducer.send(toSend);
        clientRequestSession.commit();

        final boolean correctlyReceivedMessage = receivedMessagesLatch.await(2, TimeUnit.SECONDS);

        // Assert
        Assert.assertTrue(correctlyReceivedMessage);
        Assert.assertEquals(1, receivedClientResponses.size());
        Assert.assertEquals(1, serverSideReceivedMessages.size());

        final TextMessage response = (TextMessage) receivedClientResponses.get(0);
        Assert.assertEquals(response.getText(), "Received inbound: " + clientMessage);
    }
}
