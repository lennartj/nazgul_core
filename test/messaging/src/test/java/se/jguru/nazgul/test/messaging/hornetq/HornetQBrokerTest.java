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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.test.messaging.hornetq;

import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.jms.ConnectionFactory;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class HornetQBrokerTest {

    // Shared state
    private HornetQBroker unitUnderTest;
    private EmbeddedJMS jmsServer;

    @Before
    public void createBroker() {
        unitUnderTest = new HornetQBroker();
        jmsServer = unitUnderTest.getJmsServer();
    }

    @After
    public void teardownBroker() throws Exception{
        unitUnderTest.stopBroker();
        Assert.assertNotNull(jmsServer);
    }


    @Test
    public void validateConfiguration() throws Exception {

        // Assemble

        // Act
        unitUnderTest.startBroker();

        final ConnectionFactory connectionFactory = unitUnderTest.getConnectionFactory("UnitTestConnectionFactory");

        // Assert
        Assert.assertNotNull(connectionFactory);
    }
}
