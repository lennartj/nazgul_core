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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.reflection.api.conversion;

/**
 * Generic type conversion specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface TypeConverter<From, To> {

    /**
     * @return The target type of this TypeConverter.
     */
    Class<To> getToType();

    /**
     * @return The source type of this TypeConverter.
     */
    Class<From> getFromType();

    /**
     * Validates if this TypeConverter is able to convert the provided instance.
     *
     * @param instance The instance which should be validated for conversion.
     * @return {@code true} if this JaxbTransportTypeConverter can
     *         package the provided instance for transport and {@code false} otherwise.
     */
    boolean canConvert(From instance);

    /**
     * Converts the provided instance to the resulting type.
     *
     * @param instance The instance to convert.
     * @return The converted instance of type {@code To}.
     */
    To convert(From instance);
}
