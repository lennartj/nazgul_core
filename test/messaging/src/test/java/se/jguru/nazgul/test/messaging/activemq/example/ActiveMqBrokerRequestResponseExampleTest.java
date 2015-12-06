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
package se.jguru.nazgul.test.messaging.activemq.example;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.AbstractRequestResponseJmsTest;
import se.jguru.nazgul.test.messaging.AbstractTransactionalMessageListener;
import se.jguru.nazgul.test.messaging.activemq.ActiveMQBroker;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class ActiveMqBrokerRequestResponseExampleTest extends AbstractRequestResponseJmsTest {

    /**
     * Use transacted operation.
     */
    public ActiveMqBrokerRequestResponseExampleTest() {
        super(true, new ActiveMQBroker());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    protected <R extends AbstractTransactionalMessageListener> R getServiceSideListener(
            final List<Message> serverSideReceivedMessages,
            final Session serverSideResponseSession,
            final MessageProducer responseMessageProducer) {

        return (R) new AbstractTransactionalMessageListener(
                serverSideReceivedMessages,
                serverSideResponseSession,
                responseMessageProducer) {

            /**
             * {@inheritDoc}
             */
            @Override
            protected <T extends Message> T generateResponse(final Session serverSideResponseSession,
                                                             final Message inboundRequestMessage) throws JMSException {
                // Define the outbound message
                final TextMessage toReturn = serverSideResponseSession.createTextMessage();
                toReturn.setJMSCorrelationID(inboundRequestMessage.getJMSMessageID());

                // This test service is designed only to properly accept incoming TextMessages.
                if (!(inboundRequestMessage instanceof TextMessage)) {

                    // Create an error message.
                    toReturn.setText("Only text messages are handled. Received ["
                            + inboundRequestMessage.getClass().getSimpleName() + "]");
                } else {

                    // Create a 'proper' response holding the body of the
                    // inbound TextMessage + some extra text.
                    final TextMessage msg = (TextMessage) inboundRequestMessage;
                    toReturn.setText("Received inbound: " + msg.getText());
                }

                // Send the error message back to the client.
                // responseMessageProducer.send(toReturn);
                // serverSideResponseSession.commit();

                // All done.
                return (T) toReturn;
            }
        };
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

        if(receivedClientResponses.size() != 1) {
            final List<String> messageClientList = new ArrayList<String>();
            for(Message current : receivedClientResponses) {

                final TextMessage received = (TextMessage) current;
                messageClientList.add(received.getJMSMessageID() + " --> [" + received.getText() + "]");
            }

            Assert.fail("Expected 1 received client response message, but got [" + receivedClientResponses.size()
                            + "]: " + messageClientList);
        }

        Assert.assertEquals(1, receivedClientResponses.size());
        Assert.assertEquals("Expected 1 received server message, but got: " + serverSideReceivedMessages,
                1, serverSideReceivedMessages.size());

        final TextMessage response = (TextMessage) receivedClientResponses.get(0);
        Assert.assertTrue(response.getText().startsWith("Only text messages are handled."));

        Assert.assertTrue(serverSideReceivedMessages.get(0) instanceof MapMessage);
    }

    @Test
    public void validateTextMessageYieldsCorrectResponse() throws Exception {

        // Assemble
        final String clientMessage = "This is a client-side originated message.";
        final List<Message> receivedClientResponses = new CopyOnWriteArrayList<Message>();

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

        final boolean correctlyReceivedMessages = receivedMessagesLatch.await(2, TimeUnit.SECONDS);

        // Assert
        Assert.assertTrue(correctlyReceivedMessages);

        Assert.assertEquals("Expected 1 received client response message, but got [" + receivedClientResponses.size()
                + "]: " + receivedClientResponses, 1, receivedClientResponses.size());
        Assert.assertEquals("Expected 1 received server message, but got: " + serverSideReceivedMessages,
                1, serverSideReceivedMessages.size());

        final TextMessage response = (TextMessage) receivedClientResponses.get(0);
        Assert.assertEquals(response.getText(), "Received inbound: " + clientMessage);
    }
}
