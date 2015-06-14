/*
 * #%L
 * Nazgul Project: nazgul-core-reflection-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.reflection.api.conversion.Converter;
import se.jguru.nazgul.core.reflection.api.conversion.TypeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.FakeConverter;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.MultiConverter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid, jGuru Europe AB</a>
 */
public class PrioritizedTypeConverterTest {

    // Shared state
    private PrioritizedTypeConverter<String> unitUnderTest;

    @Before
    public void setupSharedState() {
        unitUnderTest = new PrioritizedTypeConverter<String>(String.class);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSourceType() {

        // Act & Assert
        new PrioritizedTypeConverter<String>(null);
    }

    @Test
    public void validateEmptyStateOnNoConvertersAdded() {

        // Act
        final List<TypeConverter<String, StringBuffer>> result = unitUnderTest.getTypeConverters(StringBuffer.class);

        // Assert
        Assert.assertEquals(String.class, unitUnderTest.getSourceType());
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingNonConverterObjects() {

        // Act & Assert
        unitUnderTest.add(new FakeConverter());
    }

    @Test
    public void validateBehaviourOnAddingTwoIdenticalConverters() {

        // Assemble
        final MultiConverter multiConverter = new MultiConverter("fooBar!");

        // Act
        unitUnderTest.add(multiConverter);

        final int targetTypesSizeBeforeReAdd = unitUnderTest.getAvailableTargetTypes().size();
        final int convertersSizeBeforeReAdd = unitUnderTest.getTypeConverters(StringBuffer.class).size();

        unitUnderTest.add(multiConverter);

        // Assert
        Assert.assertEquals(targetTypesSizeBeforeReAdd, unitUnderTest.getAvailableTargetTypes().size());
        Assert.assertEquals(convertersSizeBeforeReAdd, unitUnderTest.getTypeConverters(StringBuffer.class).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateNullValuePermittedConverter() {

        // Assemble
        final MultiConverter multiConverter = new MultiConverter("fooBar");
        final SortedMap<Integer, Map<Class<?>, TypeConverter<String, ?>>> typeConverterMap =
                getTypeConverterMap(unitUnderTest);

        // Act
        unitUnderTest.add(multiConverter);
        final StringBuffer result = unitUnderTest.convert(null, StringBuffer.class);
        final TypeConverter<String, StringBuffer> internalConverter = (TypeConverter<String, StringBuffer>)
                typeConverterMap.get(Converter.DEFAULT_PRIORITY).get(StringBuffer.class);

        // Assert
        Assert.assertTrue(internalConverter.canConvert(null));
        Assert.assertEquals("nothing!", internalConverter.convert(null).toString());
        Assert.assertEquals("nothing!", result.toString());
    }

    @SuppressWarnings("unused")
    @Test
    public void validateAddingConverters() {

        // Assemble
        final String hourMinuteDateForm = "2012-05-06T07:08";
        final String isoDateTimeDateForm = "20120506T070809+0100";
        final MultiConverter multiConverter = new MultiConverter("fooBar");
        final SortedMap<Integer, Map<Class<?>, TypeConverter<String, ?>>> typeConverterMap =
                getTypeConverterMap(unitUnderTest);

        // Act
        unitUnderTest.add(multiConverter);
        final List<TypeConverter<String, DateTime>> typeConverters = unitUnderTest.getTypeConverters(DateTime.class);
        final TypeConverter<String, DateTime> optimumConverter = typeConverters.get(0);
        final TypeConverter<String, DateTime> specificConverter = typeConverters.get(1);

        // Assert
        Assert.assertNotNull(optimumConverter);
        Assert.assertNotNull(specificConverter);
        Assert.assertNotSame(optimumConverter, specificConverter);

        Assert.assertEquals(String.class, optimumConverter.getFromType());
        Assert.assertEquals(DateTime.class, optimumConverter.getToType());

        Assert.assertTrue(optimumConverter.canConvert(isoDateTimeDateForm));
        Assert.assertFalse(optimumConverter.canConvert(hourMinuteDateForm));
        Assert.assertTrue(specificConverter.canConvert(hourMinuteDateForm));
        Assert.assertFalse(specificConverter.canConvert(isoDateTimeDateForm));

        Assert.assertEquals(DateTime.parse(hourMinuteDateForm, ISODateTimeFormat.dateHourMinute()),
                specificConverter.convert(hourMinuteDateForm));
        Assert.assertEquals(DateTime.parse(isoDateTimeDateForm, ISODateTimeFormat.basicDateTimeNoMillis()),
                optimumConverter.convert(isoDateTimeDateForm));
    }

    @Test
    public void validateInternalStructureFollowingNormalConverterAdd() {

        // Assemble
        final String methodTypeConverterClass = PrioritizedTypeConverter.MethodTypeConverter.class.getSimpleName();
        final String constructorTypeConverterClass =
                PrioritizedTypeConverter.ConstructorTypeConverter.class.getSimpleName();

        final MultiConverter multiConverter = new MultiConverter("fooBar");
        final SortedMap<Integer, Map<Class<?>, TypeConverter<String, ?>>> typeConverterMap =
                getTypeConverterMap(unitUnderTest);

        // Act
        unitUnderTest.add(multiConverter);

        // Assert
        final Map<Class<?>, TypeConverter<String, ?>> defaultConverters =
                typeConverterMap.get(Converter.DEFAULT_PRIORITY);
        final Map<Class<?>, TypeConverter<String, ?>> lowerPriorityConverters = typeConverterMap.get(200);

        Assert.assertEquals(3, defaultConverters.size());
        Assert.assertEquals(1, lowerPriorityConverters.size());

        Assert.assertEquals(methodTypeConverterClass,
                defaultConverters.get(StringBuffer.class).getClass().getSimpleName());
        Assert.assertEquals(constructorTypeConverterClass,
                defaultConverters.get(MultiConverter.class).getClass().getSimpleName());
        Assert.assertEquals(methodTypeConverterClass,
                defaultConverters.get(DateTime.class).getClass().getSimpleName());

        Assert.assertEquals(methodTypeConverterClass,
                lowerPriorityConverters.get(DateTime.class).getClass().getSimpleName());

        for (TypeConverter<String, ?> current : defaultConverters.values()) {
            Assert.assertNotNull(current.toString());
        }
    }

    @Test
    public void validateComparison() {

        // Assemble
        final PrioritizedTypeConverter<DateTime> dateTimeConverter =
                new PrioritizedTypeConverter<DateTime>(DateTime.class);

        // Act
        final int result1 = unitUnderTest.compareTo(dateTimeConverter);
        final int result2 = unitUnderTest.compareTo(null);

        // Assert
        Assert.assertEquals(unitUnderTest.getSourceType().getName()
                        .compareTo(dateTimeConverter.getSourceType().getName()),
                result1);
        Assert.assertEquals(Integer.MAX_VALUE, result2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingNonConverterObject() {

        // Assemble
        final FakeConverter noConverterMethodsInHere = new FakeConverter();

        // Act & Assert
        unitUnderTest.add(noConverterMethodsInHere);
    }

    @Test
    public void validateFuzzyLogicConversion() {

        // Assemble
        final PrioritizedTypeConverter<Object> unitUnderTest = new PrioritizedTypeConverter<Object>(Object.class);
        unitUnderTest.add(new MultiConverter("fooBar!"));
        final String[] stringArray = new String[]{"foo", "bar"};

        // Act
        final List convert = unitUnderTest.convert(stringArray, ArrayList.class);

        // Assert
        Assert.assertNotNull(convert);
        Assert.assertEquals(stringArray.length, convert.size());
        for (int i = 0; i < stringArray.length; i++) {
            Assert.assertEquals(stringArray[i], convert.get(i));
        }
    }

    //
    // Private helpers
    //

    private <From> SortedMap<Integer, Map<Class<?>, TypeConverter<From, ?>>> getTypeConverterMap(
            final PrioritizedTypeConverter<From> converter) {

        try {
            final Field field = converter.getClass().getDeclaredField("prioritizedTypeConverterMap");
            field.setAccessible(true);
            return (SortedMap<Integer, Map<Class<?>, TypeConverter<From, ?>>>) field.get(converter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not access the converterMap", e);
        }
    }
}
