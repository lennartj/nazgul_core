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
import se.jguru.nazgul.core.reflection.api.conversion.ConverterRegistry;
import se.jguru.nazgul.core.reflection.api.conversion.registry.helpers.MockConverterRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TypeExtractorTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullInstance() {

        // Assemble
        final Predicate<Class<?>> typeAnnotationFilter =
                candidate -> candidate.isAnnotationPresent(TestTypeMarkerAnnotation.class);

        // Act & Assert
        TypeExtractor.getInterfaces(null, typeAnnotationFilter);
    }

    @Test
    public void validateNoExceptionOnNullFilter() {

        // Act & Assert
        TypeExtractor.getInterfaces(AnnotatedImplementation.class, null);
    }

    @Test
    public void validateInterfaceFilteringOnClassWithSeveralImplementedInterfaces() {

        // Assemble
        final Predicate<Class<?>> typeAnnotationFilter =
                candidate -> candidate.isAnnotationPresent(TestTypeMarkerAnnotation.class);

        // Act
        final SortedSet<Class<?>> ifs = TypeExtractor.getInterfaces(AnnotatedImplementation.class,
                typeAnnotationFilter);

        // Assert
        Assert.assertNotNull(ifs);
        Assert.assertEquals(1, ifs.size());
        Assert.assertEquals(AnnotatedSpecification.class, ifs.first());
    }

    @Test
    public void validateMethodFiltering() throws Exception {

        // Assemble
        final Method expected = AnnotatedImplementation.class.getMethod("getValue", (java.lang.Class<?>[]) null);
        final Predicate<Method> publicMethodMarkerAnnotationFilter = candidate ->
                Modifier.isPublic(candidate.getModifiers())
                        && candidate.isAnnotationPresent(TestMethodMarkerAnnotation.class);

        // Act
        final SortedSet<Method> methods = TypeExtractor.getMethods(AnnotatedImplementation.class,
                publicMethodMarkerAnnotationFilter);

        // Assert
        Assert.assertNotNull(methods);
        Assert.assertEquals(1, methods.size());
        Assert.assertEquals(expected, methods.first());
    }

    @Test
    public void validateMethodFilteringWithCompoundClassHierarchy() throws Exception {

        // Assemble
        final Method getValueMethod = AnnotatedImplementation.class.getMethod(
                "getValue",
                (java.lang.Class<?>[]) null);

        final Method getValueInSubclassMethod = AnnotatedImplementationSubclass.class.getMethod(
                "getValueInSubclass",
                (java.lang.Class<?>[]) null);

        final Predicate<Method> publicMethodMarkerAnnotationFilter = candidate ->
                Modifier.isPublic(candidate.getModifiers())
                        && candidate.isAnnotationPresent(TestMethodMarkerAnnotation.class);
        // Act
        final SortedSet<Method> methods = TypeExtractor.getMethods(
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
        final Predicate<Field> annotatedMemberFilter = candidate ->
                candidate.isAnnotationPresent(TestFieldMarkerAnnotation.class)
                        && candidate.getAnnotation(TestFieldMarkerAnnotation.class).permit();

        // Act
        final SortedSet<Field> fields = TypeExtractor.getFields(
                AnnotatedImplementationSubclass.class, annotatedMemberFilter);

        // Assert
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        Assert.assertEquals(expected, fields.first());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnUnrelatedClasses() {

        // Act & Assert
        TypeExtractor.getRelationDifference(String.class, Set.class);
    }

    @Test
    public void validateRelationDifference() {

        // Assemble

        // Act & Assert
        Assert.assertEquals(0, TypeExtractor.getRelationDifference(Set.class, Set.class));
        Assert.assertEquals(-2, TypeExtractor.getRelationDifference(AbstractSet.class, HashSet.class));
        Assert.assertEquals(2, TypeExtractor.getRelationDifference(HashSet.class, AbstractSet.class));
        Assert.assertEquals(-4, TypeExtractor.getRelationDifference(Object.class, Float.class));
        Assert.assertEquals(4, TypeExtractor.getRelationDifference(Integer.class, Object.class));
        Assert.assertEquals(-5, TypeExtractor.getRelationDifference(Collection.class, HashSet.class));
        Assert.assertEquals(3, TypeExtractor.getRelationDifference(
                MockConverterRegistry.class, ConverterRegistry.class));
    }
}
