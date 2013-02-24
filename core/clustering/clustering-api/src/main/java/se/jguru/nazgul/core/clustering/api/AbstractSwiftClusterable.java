/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.clustering.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract clusterable implementation integrating Externalizable support, to
 * enable quicker or custom (de-)serialization.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSwiftClusterable extends AbstractClusterable implements Externalizable {

    /**
     * {@inheritDoc}
     */
    protected AbstractSwiftClusterable(final IdGenerator idGenerator) {
        super(idGenerator);
    }

    /**
     * {@inheritDoc}
     */
    protected AbstractSwiftClusterable(final String clusterUniqueID) {
        super(clusterUniqueID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {

        // Start with the ID.
        out.writeUTF(getClusterId());

        // Delegate the rest.
        performWriteExternal(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        // Read the ID.
        this.id = in.readUTF();

        // Delegate the rest
        performReadExternal(in);
    }

    /**
     * Externalizable write template delegation method, invoked from within writeExternal. You should write the internal
     * state of the Listener onto the ObjectOutput, adhering to the following pattern:
     * <p/>
     * <p/>
     * <pre>
     * <code>
     * class SomeIdentifiable extends AbstractSwiftIdentifiable
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
     * @param out the stream to write the object to
     * @throws java.io.IOException Includes any I/O exception that may occur
     */
    protected abstract void performWriteExternal(final ObjectOutput out) throws IOException;

    /**
     * Externalizable read template delegation method, invoked from within writeExternal. You should read the internal
     * state of the Listener from the ObjectInput, adhering to the following pattern:
     * <p/>
     * <p/>
     * <pre>
     * <code>
     * class SomeIdentifiable extends AbstractSwiftIdentifiable
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
     * @throws ClassNotFoundException If the class for an object being restored cannot be found.
     */
    protected abstract void performReadExternal(final ObjectInput in) throws IOException, ClassNotFoundException;
}
