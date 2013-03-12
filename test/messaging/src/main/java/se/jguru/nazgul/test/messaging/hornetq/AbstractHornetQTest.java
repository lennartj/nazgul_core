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

package se.jguru.nazgul.test.messaging.hornetq;

import se.jguru.nazgul.test.messaging.AbstractJmsTest;

/**
 * HornetQ-flavoured implementation of the AbstractJmsTest.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractHornetQTest extends AbstractJmsTest {

    /**
     * Creates a new AbstractHornetQTest with the supplied transactedOperation status,
     * and using a default HornetQBroker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     */
    public AbstractHornetQTest(final boolean transactedOperation) {
        this(transactedOperation, new HornetQBroker());
    }

    /**
     * Creates an AbstractHornetQTest instance using the supplied transactedOperation setting,
     * and the supplied broker instance.
     *
     * @param transactedOperation {@code true} if the HornetQBroker is transactional,
     *                            and {@code false} otherwise.
     * @param broker              a non-null HornetQBroker instance.
     */
    public AbstractHornetQTest(final boolean transactedOperation, final HornetQBroker broker) {
        super(transactedOperation, broker);
    }
}
