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

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LatchedMessageListener implements MessageListener {

    // Internal state
    private CountDownLatch latch;
    private MessageListener delegate;

    public LatchedMessageListener(final MessageListener delegate) {
        this.delegate = delegate;
        this.latch = new CountDownLatch(1);
    }

    /**
     * Delegates inbound messages to the wrapped MessageListener,
     * following an {@code okToProceed} method call.
     *
     * @param message The message to delegate.
     */
    @Override
    public void onMessage(final Message message) {

        try {
            // Should we proceed?
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception waiting for latch", e);
        }

        // Delegate
        delegate.onMessage(message);
    }

    /**
     * Releases the latch barrier, permitting message flow.
     */
    public void okToProceed() {
        latch.countDown();
    }
}
