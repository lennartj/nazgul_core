/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.TypeExtractor;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filter utility implementations for use in discovering Converter implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see Converter
 */
@SuppressWarnings("all")
public final class Converters {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(Converters.class);

    /**
     * <p>Predicate finding Methods with the following properties:</p>
     * <ol>
     * <li>Annotated with @Converter</li>
     * <li>Being public</li>
     * <li>Not returning {@code void} or {@code Void}</li>
     * <li>Having exactly one argument</li>
     * </ol>
     */
    @SuppressWarnings("all")
    public static final Predicate<Method> CONVERSION_METHOD_FILTER = candidate -> {

        // #1) Is the candidate Method annotated with @Converter?
        final Converter converter = candidate.getAnnotation(Converter.class);
        if (converter == null) {

            if (log.isDebugEnabled()) {
                log.debug("Method [" + candidate + "] is not annotated with @Converter.");
            }

            // All Done.
            return false;
        }

        // #2) Is the candidate Method public?
        final boolean isPublic = Modifier.isPublic(candidate.getModifiers());
        if (!isPublic) {

            if (log.isDebugEnabled()) {
                log.debug("Method [" + candidate + "] is not public.");
            }

            // All Done.
            return false;
        }

        // #3) Does the method return Void or void?
        final Class<?> returnType = candidate.getReturnType();
        final boolean incorrectReturnType = returnType == Void.TYPE || returnType == Void.class;
        if (incorrectReturnType) {

            if (log.isDebugEnabled()) {
                log.debug("Method [" + candidate + "] returns void or Void.");
            }

            // All Done.
            return false;
        }

        // #4) Not a single parameter?
        final Class<?>[] parameterTypes = candidate.getParameterTypes();
        if (parameterTypes.length != 1) {

            if (log.isDebugEnabled()) {
                log.debug("Method [" + candidate + "] does not have a single parameter.");
            }

            // All Done.
            return false;
        }

        // #5) If we have a conditionalConverterMethod defined, it must be on the form
        //
        // public boolean someMethodName(final FromType aFromType);
        //
        final String conditionalConverterMethod = converter.conditionalConversionMethod();
        final boolean hasConditionalConverterMethod = !conditionalConverterMethod.equals(Converter.NO_CONVERTER_METHOD);

        if (hasConditionalConverterMethod) {

            // Create the
            final Class<?> fromType = parameterTypes[0];
            final ConditionalConverterMethodPredicate filter = new ConditionalConverterMethodPredicate(
                    fromType,
                    converter.conditionalConversionMethod());

            // Acquire the potential conversionMethods
            final SortedSet<Method> conversionMethods = TypeExtractor.getMethods(
                    candidate.getDeclaringClass(),
                    filter);

            // All done.
            return conversionMethods.size() > 0;
        }

        // No problems found.
        return true;
    };

    /**
     * Singleton instance of the ConversionConstructorFilter type.
     */
    public static final Predicate<Constructor<?>> CONVERSION_CONSTRUCTOR_FILTER = candidate -> {

        final Class<?>[] parameterTypes = candidate.getParameterTypes();
        if (candidate.getAnnotation(Converter.class) != null && parameterTypes.length == 1) {

            // The only argument should not be Void (which would be funky...).
            return (parameterTypes[0] != Void.class && parameterTypes[0] != Void.TYPE);
        }

        // Nopes
        return false;
    };

    /**
     * Function which extracts all methods and constructors able to convert types within a class.
     * Should a null class be passed as argument, an empty Function.
     */
    @NotNull
    public static Function<Class<?>, Tuple<SortedSet<Method>, SortedSet<Constructor<?>>>> GET_CONVERTERS = aClass -> {

        // Fail fast
        if (aClass == null) {
            return new Tuple<>(new TreeSet<>(), new TreeSet<>());
        }

        final SortedSet<Method> converterMethods = TypeExtractor.getMethods(aClass,
                Converters.CONVERSION_METHOD_FILTER);
        final SortedSet<Constructor<?>> converterConstructors = TypeExtractor.getConstructors(aClass,
                Converters.CONVERSION_CONSTRUCTOR_FILTER);

        // All Done.
        return new Tuple<>(converterMethods, converterConstructors);
    };

    /*
     * Helper method to extract a Tuple of converter methods and constructors, as found within the supplied
     * object instance.
     *
     * @param object The object which should be inspected for
     * @return {@code null} if the object was {@code null} or no converter methods and constructors
     * were found within the supplied object. Otherwise returns a populated Tuple.

    public static Tuple<List<Method>, List<Constructor<?>>> getConverterMethodsAndConstructors(
            final Object object) {

        // Create the return variable
        Tuple<List<Method>, List<Constructor<?>>> toReturn = null;

        if (object != null) {

            // Find any converter methods in the supplied converter
            final SortedSet<Method> methods = TypeExtractor.getMethods(
                    object.getClass(), ReflectiveConverterFilter.CONVERSION_METHOD_FILTER);

            // Find any converter constructors in the supplied converter
            final List<Constructor<?>> constructors = CollectionAlgorithms.filter(
                    Arrays.asList(object.getClass().getConstructors()),
                    ReflectiveConverterFilter.CONVERSION_CONSTRUCTOR_FILTER);

            // Only return a non-null value if we found some converter methods/constructors.
            if (methods.size() != 0 || constructors.size() != 0) {
                toReturn = new Tuple<List<Method>, List<Constructor<?>>>(methods, constructors);
            }
        }

        // All done.
        return toReturn;
    }
    */

    /**
     * Hidden constructor.
     */
    private Converters() {
    }

    /**
     * <p>Filter implementation detecting proper conditional converter methods. Converter methods must:</p>
     * <ol>
     * <li>Be public</li>
     * <li>Return boolean</li>
     * <li>Have a given method name</li>
     * <li>Have a single argument with a given type</li>
     * </ol>
     */
    @SuppressWarnings("all")
    private static final class ConditionalConverterMethodPredicate implements Predicate<Method> {

        // Internal state
        private Class<?> requiredArgumentType;
        private String methodName;

        private ConditionalConverterMethodPredicate(final Class<?> fromType, final String methodName) {
            this.requiredArgumentType = fromType;
            this.methodName = methodName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean test(final Method candidate) {

            // #1) Is the method public?
            if (!Modifier.isPublic(candidate.getModifiers())) {
                return false;
            }

            // #2) Does the method return boolean or Boolean?
            final Class<?> returnType = candidate.getReturnType();
            if (!(returnType == Boolean.TYPE || returnType == Boolean.class)) {
                return false;
            }

            // #3) Does the method have 1 single argument of the correct type?
            final Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes.length != 1 || !parameterTypes[0].isAssignableFrom(requiredArgumentType)) {
                return false;
            }

            // #4) Does the method name match the supplied requirement?
            //     ... which must not be the default value...
            if (methodName.equals(Converter.NO_CONVERTER_METHOD) || !candidate.getName().equals(methodName)) {
                return false;
            }

            // All done.
            return true;
        }
    }
}
