/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-inmemory
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
    private final AtomicInteger sequenceCounter = new AtomicInteger(0);
    private String threadNamePrefix;
    private int maxSequenceNumber;
    private final Object lock = new Object();

    /**
     * Convenience constructor using {@code Integer.MAX_VALUE} for the maximum sequence number.
     *
     * @param threadNamePrefix A string prepended to the name of any Thread created by this NamedSequenceThreadFactory.
     */
    public NamedSequenceThreadFactory(final String threadNamePrefix) {
        this(threadNamePrefix, Integer.MAX_VALUE);
    }

    /**
     * Compound constructor creating a new NamedSequenceThreadFactory from the supplied parameters.
     *
     * @param threadNamePrefix  A string prepended to the name of any Thread created by this NamedSequenceThreadFactory.
     * @param maxSequenceNumber The maximum sequence number for this ThreadFactory.
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
     * Constructs a new {@code Thread}, using a default threadname on the form
     * {@code threadNamePrefix-nn}, where {@code nn} is the sequence number of the
     * retrieved Thread.
     *
     * @param r a runnable to be executed by new thread instance.
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    @SuppressWarnings("all")
    public Thread newThread(final Runnable r) {

        synchronized (lock) {
            if (sequenceCounter.get() == maxSequenceNumber) {
                sequenceCounter.set(0);
            }
        }

        // All done.
        return new Thread(r, threadNamePrefix + "-" + sequenceCounter.getAndIncrement());
    }
}
