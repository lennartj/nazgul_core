/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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
package se.jguru.nazgul.core.cache.impl.hazelcast.helpers;

import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Native Hazelcast EntryListener implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ThreadAwareSynchronizedEntryListener extends EntryAdapter<String, String> {

    // Internal state
    private final Object[] lock = new Object[0];
    private AtomicInteger counter = new AtomicInteger();
    private SortedMap<String, SortedMap<Integer, String>> threadName2OrderedMessagesMap
            = new TreeMap<String, SortedMap<Integer, String>>();

    @Override
    public void onEntryEvent(final EntryEvent<String, String> event) {
        synchronized (lock) {

            final SortedMap<Integer, String> orderedMessages = getOrderedMessageMap();
            final String message = "onEntryEvent: " + event.getEventType().toString()
                    + " - [" + event.getKey() + "]: " + event.getOldValue() + " --> " + event.getValue();
            orderedMessages.put(counter.getAndIncrement(), message);
        }
    }

    @Override
    public void onMapEvent(final MapEvent event) {
        synchronized (lock) {
            final SortedMap<Integer, String> orderedMessages = getOrderedMessageMap();
            final String message = "onMapEvent: " + event.getEventType().toString()
                    + " - [" + event.getName() + "(" + event.getNumberOfEntriesAffected() + "]: "
                    + event.getEventType();
            orderedMessages.put(counter.getAndIncrement(), message);
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("\n\n\n######################################\n");
        for (Map.Entry<String, SortedMap<Integer, String>> current : threadName2OrderedMessagesMap.entrySet()) {

            final String threadName = current.getKey();
            for (Map.Entry<Integer, String> currentMessageTuple : current.getValue().entrySet()) {
                builder.append("(" + threadName + ") [" + currentMessageTuple.getKey() + "]: "
                        + currentMessageTuple.getValue() + "\n");
            }
        }
        return builder.toString() + "######################################\n\n\n";
    }

    public SortedMap<String, SortedMap<Integer, String>> getThreadName2OrderedMessagesMap() {
        return threadName2OrderedMessagesMap;
    }

    //
    // Private helpers
    //

    private SortedMap<Integer, String> getOrderedMessageMap() {

        final String threadName = Thread.currentThread().getName();

        SortedMap<Integer, String> orderedMessages = threadName2OrderedMessagesMap.get(threadName);
        if (orderedMessages == null) {
            orderedMessages = new TreeMap<Integer, String>();
            threadName2OrderedMessagesMap.put(threadName, orderedMessages);
        }

        return orderedMessages;
    }
}
