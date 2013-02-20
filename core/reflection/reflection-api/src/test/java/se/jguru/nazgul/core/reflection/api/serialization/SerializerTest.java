/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.reflection.api.serialization;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class SerializerTest {

    @Test
    public void validateNullReturnedOnNullObjectsSupplied() {

        // Act & Assert
        Assert.assertNull(Serializer.serialize(null));
        Assert.assertNull(Serializer.deSerialize(null));
    }

    @Test
    public void validateProperSerialization() {

        // Assemble
        final String expected = "ACED0005737200176A6176612E6C616E672E537472696E674275696C6465"
                + "723CD5FB145A4C6ACB0300007870770400000006757200025B43B02666B0"
                + "E25D84AC0200007870000000160046006F006F0042006100720000000000"
                + "00000000000000000000000000000000000000000000000000000078";
        final StringBuilder builder = new StringBuilder("FooBar");

        // Act
        final String serialized = Serializer.serialize(builder);

        // Assert
        Assert.assertNotNull(serialized);
        Assert.assertEquals(expected, serialized);
    }

    @Test
    public void validateProperDeserialization() {

        // Assemble
        final String serialized = "ACED0005737200176A6176612E6C616E672E537472696E674275696C6465"
                + "723CD5FB145A4C6ACB0300007870770400000006757200025B43B02666B0"
                + "E25D84AC0200007870000000160046006F006F0042006100720000000000"
                + "00000000000000000000000000000000000000000000000000000078";
        final String expected = "FooBar";

        // Act
        final Object result = Serializer.deSerialize(serialized);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof StringBuilder);
        Assert.assertEquals(expected, result.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnOddNumberOfChars() {

        // Act & Assert
        Serializer.deSerialize("1");
    }
}
