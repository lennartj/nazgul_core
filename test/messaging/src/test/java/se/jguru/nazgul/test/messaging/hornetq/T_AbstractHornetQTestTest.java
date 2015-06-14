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

package se.jguru.nazgul.test.messaging.hornetq;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.messaging.MessageBroker;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractHornetQTestTest {

    // Shared state
    private MockHornetQTest unitUnderTest;
    private MessageBroker broker;

    @Before
    public void setupSharedState() {
        unitUnderTest = new MockHornetQTest(false);
        broker = unitUnderTest.getBroker();
    }

    @After
    public void teardownSharedState() throws Exception {
        unitUnderTest.stopJmsBroker();
    }

    @Test
    public void validateBrokerTypeAndLifecycle() throws Exception {

        // Assemble

        // Act
        unitUnderTest.startJmsBroker();
        unitUnderTest.stopJmsBroker();

        // Assert
        Assert.assertNotNull(broker);
        Assert.assertTrue(broker instanceof HornetQBroker);

        final List<String> callTrace = unitUnderTest.callTrace;
        Assert.assertEquals(2, callTrace.size());
        Assert.assertEquals("setupServices", callTrace.get(0));
        Assert.assertEquals("tearDownServices", callTrace.get(1));
    }

    @Test
    public void validateProperCreationOfNonTransactedJmsObjects() throws Exception {

        // Assemble

        // Assert
        unitUnderTest.startJmsBroker();
        final Connection connection = unitUnderTest.createConnection();
        final Session session = unitUnderTest.createSession(connection);
        final Queue clientSideRequestQueue = session.createTemporaryQueue();
        final MessageProducer requestProducer = session.createProducer(clientSideRequestQueue);
        final TextMessage message = session.createTextMessage("fooBar!");
        requestProducer.send(message);
        unitUnderTest.stopJmsBroker();

        // Act
        Assert.assertNotNull(connection);
        Assert.assertNotNull(session);
        Assert.assertNotNull(requestProducer);
        Assert.assertNotNull(message);
    }
}
