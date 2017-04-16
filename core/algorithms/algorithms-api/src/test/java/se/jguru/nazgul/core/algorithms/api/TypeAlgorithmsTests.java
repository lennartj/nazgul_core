/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.types.BarSubtype;
import se.jguru.nazgul.core.algorithms.api.types.FooSupertype;
import se.jguru.nazgul.core.algorithms.api.types.LocaleHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TypeAlgorithmsTests {

    @Test
    public void validateFindingJavaBeanGetters() {

        // Assemble
        final String[] expectedJavaBeanGetterNames = {"readOnlyValue", "readWriteValue", "readWriteIntValue"};
        final Class<?> expectedDeclaringClass = FooSupertype.class;

        // Act
        final SortedMap<String, Method> result = TypeAlgorithms.FIND_JAVABEAN_GETTERS.apply(expectedDeclaringClass);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());

        Arrays.stream(expectedJavaBeanGetterNames).forEach(name -> Assert.assertTrue(result.containsKey(name)));
        result.forEach((key, value) -> {

            Assert.assertNotNull("Got null Getter Method for JavaBean property [" + key + "]", value);
            Assert.assertEquals(expectedDeclaringClass, value.getDeclaringClass());
        });
    }

    @Test
    public void validateFindingJavaBeanGettersInSubclass() {

        // Assemble
        final String[] gettersFromSuperclass = {"readWriteValue", "readWriteIntValue"};
        final String[] gettersFromSubclass = {"readOnlyValue", "subtypeReadOnlyValue", "subtypeReadWriteValue"};

        // Act
        final SortedMap<String, Method> result = TypeAlgorithms.FIND_JAVABEAN_GETTERS.apply(BarSubtype.class);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(5, result.size());

        Arrays.stream(gettersFromSubclass).forEach(name -> Assert.assertTrue(result.containsKey(name)));
        Arrays.stream(gettersFromSubclass).forEach((name) -> {

            final Method getterMethod = result.get(name);
            final Class declaringClass = getterMethod.getDeclaringClass();

            Assert.assertNotNull("Got null Getter Method for JavaBean property [" + name + "]", getterMethod);
            Assert.assertEquals("Expected getter [" + name + "] to be found in [" + BarSubtype.class.getName()
                            + "], but got [" + declaringClass.getName() + "]",
                    BarSubtype.class,
                    declaringClass);
        });

        Arrays.stream(gettersFromSuperclass).forEach(name -> Assert.assertTrue(result.containsKey(name)));
        Arrays.stream(gettersFromSuperclass).forEach((name) -> {

            final Method getterMethod = result.get(name);
            final Class declaringClass = getterMethod.getDeclaringClass();

            Assert.assertNotNull("Got null Getter Method for JavaBean property [" + name + "]", getterMethod);
            Assert.assertEquals("Expected getter [" + name + "] to be found in [" + FooSupertype.class.getName()
                            + "], but got [" + declaringClass + "]",
                    FooSupertype.class,
                    declaringClass);
        });
    }

    @Test
    public void validateFindingJavaBeanSettersInSubclass() {

        // Assemble
        final String[] settersFromSuperclass = {"readWriteValue", "readWriteIntValue", "writeOnlyValue"};
        final String[] settersFromSubclass = {"subtypeReadWriteValue"};

        // Act
        final SortedMap<String, Method> result = TypeAlgorithms.FIND_JAVABEAN_SETTERS.apply(BarSubtype.class);
        
        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());

        Arrays.stream(settersFromSubclass).forEach(name -> Assert.assertTrue(result.containsKey(name)));
        Arrays.stream(settersFromSubclass).forEach((name) -> {

            final Method setterMethod = result.get(name);
            final Class declaringClass = setterMethod.getDeclaringClass();

            Assert.assertNotNull("Got null Setter Method for JavaBean property [" + name + "]", setterMethod);
            Assert.assertEquals("Expected setter [" + name + "] to be found in [" + BarSubtype.class.getName()
                            + "], but got [" + declaringClass.getName() + "]",
                    BarSubtype.class,
                    declaringClass);
        });

        Arrays.stream(settersFromSuperclass).forEach(name -> Assert.assertTrue(result.containsKey(name)));
        Arrays.stream(settersFromSuperclass).forEach((name) -> {

            final Method setterMethod = result.get(name);
            final Class declaringClass = setterMethod.getDeclaringClass();

            Assert.assertNotNull("Got null Setter Method for JavaBean property [" + name + "]", setterMethod);
            Assert.assertEquals("Expected setter [" + name + "] to be found in [" + FooSupertype.class.getName()
                            + "], but got [" + declaringClass + "]",
                    FooSupertype.class,
                    declaringClass);
        });
    }

    @Test
    public void validateJavaBeanPropertyExpressionProperty() {

        // Assemble
        final LocaleHolder localeHolder = new LocaleHolder(Locale.UK);
        final String expectedCountry = localeHolder.getLocale().getCountry();

        // Act
        final Object result1 = TypeAlgorithms.getProperty(localeHolder, "locale.country");
        final Object result2 = TypeAlgorithms.getProperty(localeHolder, " locale . country ");

        // Assert
        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);
        Assert.assertEquals(expectedCountry, result1);
        Assert.assertEquals(expectedCountry, result2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentGetter() {

        // Assemble
        final LocaleHolder localeHolder = new LocaleHolder(Locale.UK);

        // Act & Assert
        TypeAlgorithms.getProperty(localeHolder, "locale.foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnInternalSpaceInExpression() {

        // Assemble
        final LocaleHolder localeHolder = new LocaleHolder(Locale.UK);

        // Act & Assert
        TypeAlgorithms.getProperty(localeHolder, "lo    cale.country");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullPropertyExpression() {

        // Assemble
        final LocaleHolder localeHolder = new LocaleHolder(Locale.UK);

        // Act & Assert
        TypeAlgorithms.getProperty(localeHolder, null);
    }
}
