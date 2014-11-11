/*
 * #%L
 * Nazgul Project: nazgul-core-cache-impl-hazelcast
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
package se.jguru.nazgul.core.cache.impl.hazelcast.helpers;

import se.jguru.nazgul.core.cache.api.AbstractCacheListener;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Debug / Test CacheListenerAdapter implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugCacheListener<T> extends AbstractCacheListener<String, T> {

    // Internal state
    private final Object[] lock = new Object[0];
    private AtomicInteger index = new AtomicInteger(1);
    public TreeMap<Integer, DebugCacheListener<T>.EventInfo> eventId2EventInfoMap
            = new TreeMap<Integer, DebugCacheListener<T>.EventInfo>();

    public class EventInfo implements Externalizable {

        // Shared state
        public String eventType;
        public String key;
        public Object value;
        public String threadName;

        public EventInfo(final String eventType, final String key, final String threadName, final Object value) {
            this.eventType = eventType;
            this.key = key;
            this.value = value;
            this.threadName = threadName;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeUTF(eventType);
            out.writeUTF(key);
            out.writeUTF(threadName);
            out.writeObject(value);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            eventType = in.readUTF();
            key = in.readUTF();
            threadName = in.readUTF();
            value = in.readObject();
        }

        @Override
        public String toString() {
            return eventType + ":" + key + ":" + value;
        }
    }

    /**
     * Creates a new DebugCacheListener with the given id.
     *
     * @param id a unique ID for this DebugCacheListener.
     */
    public DebugCacheListener(String id) {
        super(id);
    }

    public DebugCacheListener() {
        super("This constructor is for Serialization only");
    }

    /**
     * Callback method invoked when the object with the given key
     * is stored within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object
     * @param value The value which was put.
     */
    @Override
    public void doOnPut(String key, T value) {
        addEvent("put", key, value);
    }

    /**
     * Callback method invoked when the object with the given key
     * is updated within the underlying cache implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key      The cache key.
     * @param newValue The new value - after the update.
     * @param oldValue The former value - before the update.
     */
    @Override
    public void doOnUpdate(String key, T newValue, T oldValue) {
        addEvent("update", key, newValue);
    }

    /**
     * Callback method invoked when the object with the given
     * key is actively removed from the underlying cache
     * implementation (by a user call).
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was removed.
     */
    @Override
    public void doOnRemove(String key, T value) {
        addEvent("remove", key, value);
    }

    /**
     * Callback method invoked when the underlying cache
     * is cleared (i.e. its state destroyed and all cached
     * objects evicted).
     */
    @Override
    public void onClear() {
        addEvent("clear", null, null);
    }

    /**
     * Callback method invoked when the object with the given key
     * is (re-)loaded into the underlying cache implementation.
     * This is assumed to be the result of an autonomous/internal
     * call within the underlying cache implementation, as opposed
     * to a call to <code>put(key, value)</code>.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got loaded into the cache.
     * @param value The Object that was loaded.
     */
    @Override
    public void doOnAutonomousLoad(String key, T value) {
        addEvent("autonomousLoad", key, value);
    }

    /**
     * Callback method invoked when the object with the given
     * key is evicted from the underlying cache implementation.
     * This is assumed to be the result of an autonomous/internal
     * call within the underlying cache implementation, as opposed
     * to a call to <code>remove(key)</code> from a server
     * implementation.
     * <p/>
     * <strong>Note!</strong>. The key and value must not be modified
     * within this callback method.
     *
     * @param key   The key of the object which got evicted from the cache.
     * @param value The object that was evicted.
     */
    @Override
    public void doOnAutonomousEvict(String key, T value) {
        addEvent("autonomousEvict", key, value);
    }

    /**
     * Externalizable write template delegation method, invoked from
     * within writeExternal. You should write the internal state of
     * the Listener onto the ObjectOutput, adhering to the following
     * pattern:
     * <p/>
     * <pre>
     * <code>
     * class SomeCacheListener extends CacheListenerAdapter
     * {
     *      // Internal state
     *      private int meaningOfLife = 42;
     *      private String name = "Lennart";
     *      ...
     *
     *      @Override
     *      protected void performWriteExternal(ObjectOutput out) throws IOException
     *      {
     *          // Write your own state
     *          out.writeInt(meaningOfLife);
     *          out.writeUTF(name);
     *      }
     *
     *      @Override
     *      protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException
     *      {
     *          // Then write your own state.
     *          // Use the same order as you wrote the properties in writeExternal.
     *          meaningOfLife = in.readInt();
     *          name = in.readUTF();
     *      }
     * }
     * </code>
     * </pre>
     *
     * @param out
     * @throws java.io.IOException
     */
    @Override
    protected void performWriteExternal(ObjectOutput out) throws IOException {

        out.writeInt(index.get());

        // Write the eventId2EventInfoMap
        out.writeInt(eventId2EventInfoMap.size());
        for (Map.Entry<Integer, DebugCacheListener<T>.EventInfo> current : eventId2EventInfoMap.entrySet()) {
            out.writeInt(current.getKey());
            current.getValue().writeExternal(out);
        }
    }

    /**
     * Externalizable read template delegation method, invoked from
     * within writeExternal. You should read the internal state of
     * the Listener from the ObjectInput, adhering to the following
     * pattern:
     * <p/>
     * <pre>
     * <code>
     * class SomeCacheListener extends CacheListenerAdapter
     * {
     *      // Internal state
     *      private int meaningOfLife = 42;
     *      private String name = "Lennart";
     *      ...
     *
     *      @Override
     *      protected void performWriteExternal(ObjectOutput out) throws IOException
     *      {
     *          // Write your own state
     *          out.writeInt(meaningOfLife);
     *          out.writeUTF(name);
     *      }
     *
     *      @Override
     *      protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException
     *      {
     *          // Write your own state.
     *          // Use the same order as you wrote the properties in writeExternal.
     *          meaningOfLife = in.readInt();
     *          name = in.readUTF();
     *      }
     * }
     * </code>
     * </pre>
     *
     * @param in the stream to read data from in order to restore the object
     * @throws java.io.IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    protected void performReadExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        index = new AtomicInteger(in.readInt());
        final int size = in.readInt();

        // Check sanity
        if (eventId2EventInfoMap == null) {
            eventId2EventInfoMap = new TreeMap<Integer, DebugCacheListener<T>.EventInfo>();
        }
        for (int i = 0; i < size; i++) {
            Integer currentKey = in.readInt();
            final DebugCacheListener<T> outer = new DebugCacheListener<T>();
            final DebugCacheListener<T>.EventInfo currentValue = outer.new EventInfo(null, null, null, null);
            currentValue.readExternal(in);

            // ... and re-create the entry.
            eventId2EventInfoMap.put(currentKey, currentValue);
        }
    }

    public SortedMap<String, SortedMap<Integer, DebugCacheListener<T>.EventInfo>>
    getThreadName2SortedEventInfoMap() {

        final SortedMap<String, SortedMap<Integer, DebugCacheListener<T>.EventInfo>> toReturn
                = new TreeMap<String, SortedMap<Integer, DebugCacheListener<T>.EventInfo>>();

        for (Map.Entry<Integer, DebugCacheListener<T>.EventInfo> current : eventId2EventInfoMap.entrySet()) {
            DebugCacheListener<T>.EventInfo info = current.getValue();

            SortedMap<Integer, DebugCacheListener<T>.EventInfo> innerMap = toReturn.get(info.threadName);
            if (innerMap == null) {
                innerMap = new TreeMap<Integer, DebugCacheListener<T>.EventInfo>();
                toReturn.put(info.threadName, innerMap);
            }

            innerMap.put(current.getKey(), info);
        }

        return toReturn;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder("\n\n\n######################################\n");
        for (Map.Entry<String, SortedMap<Integer, DebugCacheListener<T>.EventInfo>> current
                : getThreadName2SortedEventInfoMap().entrySet()) {

            final String threadName = current.getKey();
            for (Map.Entry<Integer, DebugCacheListener<T>.EventInfo> currentMessageTuple
                    : current.getValue().entrySet()) {
                builder.append("(" + threadName + ") [" + currentMessageTuple.getKey() + "]: "
                        + currentMessageTuple.getValue() + "\n");
            }
        }
        return builder.toString() + "######################################\n\n\n";
    }

    //
    // private helpers
    //

    private synchronized void addEvent(String event, String key, Object value) {
        synchronized (lock) {

            final DebugCacheListener<T> outer = new DebugCacheListener<T>();
            eventId2EventInfoMap.put(
                    index.getAndIncrement(),
                    outer.new EventInfo(event, key, Thread.currentThread().getName(), value));
        }
    }
}
