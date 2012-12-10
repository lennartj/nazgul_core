/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
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
 * <p/>
 * <pre>
 *     class FooConverter {
 *
 *         @Converter
 *         public String convert(Foo aFoo) {
 *             ...
 *         }
 *
 *         @Converter
 *         public Foo convert(String aString) {
 *             ...
 *         }
 *     }
 * </pre>
 * If constructor conversion is desired, the typical pattern becomes:
 * <pre>
 *     class Foo {
 *
 *         @Converter
 *         public Foo(String aString) {
 *             ...
 *         }
 *     }
 * </pre>
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
    public static final int DEFAULT_PRIORITY = 100;

    /**
     * Parameter to indicate that this converter method or constructor accepts {@code null} values.
     * Defaults to {@code false}.
     */
    boolean acceptsNullValues() default false;

    /**
     * Parameter to indicate which priority this converter method or constructor should have.
     * A lower (but positive, until minimum 0) priority implies that this converter will be
     * attempted before a converter with higher priority.
     */
    int priority() default DEFAULT_PRIORITY;

    /**
     * Name of a method with the same source type as the {@code @Converter}-annotated method,
     * and which must return a {@code boolean}. If present, this conditionalConversionMethod value
     * supplies the name of a method [within the same class as this Converter] which should be
     * invoked to find out if the supplied source object can be converted by this method.
     * <p/>
     * This attribute is ignored for Constructor Converters.
     * <p/>
     * A typical example would be:
     * <pre>
     *     class FooConverter {
     *
     *         @Converter(conditionalConversionMethod = "checkFoo")
     *         public String convert(Foo aFoo) {
     *             ...
     *         }
     *
     *         public boolean checkFoo(Foo aFoo) {
     *             // Find out if the aFoo can be converted by this FooConverter instance.
     *         }
     *     }
     * </pre>
     */
    String conditionalConversionMethod();
}
