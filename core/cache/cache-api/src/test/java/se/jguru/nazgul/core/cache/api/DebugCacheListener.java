/*
 * #%L
 * Nazgul Project: nazgul-core-cache-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.cache.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugCacheListener extends AbstractCacheListener<String> {

    // Internal state
    public List<String> callTrace;
    private String name;
    private int age;

    /**
     * Creates a new CacheListenerAdapter and assigns the internal ID state.
     *
     * @param id The identifier of this CacheListenerAdapter.
     */
    public DebugCacheListener(String id, String name, int age) {

        super(id);

        callTrace = new ArrayList<String>();

        this.name = name;
        this.age = age;
    }

    /**
     * Serialization-usable constructor, and not part of the public API of this
     * CacheListenerAdapter. <strong>This is for framework use only</strong>.
     */
    public DebugCacheListener() {

        super();

        callTrace = new ArrayList<String>();
    }

    public String getName() {

        callTrace.add("getName");
        return name;
    }

    public int getAge() {

        callTrace.add("getAge");
        return age;
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

        callTrace.add("performWriteExternal [" + name + ", " + age + "]");

        out.writeUTF(name);
        out.writeInt(age);
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

        name = in.readUTF();
        age = in.readInt();

        callTrace.add("performReadExternal [" + name + ", " + age + "]");
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

        callTrace.add("onPut [" + key + ", " + value + "]");
        super.doOnPut(key, value);
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

        callTrace.add("onUpdate [" + key + ", " + newValue + ", " + oldValue + "]");
        super.doOnUpdate(key, newValue, oldValue);
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

        callTrace.add("onRemove [" + key + ", " + value + "]");
        super.doOnRemove(key, value);
    }

    /**
     * Callback method invoked when the underlying cache
     * is cleared (i.e. its state destroyed and all cached
     * objects evicted).
     */
    @Override
    public void onClear() {

        callTrace.add("onClear");
        super.onClear();
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

        callTrace.add("onAutonomousLoad [" + key + ", " + value + "]");
        super.doOnAutonomousLoad(key, value);
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

        callTrace.add("onAutonomousEvict [" + key + ", " + value + "]");
        super.doOnAutonomousEvict(key, value);
    }
}
