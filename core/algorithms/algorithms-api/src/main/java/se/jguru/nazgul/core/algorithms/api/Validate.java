/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.Map;

/**
 * Simple argument validator, inspired by the commons-lang.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
@XmlTransient
public final class Validate {

    /**
     * Hide constructor for utility classes.
     */
    private Validate() {
        // Do nothing.
    }

    /**
     * Validates that the supplied object is not null, and throws a NullPointerException otherwise.
     *
     * @param object       The object to validate for {@code null}-ness.
     * @param argumentName The argument name of the object to validate. If supplied (i.e. non-{@code null}),
     *                     this value is used in composing a better exception message.
     * @return The supplied object - if it is was not null.
     * @throws NullPointerException if the supplied object was null.
     */
    public static <T> T notNull(final T object, final String argumentName) {

        // Check sanity
        if (object == null) {
            throw new NullPointerException(getMessage("null", argumentName));
        }

        // All done.
        return object;
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aMap         The Map to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     * @param <K>          the type of keys found within the Map.
     * @param <V>          the type of values found within the Map.
     * @param <M>          the exact type of Map submitted for emptyness check.
     * @return The Collection submitted.
     * @throws IllegalArgumentException if the submitted Map is empty.
     * @throws NullPointerException     if the supplied Map is null.
     */
    public static <K, V, M extends Map<K, V>> M notEmpty(final M aMap, final String argumentName) {

        // Check sanity
        notNull(aMap, argumentName);

        if (aMap.isEmpty()) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }

        // All done
        return aMap;
    }

    /**
     * Validates that the supplied object is not null, and throws a NullPointerException otherwise.
     * If the supplied collection is empty, then an IllegalArgumentException is thrown.
     *
     * @param aCollection  The Collection to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     * @param <C>          The type of Collection to validate for emptyness.
     * @param <T>          The type elements within the Collection.
     * @return The Collection submitted.
     * @throws IllegalArgumentException if the submitted Collection is empty.
     * @throws NullPointerException     if the supplied Collection is null.
     */
    public static <T, C extends Collection<T>> C notEmpty(final C aCollection, final String argumentName) {

        // Check sanity
        notNull(aCollection, argumentName);

        if (aCollection.isEmpty()) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }

        // All done
        return aCollection;
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aString      The string to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     * @return The non-empty String submitted.
     * @throws IllegalArgumentException if the submitted {@code aString} is empty.
     * @throws NullPointerException     if the supplied {@code aString} is null.
     */
    public static String notEmpty(final String aString, final String argumentName) {

        // Check sanity
        notNull(aString, argumentName);

        if (aString.length() == 0) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }

        // All done
        return aString;
    }

    /**
     * Validates that the supplied array is not null, and throws a NullPointerException otherwise.
     * If the supplied array is empty, then an IllegalArgumentException is thrown.
     *
     * @param anArray      The Array to validate for empty-ness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     * @param <T>          The type elements within the Array.
     * @return The Array submitted.
     * @throws IllegalArgumentException if the submitted Array is empty.
     * @throws NullPointerException     if the supplied Array is null.
     */
    public static <T> T[] notEmpty(final T[] anArray, final String argumentName) {

        // Check sanity
        notNull(anArray, argumentName);

        if (anArray.length == 0) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }

        // All Done
        return anArray;
    }

    /**
     * Validates that the supplied condition is true, and throws an IllegalArgumentException otherwise.
     *
     * @param condition The condition to validate for truth.
     * @param message   The exception message used within the IllegalArgumentException if the condition is false.
     */
    public static void isTrue(final boolean condition, final String message) {

        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    //
    // Private helpers
    //

    @NotNull
    private static String getMessage(@NotNull final String exceptionDefinition,
                                     @NotNull final String argumentName) {
        return "Cannot handle "
                + exceptionDefinition
                + (argumentName == null ? "" : " '" + argumentName + "'")
                + " argument.";
    }
}
