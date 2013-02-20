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
