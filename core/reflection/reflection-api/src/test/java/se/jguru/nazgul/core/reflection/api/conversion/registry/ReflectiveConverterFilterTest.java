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

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.FakeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.MultiConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.StringConstructorConverter;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class ReflectiveConverterFilterTest {

    // Shared state
    private Filter<Method> conversionMethodFilter = ReflectiveConverterFilter.CONVERSION_METHOD_FILTER;
    private Filter<Constructor<?>> conversionConstructorFilter =
            ReflectiveConverterFilter.CONVERSION_CONSTRUCTOR_FILTER;

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullMethodArgument() {

        // Act & Assert
        conversionMethodFilter.accept(null);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullConstructorArgument() {

        // Act & Assert
        conversionMethodFilter.accept(null);
    }

    @Test
    public void validateAcceptMethodWithConditionalConverterMethod() throws Exception {

        // Assemble
        final Method convertToDateMethod = getClass().getDeclaredMethod("convertToDate", String.class);

        // Act
        final boolean result = conversionMethodFilter.accept(convertToDateMethod);

        // Assert
        Assert.assertTrue(result);
    }

    @Test
    public void validateAcceptDefaultConverterMethod() throws Exception {

        // Assemble
        final Method stringBufferConverterMethod = getClass().getMethod("stringConverter", String.class);

        // Act
        final boolean result = conversionMethodFilter.accept(stringBufferConverterMethod);

        // Assert
        Assert.assertTrue(result);
    }

    @Test
    public void validateVoidReturnMethodExcluded() throws Exception {

        // Assemble
        final Method voidMethod = getClass().getDeclaredMethod("voidMethod", String.class);

        // Act
        final boolean result = conversionMethodFilter.accept(voidMethod);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateNonAnnotatedMethodExcluded() throws Exception {

        // Assemble
        final Method nonAnnotatedMethod = getClass().getMethod("nonAnnotatedMethod", Boolean.TYPE);

        // Act
        final boolean result = conversionMethodFilter.accept(nonAnnotatedMethod);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateMethodWithTooManyArgumentsExcluded() throws Exception {

        // Assemble
        final Method stringBufferConverterMethod = getClass().getMethod("stringConverter", Boolean.TYPE, String.class);

        // Act
        final boolean result = conversionMethodFilter.accept(stringBufferConverterMethod);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateMethodWithTooFewArgumentsExcluded() throws Exception {

        // Assemble
        final Method stringBufferConverterMethod = getClass().getMethod("tooFewArguments");

        // Act
        final boolean result = conversionMethodFilter.accept(stringBufferConverterMethod);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateMethodWithNonexistentConditionalConversionMethodExcluded() throws Exception {

        // Assemble
        final Method almostAConverterMethod = getClass().getMethod("almostAConverterMethod", String.class);

        // Act
        final boolean result = conversionMethodFilter.accept(almostAConverterMethod);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateAcceptDefaultConverterConstructor() throws Exception {

        // Assemble
        final Constructor<?> okConstructor = StringConstructorConverter.class.getConstructor(String.class);

        // Act
        final boolean result = conversionConstructorFilter.accept(okConstructor);

        // Assert
        Assert.assertTrue(result);
    }

    @Test
    public void validateConstructorWithTooManyArgumentsExcluded() throws Exception {

        // Assemble
        final Constructor<?> incorrectConstructor = StringConstructorConverter.class
                .getConstructor(String.class, Boolean.TYPE);

        // Act
        final boolean result = conversionConstructorFilter.accept(incorrectConstructor);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateNonAnnotatedConstructorExcluded() throws Exception {

        // Assemble
        final Constructor<?> nonAnnotatedConstructor = String.class.getConstructor(String.class);

        // Act
        final boolean result = conversionConstructorFilter.accept(nonAnnotatedConstructor);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void validateNullReturnedForNoConvertersFound() {

        // Assemble
        final FakeConverter noConvertersHere = new FakeConverter();

        // Act
        final Tuple<List<Method>, List<Constructor<?>>> result =
                ReflectiveConverterFilter.getConverterMethodsAndConstructors(noConvertersHere);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateNoExceptionOnNullConverterObject() {

        // Act
        final Tuple<List<Method>, List<Constructor<?>>> result =
                ReflectiveConverterFilter.getConverterMethodsAndConstructors(null);

        // Assert
        Assert.assertNull(result);
    }

    @Test
    public void validateConvertersProperlyExtractedFromConverterClass() {

        // Assemble
        final List<String> expectedMethodNames = Arrays.asList("convert", "convertToDate", "convertToDateTime",
                "lowerPriorityDateTimeConverter", "convertToCollection");
        final MultiConverter multiConverter = new MultiConverter("foobar!");

        // Act
        final Tuple<List<Method>, List<Constructor<?>>> result =
                ReflectiveConverterFilter.getConverterMethodsAndConstructors(multiConverter);

        // Assert
        Assert.assertNotNull(result);

        final List<Method> methods = result.getKey();
        final List<Constructor<?>> constructors = result.getValue();

        Assert.assertEquals(expectedMethodNames.size(), methods.size());
        Assert.assertEquals(1, constructors.size());

        Assert.assertEquals(String.class, constructors.get(0).getParameterTypes()[0]);
        for (Method current : methods) {
            Assert.assertTrue(expectedMethodNames.contains(current.getName()));
        }
    }

    @Test
    public void validateShortClassNameExtraction() {

        // Assemble
        final List<Class<?>> classes = Arrays.asList(DateTime.class, String.class, Introspector.class);

        // Act
        final Collection<String> result = CollectionAlgorithms.transform(classes,
                ReflectiveConverterFilter.SHORT_CLASSNAME_TRANSFORMER);

        // Assert
        for (Class<?> current : classes) {
            Assert.assertTrue(result.contains(current.getSimpleName()));
        }
    }

    @Test
    public void validateClassNameExtraction() {

        // Assemble
        final List<Class<?>> classes = Arrays.asList(DateTime.class, String.class, Introspector.class);

        // Act
        final Collection<String> result = CollectionAlgorithms.transform(classes,
                ReflectiveConverterFilter.FULL_CLASSNAME_TRANSFORMER);

        // Assert
        for (Class<?> current : classes) {
            Assert.assertTrue(result.contains(current.getName()));
        }
    }

    //
    // Helpers
    //


    @Converter
    public StringBuffer stringConverter(final String aString) {
        return new StringBuffer(aString);
    }

    public StringBuffer nonAnnotatedMethod(final boolean foo) {
        return new StringBuffer("" + foo);
    }

    @Converter
    public void voidMethod(final String aString) {
    }

    @Converter
    public StringBuffer stringConverter(final boolean foo, final String bar) {
        return new StringBuffer(bar);
    }

    public boolean checkDate(String aString) {

        final long result = Long.parseLong(aString);
        return result >= 0;
    }

    @Converter(conditionalConversionMethod = "checkDate")
    public Date convertToDate(final String aString) {
        return new Date(Long.parseLong(aString));
    }

    @Converter
    public StringBuffer tooFewArguments() {
        return new StringBuffer("Whoops...");
    }

    @Converter(conditionalConversionMethod = "aNonExistentMethod")
    public StringBuffer almostAConverterMethod(final String aString) {
        return new StringBuffer(aString);
    }
}

