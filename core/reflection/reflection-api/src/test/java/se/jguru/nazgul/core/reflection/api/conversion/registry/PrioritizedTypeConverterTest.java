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
        final TypeConverter<String, StringBuffer> result = unitUnderTest.getOptimumConverter(StringBuffer.class);

        // Assert
        Assert.assertEquals(String.class, unitUnderTest.getSourceType());
        Assert.assertNull(result);
    }

    @Test
    public void validateNullValuePermittedConverter() {

        // Assemble
        final MultiConverter multiConverter = new MultiConverter("fooBar");

        // Act
        unitUnderTest.add(multiConverter);
        final TypeConverter<String, StringBuffer> result = unitUnderTest.getOptimumConverter(StringBuffer.class);

        // Assert
        Assert.assertTrue(result.canConvert(null));
        Assert.assertEquals("nothing!", result.convert(null).toString());
    }

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
        final TypeConverter<String, DateTime> optimumConverter = unitUnderTest.getOptimumConverter(DateTime.class);
        final TypeConverter<String, DateTime> specificConverter =
                unitUnderTest.getConverterWithMinimumPriority(150, DateTime.class);

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

    }

    @Test
    public void validateComparison() {

        // Assemble
        final PrioritizedTypeConverter<DateTime> dateTimeConverter =
                new PrioritizedTypeConverter<DateTime>(DateTime.class);

        // Act
        final int result = unitUnderTest.compareTo(dateTimeConverter);

        // Assert
        Assert.assertEquals(unitUnderTest.getSourceType().getName()
                .compareTo(dateTimeConverter.getSourceType().getName()),
                result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingNonConverterObject() {

        // Assemble
        final FakeConverter noConverterMethodsInHere = new FakeConverter();

        // Act & Assert
        unitUnderTest.add(noConverterMethodsInHere);
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
