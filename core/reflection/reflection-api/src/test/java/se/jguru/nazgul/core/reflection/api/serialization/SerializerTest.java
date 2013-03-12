/*
 * #%L
 *   se.jguru.nazgul.core.poms.core-parent.nazgul-core-parent
 *   %%
 *   Copyright (C) 2010 - 2013 jGuru Europe AB
 *   %%
 *   Licensed under the jGuru Europe AB license (the "License"), based
 *   on Apache License, Version 2.0; you may not use this file except
 *   in compliance with the License.
 *
 *   You may obtain a copy of the License at
 *
 *         http://www.jguru.se/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   #L%
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
