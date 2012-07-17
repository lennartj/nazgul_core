/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.types;

import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SerializableVoidTest {

    @Test
    public void validateSerializableNatureOfSerializableVoid() throws IOException {

        // Assemble
        final SerializableVoid unitUnderTest = getInstance();
        final ByteArrayOutputStream underlyingStream = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(underlyingStream);

        // Act
        out.writeObject(unitUnderTest);
        final byte[] result = underlyingStream.toByteArray();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(new String(result).contains(SerializableVoid.class.getName()));
    }

    //
    // Private helpers
    //

    private SerializableVoid getInstance() {

        try {
            Constructor<SerializableVoid> constructor = SerializableVoid.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed creating a SerializableVoid ... ", e);
        }
    }
}
