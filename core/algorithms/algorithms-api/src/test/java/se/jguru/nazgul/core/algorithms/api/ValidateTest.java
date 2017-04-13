/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
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
package se.jguru.nazgul.core.algorithms.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class ValidateTest {

    @Test
    public void validateErrorMessageOnSuppliedArgumentName() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle empty 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notEmpty("", argumentName);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgumentName() {

        // Act & Assert
        try {
            Validate.notEmpty("", null);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("Cannot handle empty argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgument() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle null 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notNull(null, argumentName);
        } catch (NullPointerException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateCollectionEmptyness() {

        // Assemble
        final List<String> aCollection = new ArrayList<>();

        // Act & Assert
        Validate.notEmpty(aCollection, "aCollection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateMapEmptyness() {

        // Assemble
        final SortedMap<String, Double> anEmptySortedMap = new TreeMap<>();

        // Act & Assert
        Validate.notEmpty(anEmptySortedMap, "anEmptySortedMap");
    }

    @Test
    public void validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull(null, null);
        } catch (NullPointerException expected) {
            Assert.assertEquals("Cannot handle null argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateReturningSameInstance() {

        // Assemble
        final StringBuilder toValidate = new StringBuilder();

        // Act
        final StringBuilder result = Validate.notNull(toValidate, "toValidate");

        // Assert
        Assert.assertSame(toValidate, result);
    }
}
