/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TestAbstractSwiftClusterable extends AbstractSwiftClusterable {

    // Internal state
    public List<String> callTrace = new ArrayList<String>();
    private String name;
    private int age;

    /**
     * Creates a new AbstractIdentifiable and assigns the internal ID state.
     *
     * @param id The identifier of this CacheListenerAdapter.
     */
    public TestAbstractSwiftClusterable(String id, String name, int age) {
        super(id);
        this.name = name;
        this.age = age;
    }

    /**
     * Serialization-usable constructor, and not part of the public API of this
     * AbstractIdentifiable. <strong>This is for framework use only</strong>.
     */
    public TestAbstractSwiftClusterable() {
        super((IdGenerator) null);
    }

    /**
     * {@inheritDoc}
     */
    public TestAbstractSwiftClusterable(IdGenerator idGenerator, String name, int age) {
        super(idGenerator);
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
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
     * @param out The ObjectOutput to write to.
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
}
