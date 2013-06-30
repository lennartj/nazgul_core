package se.jguru.nazgul.core.cache.impl.inmemory;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory which gives all made Threads a name and sequence number.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NamedSequenceThreadFactory implements ThreadFactory {

    // Internal state
    private static final AtomicInteger counter = new AtomicInteger(0);
    private String threadNamePrefix;
    private int maxSequenceNumber;
    private final Object lock = new Object();

    public NamedSequenceThreadFactory(final String threadNamePrefix) {
        this(threadNamePrefix, Integer.MAX_VALUE);
    }

    /**
     *
     * @param threadNamePrefix
     * @param maxSequenceNumber
     */
    public NamedSequenceThreadFactory(final String threadNamePrefix,
                                      final int maxSequenceNumber) {
        // Check sanity
        Validate.notEmpty(threadNamePrefix, "Cannot handle null or empty threadNamePrefix argument.");
        Validate.isTrue(maxSequenceNumber > 0, "Cannot handle zero or negative maxSequenceNumber argument.");

        // Assign internal state
        this.threadNamePrefix = threadNamePrefix;
        this.maxSequenceNumber = maxSequenceNumber;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param r a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     *         create a thread is rejected
     */
    @Override
    public Thread newThread(final Runnable r) {

        synchronized (lock) {
            if(counter.get() == maxSequenceNumber) {
                counter.set(0);
            }
        }

        // All done.
        return new Thread(r, threadNamePrefix + "-" + counter.getAndIncrement());
    }
}
