/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
 */

package se.jguru.nazgul.core.messaging.api;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.activemq.AbstractActiveMqTest;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class InteractionPatternRealizationTest extends AbstractActiveMqTest {

    // Shared state
    final int numMessages = 25;
    private String requestQueueName = "requestQueue";
    private String responseQueueName = "responseQueue";

    private Connection serverSideConnection;
    private Session serviceSideRequestSession;
    private Session serviceSideResponseSession;

    private LatchedMessageListener serviceRequestListener;
    private List<TextMessage> receivedRequestMessages = new ArrayList<TextMessage>();
    private List<TextMessage> sentResponseMessages = new ArrayList<TextMessage>();

    public InteractionPatternRealizationTest() {
        super(false);
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     */
    @Override
    public void setupServices() throws JMSException {

        // Start a server-side connection & session
        serverSideConnection = createConnection();
        serviceSideRequestSession = createSession(serverSideConnection);
        serviceSideResponseSession = createSession(serverSideConnection);

        final Queue serviceSideRequestQueue = serviceSideRequestSession.createQueue(requestQueueName);
        final Queue serviceSideResponseQueue = serviceSideResponseSession.createQueue(responseQueueName);

        final MessageConsumer requestConsumer = serviceSideRequestSession.createConsumer(serviceSideRequestQueue);
        final MessageProducer responseProducer = serviceSideResponseSession.createProducer(serviceSideResponseQueue);
        responseProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Add an inbound request listener
        MessageListener loggingEchoListener = new MessageListener() {
            @Override
            public void onMessage(final Message message) {

                try {
                    // We should really only get text messages
                    TextMessage textMessage = (TextMessage) message;

                    // Copy the inbound message
                    receivedRequestMessages.add(textMessage);

                    // Find the jms replyTo queue
                    Destination jmsReplyTo = textMessage.getJMSReplyTo();
                    if(jmsReplyTo == null) {
                        jmsReplyTo = serviceSideResponseQueue;
                    }

                    // Create a message clone
                    final TextMessage responseMessage = serviceSideResponseSession.createTextMessage(textMessage.getText());
                    responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                    responseMessage.setStringProperty("requestJMSMessageID", textMessage.getJMSMessageID());

                    for(Enumeration en = textMessage.getPropertyNames(); en.hasMoreElements(); ) {
                        final String key = "" + en.nextElement();
                        if(!key.toLowerCase().contains("jms")) {
                            responseMessage.setStringProperty(key, "" + textMessage.getStringProperty(key));
                        }
                    }

                    // Log and send the message
                    sentResponseMessages.add(responseMessage);
                    responseProducer.send(jmsReplyTo, textMessage);

                    if(isTransactedOperation()) {
                        serviceSideResponseSession.commit();
                    }

                } catch (JMSException e) {
                    throw new IllegalArgumentException("Could not process message.", e);
                }
            }
        };

        serviceRequestListener = new LatchedMessageListener(loggingEchoListener);
        requestConsumer.setMessageListener(serviceRequestListener);
    }

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     */
    @Override
    public void tearDownServices() throws JMSException {

        // Close all open resource objects
        serviceSideRequestSession.close();
        serviceSideResponseSession.close();

        // Close the connections.
        serverSideConnection.close();
    }

    @Test
    public void validateFireAndForgetInteractionPattern() throws JMSException, InterruptedException {

        // Assemble
        final Connection clientSideConnection = createConnection();
        final Session clientSideRequestSession = clientSideConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Session clientSideResponseSession = clientSideConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue clientSideRequestQueue = clientSideRequestSession.createQueue(requestQueueName);
        final Queue clientSideResponseQueue = clientSideResponseSession.createQueue(responseQueueName);

        final MessageProducer requestProducer = clientSideRequestSession.createProducer(clientSideRequestQueue);
        final MessageConsumer responseConsumer = clientSideResponseSession.createConsumer(clientSideResponseQueue);
        requestProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        final List<TextMessage> receivedResponses = new ArrayList<TextMessage>();
        final CountDownLatch receiveMessagesLatch = new CountDownLatch(numMessages);

        final MessageListener responseListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {

                TextMessage received = (TextMessage) message;
                receivedResponses.add(received);
                receiveMessagesLatch.countDown();
            }
        };

        responseConsumer.setMessageListener(responseListener);

        // Act
        serviceRequestListener.okToProceed();
        for(int i = 0; i < numMessages; i++) {

            // Tailor the sent message a trifle.
            final TextMessage toSend = clientSideRequestSession.createTextMessage("message [" + i + "]");
            toSend.setIntProperty("messageId", i);

            // ... and send it.
            requestProducer.send(toSend);

            // Commit the session to actually send the message, if running in transacted operation mode.
            if(isTransactedOperation()) {
                clientSideRequestSession.commit();
            }
        }

        receiveMessagesLatch.await();

        // Assert
        validateMessages(receivedResponses, numMessages);
        validateMessages(receivedRequestMessages, numMessages);
        validateMessages(sentResponseMessages, numMessages);
    }

    //
    // Private helpers
    //

    private void validateMessages(List<TextMessage> toValidate, int expectedNumber) throws JMSException {

        Assert.assertEquals(expectedNumber, toValidate.size());
        for(int i = 0; i < toValidate.size(); i++) {

            final TextMessage textMessage = toValidate.get(i);
            Assert.assertEquals("message [" + i + "]", textMessage.getText());
            Assert.assertEquals(i, textMessage.getIntProperty("messageId"));
        }
    }
}
