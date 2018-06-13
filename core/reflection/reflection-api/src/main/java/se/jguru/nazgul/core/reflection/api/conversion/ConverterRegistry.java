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

package se.jguru.nazgul.core.reflection.api.conversion;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Type converter registry specification, used as a generic type conversion service.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ConverterRegistry {

    /**
     * Adds the provided Converters to this ConverterRegistry.
     *
     * @param converters A List of objects, annotated with @Converter. The priority provided in the annotation
     *                   of each supplied converter will be used when setting up the internal state and the
     *                   converter chain.
     * @throws IllegalArgumentException if any of the converters was not annotated with @Converter, or if the
     *                                  methods/constructors annotated with @Converter did not comply with a converter
     *                                  specification.
     * @see se.jguru.nazgul.core.reflection.api.conversion.Converter
     */
    void add(@NotNull @Size(min = 1) Object... converters) throws IllegalArgumentException;

    /**
     * Removes the supplied converter instance from this ConverterRegistry.
     *
     * @param converter The converter to remove.
     */
    void remove(@NotNull Object converter);

    /**
     * Converts the provided source object to the desired type.
     *
     * @param source      The object to convert.
     * @param desiredType The type to which the source object should be converted.
     * @param <To>        The resulting type.
     * @param <From>      The source type.
     * @return The converted object.
     * @throws IllegalArgumentException if the conversion failed.
     */
    <From, To> To convert(@NotNull From source, @NotNull Class<To> desiredType) throws IllegalArgumentException;

    /**
     * Retrieves the available targetTypes for the supplied sourceType, implying the closure of Classes
     * to which the supplied sourceType can be converted by this ConverterRegistry.
     *
     * @param sourceType The source type.
     * @param <From>     The source type.
     * @return the available targetTypes for the supplied sourceType, implying the closure of Classes
     * to which the supplied sourceType can be converted by this ConverterRegistry, or
     * {@code null} in case this TypeConverterRegistry could not convert the supplied sourceType.
     * @throws IllegalArgumentException if the calculation could not be performed.
     */
    <From> Set<Class<?>> getPossibleConversions(@NotNull Class<From> sourceType) throws IllegalArgumentException;
}
