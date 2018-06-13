/*-
 * #%L
 * Nazgul Project: nazgul-core-algorithms-api
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

package se.jguru.nazgul.core.algorithms.api.collections.common;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.common.ClassnameToClassTransformer;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.common.PatternMatchFilter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ClassnameToClassTransformerTest {

    @Test
    public void validateDefaultClassLoading() {

        // Assemble
        final String patternMatchFilterClassName = PatternMatchFilter.class.getName();
        final ClassnameToClassTransformer unitUnderTest = new ClassnameToClassTransformer();

        // Act
        final Class<?> result = unitUnderTest.transform(patternMatchFilterClassName);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(patternMatchFilterClassName, result.getName());
    }

    @Test
    public void validateProvidedClassLoading() {

        // Assemble
        final String patternMatchFilterClassName = PatternMatchFilter.class.getName();
        final MockClassLoader classLoader = new MockClassLoader();
        final ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        final ClassnameToClassTransformer unitUnderTest = new ClassnameToClassTransformer();

        // Act
        final Class<?> result = unitUnderTest.transform(patternMatchFilterClassName);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(patternMatchFilterClassName, result.getName());

        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Test
    public void validateFallbackClassLoading() {

        // Assemble
        final String patternMatchFilterClassName = PatternMatchFilter.class.getName();
        final ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        final ClassnameToClassTransformer unitUnderTest = new ClassnameToClassTransformer();

        // Act
        final Class<?> result = unitUnderTest.transform(patternMatchFilterClassName);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(patternMatchFilterClassName, result.getName());

        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentClass() {

        // Assemble
        final ClassnameToClassTransformer unitUnderTest = new ClassnameToClassTransformer();

        // Act & Assert
        unitUnderTest.transform("Nonexistent.Class.Name");
    }
}
