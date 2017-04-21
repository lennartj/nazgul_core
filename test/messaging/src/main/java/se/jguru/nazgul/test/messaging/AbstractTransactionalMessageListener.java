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
package se.jguru.nazgul.test.messaging;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Abstract implementation of a MessageListener which simplifies OOC JMS testing.
 * This MessageListener should be used on the service side only.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class AbstractTransactionalMessageListener implements MessageListener {

    // Internal state
    private List<Message> serverSideReceivedMessages;
    private Session serverSideResponseSession;
    private MessageProducer responseMessageProducer;

    /**
     * Creates a new AbstractTransactionalMessageListener wrapping the supplied objects.
     *
     * @param serverSideReceivedMessages The non-null List to which this AbstractTransactionalMessageListener will
     *                                   copy all inbound messages for test tracking purposes.
     * @param serverSideResponseSession  The non-null session used to create outbound (i.e. response) messages from
     *                                   this MessageListener. Also used to commit JMS transactions.
     * @param responseMessageProducer    The non-null MessageProducer, created from the supplied
     *                                   serverSideResponseSession, used to send response messages from the
     */
    public AbstractTransactionalMessageListener(@NotNull final List<Message> serverSideReceivedMessages,
                                                @NotNull final Session serverSideResponseSession,
                                                @NotNull final MessageProducer responseMessageProducer) {

        // Check sanity
        Validate.notNull(serverSideReceivedMessages, "serverSideReceivedMessages");
        Validate.notNull(serverSideResponseSession, "serverSideResponseSession");
        Validate.notNull(responseMessageProducer, "responseMessageProducer");

        // Assign internal state
        this.serverSideReceivedMessages = serverSideReceivedMessages;
        this.serverSideResponseSession = serverSideResponseSession;
        this.responseMessageProducer = responseMessageProducer;
    }

    /**
     * Template onMessage method, delegating all real processing to {@code generateResponse}.
     *
     * @see #generateResponse(javax.jms.Session, javax.jms.Message)
     */
    @Override
    public final void onMessage(final Message message) {

        // Stash the received message for test purposes
        serverSideReceivedMessages.add(message);

        try {

            // Generate a response.
            final TextMessage toReturn = generateResponse(serverSideResponseSession, message);

            // Send the error message back to the client.
            responseMessageProducer.send(toReturn);
            serverSideResponseSession.commit();

        } catch (JMSException e) {
            throw new IllegalStateException("Could not send message.", e);
        }
    }

    /**
     * Override this method to produce a response from an inbound request.
     *
     * @param inboundRequestMessage     The inbound request message, sent from the client side.
     * @param serverSideResponseSession The server-side session from which the response JMS Message is created.
     * @param <T>                       The explicit Message type.
     * @return The response Message to be sent out by the JMS server side in response to the client request.
     * @throws javax.jms.JMSException If the creating of JMS messages fails.
     */
    protected abstract <T extends Message> T generateResponse(final Session serverSideResponseSession,
                                                              final Message inboundRequestMessage) throws JMSException;
}
