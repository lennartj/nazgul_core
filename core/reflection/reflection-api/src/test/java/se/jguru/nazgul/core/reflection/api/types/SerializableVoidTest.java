/*-
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

package se.jguru.nazgul.core.reflection.api.types;

import org.junit.Assert;
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
