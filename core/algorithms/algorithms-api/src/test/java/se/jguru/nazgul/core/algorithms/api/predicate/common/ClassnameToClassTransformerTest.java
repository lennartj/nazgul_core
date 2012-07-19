/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.algorithms.api.predicate.common;

import junit.framework.Assert;
import org.junit.Test;

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
