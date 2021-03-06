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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation defining a method or constructor accepting a single argument with a given
 * (source) type, and returning a non-void object (called target type). The two typical
 * examples of conversion are illustrated below:
 * <pre>
 *     class FooConverter {
 *
 *         &#64;Converter
 *         public String convert(Foo aFoo) {
 *             ...
 *         }
 *
 *         &#64;Converter
 *         public Foo convert(String aString) {
 *             ...
 *         }
 *     }
 * </pre>
 * If constructor conversion is desired, the typical pattern becomes:
 * <pre>
 *     class Foo {
 *
 *         &#64;Converter
 *         public Foo(String aString) {
 *             ...
 *         }
 *     }
 * </pre>
 * Examples for arguments to the Converter annotation are found within their respective JavaDoc.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Converter {

    /**
     * The default priority value, used if no other priority has been supplied.
     */
    int DEFAULT_PRIORITY = 100;

    /**
     * The default converterMethod value, used to indicate that no converter method name has been supplied.
     */
    String NO_CONVERTER_METHOD = "##NONE##";

    /**
     * Parameter to indicate that this converter method or constructor accepts {@code null} values.
     * Defaults to {@code false}.
     * If this parameter is set to {@code true}, your converter [method or constructor] indicates that it
     * should be able to provide a default value in the case of null input values. A typical example is
     * provided below:
     * <pre>
     *
     *     &#64;Converter(acceptsNullValues = true)
     *     public StringBuffer convert(final String aString) {
     *          final String bufferValue = aString == null ? "nothing!" : aString;
     *          return new StringBuffer(bufferValue);
     *     }
     * </pre>
     *
     * @return {@code true} if this Converter accepts {@code null}s as values for conversion.
     */
    boolean acceptsNullValues() default false;

    /**
     * <p>Parameter to indicate which priority this converter method or constructor should have.
     * A lower (but positive, until minimum 0) priority implies that this converter will be
     * attempted <strong>before</strong> a converter with higher priority value.
     * In that sense, the priority should be regarded as the execution index of several converters.</p>
     * <p>A typical example for defining a would be:</p>
     * <pre>
     *     class AnotherFooConverter {
     *
     *         &#64;Converter(priority = 200)
     *         public String convert(Foo aFoo) {
     *             ...
     *         }
     *
     *         public boolean checkFoo(Foo aFoo) {
     *             // Find out if the aFoo can be converted by this FooConverter instance.
     *         }
     *     }
     * </pre>
     *
     * @return the priority of this Converter method or constructor. Members with lower priorities are
     * attempted <strong>before</strong> higher priority ones.
     */
    int priority() default DEFAULT_PRIORITY;

    /**
     * <p>Name of a method with a single parameter of the same source type as the {@code @Converter}-annotated
     * method, and returning a {@code boolean}.
     * If present, this conditionalConversionMethod value supplies the name of a method [within the
     * same class as this Converter] which should be invoked to find out if the supplied source object
     * can be converted by this method.</p>
     * <p>This attribute is ignored for Constructor Converters. A typical example would be:</p>
     * <pre>
     *     class FooConverter {
     *
     *         &#64;Converter(conditionalConversionMethod = "checkFoo")
     *         public String convert(Foo aFoo) {
     *             ...
     *         }
     *
     *         public boolean checkFoo(Foo aFoo) {
     *             // Find out if the aFoo can be converted by this FooConverter instance.
     *         }
     *     }
     * </pre>
     *
     * @return Name of a method with a single parameter of the same source type as the {@code @Converter}-annotated
     * method, and returning a {@code boolean}.
     */
    String conditionalConversionMethod() default NO_CONVERTER_METHOD;
}
