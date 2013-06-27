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
package se.jguru.nazgul.core.reflection.api.conversion.registry;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.CollectionsConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.FakeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.MultiConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultConverterRegistryTest {

    // Shared state
    private DefaultConverterRegistry unitUnderTest;
    private MultiConverter multiConverter;

    @Before
    public void setupSharedState() {
        unitUnderTest = new DefaultConverterRegistry();
        multiConverter = new MultiConverter("fooBar!");
    }

    @Test
    public void validateBlankState() {

        // Act
        final Set<Class<?>> result = unitUnderTest.getPossibleConversions(String.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void validatePossibleConversions() {

        // Assemble
        final List<Class<?>> expectedTypes = Arrays.asList(DateTime.class, String.class, MultiConverter.class);

        // Act
        unitUnderTest.add(multiConverter);
        final Set<Class<?>> result = unitUnderTest.getPossibleConversions(String.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());

        for (Class<?> current : expectedTypes) {
            Assert.assertTrue(expectedTypes.contains(current));
        }
    }

    @Test
    public void validateNullReturnedForIncapableConversion() {

        // Act
        unitUnderTest.add(multiConverter);
        final SimpleDateFormat result = unitUnderTest.convert(42, SimpleDateFormat.class);

        // Assert
        Assert.assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullConversionToType() {

        // Act & Assert
        unitUnderTest.getPossibleConversions(null);
    }

    @Test
    public void validateExceptionOnAddingNonConverterObject() {

        // Assemble
        final FakeConverter nonConverter = new FakeConverter();
        final Map<Class<?>, PrioritizedTypeConverter> convertersMap = getConvertersMap(unitUnderTest);

        // Act
        try {
            unitUnderTest.add(multiConverter, nonConverter);
            Assert.fail("Adding a non-converter-annotated object should yield exception.");
        } catch (IllegalArgumentException expected) {
            // Do nothing
        }

        // Assert.
        Assert.assertEquals(0, convertersMap.size());
    }

    @Test
    public void validateStateAfterAddingConverters() {

        // Assemble
        final List<Class<?>> expectedFroms = Arrays.asList(DateTime.class, String.class, Object.class);
        final Map<Class<?>, PrioritizedTypeConverter> convertersMap = getConvertersMap(unitUnderTest);

        // Act
        unitUnderTest.add(multiConverter);

        // Assert
        Assert.assertEquals(3, convertersMap.size());
        for (Class<?> current : expectedFroms) {
            Assert.assertTrue("Expected type [" + current.getSimpleName() + "] was not found.",
                    convertersMap.keySet().contains(current));
        }
    }

    @Test
    public void validateNormalConversion() {

        // Assemble
        final String hourMinuteDateForm = "2012-05-06T07:08";
        final String isoDateTimeDateForm = "20120506T070809+0100";

        // Act
        unitUnderTest.add(multiConverter);
        final DateTime result1 = unitUnderTest.convert(hourMinuteDateForm, DateTime.class);
        final DateTime result2 = unitUnderTest.convert(isoDateTimeDateForm, DateTime.class);

        // Assert
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);

        Assert.assertEquals(6, result1.getDayOfMonth());
        Assert.assertEquals(6, result2.getDayOfMonth());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateRemovalOfConvertersNotYetImplemented() {

        // Act & Assert
        unitUnderTest.remove(multiConverter);
    }

    @Test
    public void validateToStringPrintout() {

        // Act
        final String result = unitUnderTest.toString();

        // Assert
        Assert.assertNotNull(result);
        Assert.assertFalse(result.contains("@"));
    }

    @Test
    public void validateFuzzyLogicConversion() {

        // Assemble
        final String setToListConverterElementIdentifier = "convertSetToList";
        final HashSet<String> set = new HashSet<String>(Arrays.asList("foo", "bar"));
        unitUnderTest.add(new CollectionsConverter());

        // Act
        final Collection result1 = unitUnderTest.convert(set, SortedSet.class);
        final Collection result2 = unitUnderTest.convert(set, List.class);

        // Assert
        Assert.assertNull(result1);
        Assert.assertNotNull(result2);

        Assert.assertTrue("convertSetToList element not found --> Incorrect type converter selected.",
                result2.contains(setToListConverterElementIdentifier));

        for (String current : set) {
            Assert.assertTrue(result2.contains(current));
        }
    }

    @Test
    public void validatePossibleFuzzyLogicConverterTypesSelection1() {

        // Assemble
        unitUnderTest.add(new CollectionsConverter());

        // Act
        final Set<Class<?>> possibleConversions = unitUnderTest.getPossibleConversions(TreeSet.class);

        // Assert
        Assert.assertEquals(1, possibleConversions.size());
        Assert.assertEquals(List.class, possibleConversions.iterator().next());
    }

    //
    // Private helpers
    //

    private Map<Class<?>, PrioritizedTypeConverter> getConvertersMap(final DefaultConverterRegistry instance) {

        try {
            Field mapField = DefaultConverterRegistry.class.getDeclaredField("sourceTypeToTypeConvertersMap");
            mapField.setAccessible(true);
            return (Map<Class<?>, PrioritizedTypeConverter>) mapField.get(instance);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not acquire field.", e);
        }
    }

    private <From> SortedMap<Integer, PrioritizedTypeConverter> getPrioritizedConverters(
            final Class<From> sourceType, final DefaultConverterRegistry registry) {

        try {
            Method method = DefaultConverterRegistry.class.getDeclaredMethod("getPrioritizedConverters", Class.class);
            method.setAccessible(true);

            return (SortedMap<Integer, PrioritizedTypeConverter>) method.invoke(registry, sourceType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not aquuire prioritizedConverters", e);
        }
    }
}
