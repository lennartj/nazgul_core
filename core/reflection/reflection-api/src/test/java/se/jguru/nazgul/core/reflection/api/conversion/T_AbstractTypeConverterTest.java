/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.reflection.api.conversion;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractTypeConverterTest {

    @Test
    public void validateNullReturnedOnNullType() {

        // Act & Assert
        Assert.assertNull(AbstractTypeConverter.getClass(null));
    }

    @Test
    public void validateSimpleClassReturnedOnClassType() {

        // Assemble
        final Class<String> stringClass = String.class;

        // Act
        final Class<?> result = AbstractTypeConverter.getClass("".getClass());

        // Assert
        Assert.assertSame(stringClass, result);
    }

    @Test
    public void validateGenericTypeFromAbstractSuperclass() {

        // Act
        final List<Class<?>> typeArguments = AbstractTypeConverter.getTypeArguments(
                AbstractGenericType.class,
                StringGenericSubtype.class);

        // Assert
        Assert.assertEquals(1, typeArguments.size());
        Assert.assertEquals(String.class, typeArguments.get(0));
    }

    @Test
    public void validateTypeArgumentsFromSameType() {

        // Act
        final List<Class<?>> typeArguments = AbstractTypeConverter.getTypeArguments(
                AbstractTypeConverter.class,
                AbstractTypeConverter.class);

        // Assert
        Assert.assertEquals(2, typeArguments.size());
        Assert.assertEquals(null, typeArguments.get(0));
        Assert.assertEquals(null, typeArguments.get(1));
    }

    @Test
    public void validateTypeArgumentsFromParametrizedSubType() {

        // Act
        final MockParametrizedTypeConverter mockTypeConverter = new MockParametrizedTypeConverter();
        final List<Class<?>> typeArguments = AbstractTypeConverter.getTypeArguments(
                AbstractTypeConverter.class,
                mockTypeConverter.getClass());

        // Assert
        Assert.assertEquals(2, typeArguments.size());
        Assert.assertEquals(String.class, mockTypeConverter.getFromType());
        Assert.assertEquals(Integer.class, mockTypeConverter.getToType());
    }

    @Test
    public void validateTypeArgumentsFromExtraParametrizedTypeConverter() {

        // Act
        final ExtraParametrizedTypeConverter<Date> testConverter = new ExtraParametrizedTypeConverter<Date>();
        final boolean no = testConverter.canConvert(null);

        // Assert
        Assert.assertEquals(String.class, testConverter.getFromType());
        Assert.assertEquals(Short.class, testConverter.getToType());
        Assert.assertFalse(no);
    }

    @Test
    public void validateFallbackConstructor() {

        // Assemble
        final String toConvert = "fooBar";

        // Act
        final FallbackTypeConverter converter = new FallbackTypeConverter();
        final boolean yes = converter.canConvert(toConvert);
        final StringBuffer result = converter.convert(toConvert);

        // Assert
        Assert.assertEquals(String.class, converter.getFromType());
        Assert.assertEquals(StringBuffer.class, converter.getToType());
        Assert.assertTrue(yes);
        Assert.assertEquals(toConvert, result.toString());
    }

    @Test(expected = NullPointerException.class)
    public void validateParametrizedTypeConverter() {

        // Assemble
        final SemiTypeConverter<Integer> integerSemiTypeConverter = new SemiTypeConverter<Integer>();

        // Act & Assert
        Assert.assertEquals(String.class, integerSemiTypeConverter.getFromType());
        Assert.assertEquals(Integer.class, integerSemiTypeConverter.getToType());
    }
}
