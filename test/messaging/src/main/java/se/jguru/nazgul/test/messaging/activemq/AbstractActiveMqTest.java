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


package se.jguru.nazgul.test.messaging.activemq;

import se.jguru.nazgul.test.messaging.AbstractJmsTest;

/**
 * ActiveMQ-flavoured implementation of the AbstractJmsTest.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractActiveMqTest extends AbstractJmsTest {

    /**
     * Creates a default (i.e. unit-test-tailored) ActiveMQBroker using
     * the supplied transactedOperation setting.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public AbstractActiveMqTest(final boolean transactedOperation) {
        this(transactedOperation, new ActiveMQBroker());
    }

    /**
     * Creates an AbstractActiveMqTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the ActiveMQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null ActiveMQBroker instance.
     */
    public AbstractActiveMqTest(final boolean transactedOperation, final ActiveMQBroker broker) {
        super(transactedOperation, broker);
    }
}
