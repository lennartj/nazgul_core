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


package se.jguru.nazgul.core.algorithms.api.types;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TypeInformationTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullSource() {

        // Act & Assert
        new TypeInformation(null);
    }

    @Test
    public void validateTypeParsingForAnnotations() {

        // Assemble
        final Class[] expectedSuperTypeAnnotations = new Class[] {Resource.class};
        // The @SuppressWarnings Annotation has RetentionPolicy#SOURCE, and should not be seen.
        final Class[] expectedSubTypeAnnotations = new Class[] {Resource.class, XmlTransient.class};

        final Class<?> subClass = BarSubtype.class;
        final Class<?> superClass = FooSupertype.class;

        // Act
        final TypeInformation superTypeInfo = new TypeInformation(superClass);
        final TypeInformation subTypeInfo = new TypeInformation(subClass);

        final SortedSet<Annotation> superTypeAnnotations = superTypeInfo.getAllAnnotations();
        final SortedSet<Annotation> subTypeAnnotations = subTypeInfo.getAllAnnotations();

        // Assert
        Assert.assertEquals(expectedSuperTypeAnnotations.length, superTypeAnnotations.size());
        Assert.assertEquals(expectedSubTypeAnnotations.length, subTypeAnnotations.size());

        final List<Class> subTypeAnnotationClasses = Arrays.asList(expectedSubTypeAnnotations);
        final List<Class> superTypeAnnotationClasses = Arrays.asList(expectedSuperTypeAnnotations);

        subTypeAnnotations.forEach(c -> subTypeAnnotationClasses.contains(c.getClass()));
        superTypeAnnotations.forEach(c -> superTypeAnnotationClasses.contains(c.getClass()));
    }

    @Test
    public void validateTypeParsingForInterfaces() {

        // Assemble
        final Class[] superTypeInterfaces = new Class[] {Serializable.class};
        final Class[] subTypeInterfaces = new Class[] {Serializable.class, Comparable.class};

        // Act
        final TypeInformation superTypeInfo = new TypeInformation(FooSupertype.class);
        final TypeInformation subTypeInfo = new TypeInformation(BarSubtype.class);

        final SortedMap<Class<?>, List<Class<?>>> superClass2InterfaceMap = superTypeInfo.getClass2InterfaceMap();
        final SortedMap<Class<?>, List<Class<?>>> subClass2InterfaceMap = subTypeInfo.getClass2InterfaceMap();

        // Assert
        Assert.assertEquals(superTypeInterfaces.length, superClass2InterfaceMap.size());
        Assert.assertEquals(subTypeInterfaces.length, subClass2InterfaceMap.size());

        final List<Class<?>> interfacesForFooSupertype = subClass2InterfaceMap.get(FooSupertype.class);
        final List<Class<?>> interfacesForBarSubtype = subClass2InterfaceMap.get(BarSubtype.class);

        Assert.assertEquals(1, interfacesForFooSupertype.size());
        Assert.assertEquals(1, interfacesForBarSubtype.size());

        Assert.assertEquals(interfacesForFooSupertype.get(0), Serializable.class);
        Assert.assertEquals(interfacesForBarSubtype.get(0), Comparable.class);
    }

    @Test
    public void validateTypeParsingForJavaBeanSettersAndGetters() {

        // Assemble
        final Class<?> clazz = BarSubtype.class;

        final String[] expectedGetters = {"readWriteValue", "readWriteIntValue", "readOnlyValue",
                "subtypeReadOnlyValue", "subtypeReadWriteValue"};

        final String[] expectedSetters = {"readWriteValue", "readWriteIntValue", "writeOnlyValue", 
                "subtypeReadWriteValue"};

        // Act
        final TypeInformation unitUnderTest = new TypeInformation(clazz);

        final SortedMap<String, Method> getterMap = unitUnderTest.getJavaBeanGetterMethods();
        final SortedMap<String, Method> setterMap = unitUnderTest.getJavaBeanSetterMethods();

        // Assert
        Assert.assertEquals(clazz, unitUnderTest.getSource());

        Assert.assertNotNull(getterMap);
        Assert.assertEquals(expectedGetters.length, getterMap.size());
        Arrays.stream(expectedGetters).forEach(g -> Assert.assertTrue(getterMap.containsKey(g)));

        Assert.assertNotNull(setterMap);
        Assert.assertEquals(expectedSetters.length, setterMap.size());
        Arrays.stream(expectedSetters).forEach(s -> Assert.assertTrue(setterMap.containsKey(s)));
    }
}
