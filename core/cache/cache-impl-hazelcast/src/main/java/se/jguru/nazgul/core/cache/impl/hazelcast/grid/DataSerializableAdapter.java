/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.cache.impl.hazelcast.grid;

import com.hazelcast.nio.DataSerializable;
import org.apache.commons.lang3.Validate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * Transport wrapper optimizer for Externalizable instances inside Hazelcast.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DataSerializableAdapter implements Serializable, DataSerializable {

    // Internal state
    private Externalizable original;
    private String className;

    /**
     * Creates a Hazelcast DataSerializable, wrapping the provided Externalizable.
     *
     * @param externalizable The Externalizable to wrap.
     */
    public DataSerializableAdapter(final Externalizable externalizable) {
        Validate.notNull(externalizable, "Cannot handle null Externalizable argument.");
        this.original = externalizable;
        className = externalizable.getClass().getName();
    }

    /**
     * Constructor reserved for framework use.
     */
    DataSerializableAdapter() {
    }

    /**
     * @return The originally wrapped Externalizable.
     */
    public Externalizable getWrappedExternalizable() {
        return original;
    }

    /**
     * Writes the contained Externalizable onto the provided DataOutput,
     * or dies throwing an IOException if it failed.
     *
     * @param out The DataOutput to write to.
     * @throws java.io.IOException if the out parameter was neither an ObjectOutput
     *                             or an OutputStream of some sort.
     */
    @Override
    public void writeData(final DataOutput out) throws IOException {

        // Convert the DataOutput to an ObjectOutput.
        ObjectOutput objectOutput = null;
        if (out instanceof ObjectOutput) {
            objectOutput = (ObjectOutput) out;
        } else if (out instanceof OutputStream) {
            objectOutput = new ObjectOutputStream((OutputStream) out);
        } else {
            throw new IOException("Received unmanageable DataOutput of type ["
                    + out.getClass().getName() + "]");
        }

        // Write the class of the Externalizable.
        objectOutput.writeUTF(className);

        // Write the Externalizable data itself.
        original.writeExternal(objectOutput);

        // Flush the object output
        objectOutput.flush();
    }

    /**
     * Reads the provided DataInput into an Externalizable.
     *
     * @param in The DataInput to read from.
     * @throws java.io.IOException if the original Externalizable could not be read properly.
     */
    @Override
    public void readData(final DataInput in) throws IOException {

        // Convert the DataInput to ObjectInput
        ObjectInput objectInput = null;
        if (in instanceof ObjectInput) {
            objectInput = (ObjectInput) in;
        } else if (in instanceof InputStream) {
            objectInput = new ObjectInputStream((InputStream) in);
        } else {
            throw new IOException("Received unmanageable DataInput of type ["
                    + in.getClass().getName() + "]");
        }


        try {
            // Find the className of the Externalizable
            original = (Externalizable) newInstance(objectInput.readUTF());

            // Restore the Externalizable data.
            original.readExternal(objectInput);

        } catch (Exception e) {
            throw new IOException("Could not find Externalizable class.", e);
        }
    }

    //
    // Private helpers
    //

    private static Object newInstance(final String className) throws Exception {

        // Convert to a class.
        Class<?> externalizableClass = Class.forName(className);

        // Create a new instance.
        final Constructor<?> constructor = externalizableClass.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }

        // We should have a default constructor.
        return constructor.newInstance();
    }
}
