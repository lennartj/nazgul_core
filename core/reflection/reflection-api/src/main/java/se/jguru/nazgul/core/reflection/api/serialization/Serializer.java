/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
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

package se.jguru.nazgul.core.reflection.api.serialization;

import org.apache.commons.lang3.Validate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Trivial implementations of Java serialization and de-serialization into
 * ASCII-armoured byte[] instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public abstract class Serializer {

    // Internal state
    private static final int BASELENGTH = 128;
    private static final int LOOKUPLENGTH = 16;
    private static final byte[] HEX_NUMBER_TABLE = new byte[BASELENGTH];
    private static final char[] LOOK_UP_HEX_ALPHABET = new char[LOOKUPLENGTH];
    private static final String ERROR_PREFIX = "Incorrect ASCII-armoured instance: ";

    static {

        final int numDigits = 10;

        // Setup the Hex number table
        for (int i = 0; i < BASELENGTH; i++) {
            HEX_NUMBER_TABLE[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            HEX_NUMBER_TABLE[i] = (byte) (i - '0');
        }
        for (int i = 'F'; i >= 'A'; i--) {
            HEX_NUMBER_TABLE[i] = (byte) (i - 'A' + numDigits);
        }
        for (int i = 'f'; i >= 'a'; i--) {
            HEX_NUMBER_TABLE[i] = (byte) (i - 'a' + numDigits);
        }

        // Setup the Hex alphabet.
        for (int i = 0; i < numDigits; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('0' + i);
        }
        for (int i = numDigits; i <= 15; i++) {
            LOOK_UP_HEX_ALPHABET[i] = (char) ('A' + i - numDigits);
        }
    }

    /**
     * Serializes the provided object to an ASCII-armoured byte array.
     *
     * @param toSerialize The object to serialize and wrap in an ASCII-armoured String.
     * @return The ASCII-armoured byte array from the provided {@code toSerialize} object,
     *         or {@code null} if given a null argument.
     */
    public static String serialize(final Serializable toSerialize) {

        // Check sanity
        String toReturn = null;
        if (toSerialize != null) {

            try {

                // Serialize the object to a byte array
                ByteArrayOutputStream bits = new ByteArrayOutputStream();

                ObjectOutputStream oos = new ObjectOutputStream(bits);
                oos.writeObject(toSerialize);
                oos.flush();

                // Wrap the byte array in ASCII armour.
                toReturn = encode(bits.toByteArray());

            } catch (Exception e) {
                throw new IllegalArgumentException("Could not serialize object of type ["
                        + toSerialize.getClass().getName() + "]", e);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * De-serializes the provided ASCII-armoured byte array to a Java Object.
     *
     * @param serializedInstance an ASCII-armoured byte array being a serialized Java Object.
     * @return The de-serialized Java Object.
     */
    public static Object deSerialize(final String serializedInstance) {

        // Check sanity
        Object toReturn = null;
        if (serializedInstance != null) {

            try {
                ByteArrayInputStream decoded = new ByteArrayInputStream(decode(serializedInstance));

                ObjectInputStream ois = new ObjectInputStream(decoded);
                toReturn = ois.readObject();
                ois.close();

            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not deserialize object.", e);
            }
        }

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    /**
     * Decodes the provided ASCII-armoured String into a byte array.
     *
     * @param encoded encoded string
     * @return return array of byte to encode
     */
    private static byte[] decode(final String encoded) throws IllegalArgumentException {

        // Check sanity
        final int lengthData = encoded.length();
        if (lengthData % 2 != 0) {
            throw new IllegalArgumentException(ERROR_PREFIX + "Must be an even number of bytes.");
        }

        final int returnSize = lengthData / 2;
        final byte[] toReturn = new byte[returnSize];
        final char[] binaryData = encoded.toCharArray();

        char char1, char2;
        for (int i = 0; i < returnSize; i++) {

            // Read the bytes in order
            char1 = binaryData[i * 2];
            char2 = binaryData[i * 2 + 1];

            // Check sanity
            Validate.isTrue(char1 < BASELENGTH, ERROR_PREFIX
                    + "Each byte must be < " + BASELENGTH + " [Got: " + char1 + "].");
            Validate.isTrue(char2 < BASELENGTH, ERROR_PREFIX
                    + "Each byte must be < " + BASELENGTH + " [Got: " + char2 + "].");

            // Restore the byte.
            toReturn[i] = (byte) ((HEX_NUMBER_TABLE[char1] << 4) | HEX_NUMBER_TABLE[char2]);
        }

        // All done.
        return toReturn;
    }

    /**
     * Encodes the provided byte array to an ASCII-armoured String.
     *
     * @param toEncode array of byte to encode
     * @return the ASCII encoded String
     */
    private static String encode(final byte[] toEncode) {

        // toEncode should not be null
        final int returnSize = toEncode.length * 2;

        char[] toReturn = new char[returnSize];
        int current;
        for (int i = 0; i < toEncode.length; i++) {

            // Get the data, and compensate for the signed nature of Java's bytes.
            current = toEncode[i];
            if (current < 0) {
                current += 256;
            }

            // The bytes are assumed to be sent in LSB order.
            toReturn[i * 2] = LOOK_UP_HEX_ALPHABET[current >> 4];
            toReturn[i * 2 + 1] = LOOK_UP_HEX_ALPHABET[current & 0xf];
        }

        // All done.
        return new String(toReturn);
    }
}
