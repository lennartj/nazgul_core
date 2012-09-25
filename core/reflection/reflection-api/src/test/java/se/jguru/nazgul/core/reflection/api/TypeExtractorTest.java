/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.reflection.api;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Filter;
import se.jguru.nazgul.core.reflection.api.annotation.AnnotatedImplementation;
import se.jguru.nazgul.core.reflection.api.annotation.AnnotatedImplementationSubclass;
import se.jguru.nazgul.core.reflection.api.annotation.AnnotatedSpecification;
import se.jguru.nazgul.core.reflection.api.annotation.TestFieldMarkerAnnotation;
import se.jguru.nazgul.core.reflection.api.annotation.TestMethodMarkerAnnotation;
import se.jguru.nazgul.core.reflection.api.annotation.TestTypeMarkerAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TypeExtractorTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullInstance() {

        // Assemble
        final Filter<Class<?>> typeAnnotationFilter = new Filter<Class<?>>() {
            @Override
            public boolean accept(final Class<?> candidate) {
                return candidate.isAnnotationPresent(TestTypeMarkerAnnotation.class);
            }
        };

        // Act & Assert
        TypeExtractor.getInterfaces(null, typeAnnotationFilter);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullFilter() {

        // Act & Assert
        TypeExtractor.getInterfaces(AnnotatedImplementation.class, null);
    }

    @Test
    public void validateInterfaceFilteringOnClassWithSeveralImplementedInterfaces() {

        // Assemble
        final Filter<Class<?>> typeAnnotationFilter = new Filter<Class<?>>() {
            @Override
            public boolean accept(final Class<?> candidate) {
                return candidate.isAnnotationPresent(TestTypeMarkerAnnotation.class);
            }
        };

        // Act
        final List<Class<?>> ifs = TypeExtractor.getInterfaces(AnnotatedImplementation.class, typeAnnotationFilter);

        // Assert
        Assert.assertNotNull(ifs);
        Assert.assertEquals(1, ifs.size());
        Assert.assertEquals(AnnotatedSpecification.class, ifs.get(0));
    }

    @Test
    public void validateMethodFiltering() throws Exception {

        // Assemble
        final Method expected = AnnotatedImplementation.class.getMethod("getValue", null);
        final Filter<Method> publicMethodMarkerAnnotationFilter = new Filter<Method>() {
            @Override
            public boolean accept(final Method candidate) {
                return Modifier.isPublic(candidate.getModifiers())
                        && candidate.isAnnotationPresent(TestMethodMarkerAnnotation.class);
            }
        };

        // Act
        final List<Method> methods = TypeExtractor.getMethods(AnnotatedImplementation.class,
                publicMethodMarkerAnnotationFilter);

        // Assert
        Assert.assertNotNull(methods);
        Assert.assertEquals(1, methods.size());
        Assert.assertEquals(expected, methods.get(0));
    }

    @Test
    public void validateMethodFilteringWithCompoundClassHierarchy() throws Exception {

        // Assemble
        final Method getValueMethod = AnnotatedImplementation.class.getMethod("getValue", null);
        final Method getValueInSubclassMethod = AnnotatedImplementationSubclass.class.getMethod("getValueInSubclass", null);

        final Filter<Method> publicMethodMarkerAnnotationFilter = new Filter<Method>() {
            @Override
            public boolean accept(final Method candidate) {
                return Modifier.isPublic(candidate.getModifiers())
                        && candidate.isAnnotationPresent(TestMethodMarkerAnnotation.class);
            }
        };

        // Act
        final List<Method> methods = TypeExtractor.getMethods(
                AnnotatedImplementationSubclass.class,
                publicMethodMarkerAnnotationFilter);

        // Assert
        Assert.assertNotNull(methods);
        Assert.assertEquals(1, methods.size());
        Assert.assertFalse(methods.contains(getValueMethod));
        Assert.assertTrue(methods.contains(getValueInSubclassMethod));
    }

    @Test
    public void validateFieldFiltering() throws Exception {

        // Assemble
        final Field expected = AnnotatedImplementationSubclass.class.getDeclaredField("aPrivateMarkedValue");
        final Filter<Field> annotatedMemberFilter = new Filter<Field>() {
            @Override
            public boolean accept(final Field candidate) {
                return candidate.isAnnotationPresent(TestFieldMarkerAnnotation.class)
                        && candidate.getAnnotation(TestFieldMarkerAnnotation.class).permit();
            }
        };

        // Act
        final List<Field> fields = TypeExtractor.getFields(AnnotatedImplementationSubclass.class, annotatedMemberFilter);

        // Assert
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(expected, fields.get(0));
    }
}
