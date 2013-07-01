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

package se.jguru.nazgul.test.messaging.activemq;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MockActiveMqTest extends AbstractActiveMqTest {

    public List<String> callTrace = new ArrayList<String>();

    /**
     * Creates a default (i.e. unit-test-tailored) ActiveMQBroker using
     * the supplied transactedOperation setting.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public MockActiveMqTest(boolean transactedOperation) {
        super(transactedOperation);
    }

    /**
     * Creates an AbstractActiveMqTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null ActiveMQBroker instance.
     */
    public MockActiveMqTest(boolean transactedOperation, ActiveMQBroker broker) {
        super(transactedOperation, broker);
    }

    /**
     * Implement this method to setup any Services (i.e. server-side
     * listeners) that should be active and connected for the test.
     *
     * @throws javax.jms.JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void setupServices() throws JMSException {
        callTrace.add("setupServices");
    }

    /**
     * Implement this method to tear down any Services
     * that have been active and connected during the test.
     *
     * @throws javax.jms.JMSException if the underlying operations throws a JMSException
     */
    @Override
    public void tearDownServices() throws JMSException {
        callTrace.add("tearDownServices");
    }
}
