/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.cache.impl.hazelcast;

import se.jguru.nazgul.core.cache.api.AbstractCacheListener;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.TreeMap;

/**
 * Debug / Test CacheListenerAdapter implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugCacheListener extends AbstractCacheListener<String> {

    // Internal state
    private int index = 1;
    public TreeMap<Integer, EventInfo> eventId2KeyValueMap = new TreeMap<Integer, EventInfo>();

    public class EventInfo implements Externalizable {

        public EventInfo(final String eventType, final String key, final Object value) {
            this.eventType = eventType;
            this.key = key;
            this.value = value;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeUTF(eventType);
            out.writeUTF(key);
            out.writeObject(value);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            eventType = in.readUTF();
            key = in.readUTF();
            value = in.readObject();
        }

        public String eventType;
        public String key;
        public Object value;

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
    public void doOnPut(String key, Serializable value) {
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
    public void doOnUpdate(String key, Serializable newValue, Serializable oldValue) {
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
    public void doOnRemove(String key, Serializable value) {
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
    public void doOnAutonomousLoad(String key, Serializable value) {
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
    public void doOnAutonomousEvict(String key, Serializable value) {
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

        out.writeInt(index);

        // Write the eventId2KeyValueMap
        out.writeInt(eventId2KeyValueMap.size());
        for (Integer current : eventId2KeyValueMap.keySet()) {
            out.writeInt(current);
            eventId2KeyValueMap.get(current).writeExternal(out);
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

        index = in.readInt();
        final int size = in.readInt();

        // Check sanity
        if (eventId2KeyValueMap == null) {
            eventId2KeyValueMap = new TreeMap<Integer, EventInfo>();
        }
        for (int i = 0; i < size; i++) {
            Integer currentKey = in.readInt();
            EventInfo currentValue = new EventInfo(null, null, null);
            currentValue.readExternal(in);

            // ... and re-create the entry.
            eventId2KeyValueMap.put(currentKey, currentValue);
        }
    }

    //
    // private helpers
    //

    private synchronized void addEvent(String event, String key, Object value) {
        eventId2KeyValueMap.put(index++, new EventInfo(event, key, value));
    }
}
