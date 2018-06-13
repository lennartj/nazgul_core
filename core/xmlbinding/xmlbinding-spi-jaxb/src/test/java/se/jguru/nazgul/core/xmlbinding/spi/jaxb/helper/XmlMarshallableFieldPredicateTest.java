/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.marshal.ClassWithPrimitivesAndCollections;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.marshal.XmlTransientClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XmlMarshallableFieldPredicateTest {

    // Shared state
    private ClassWithPrimitivesAndCollections primitivesAndCollections;
    private List<String> data = Arrays.asList("one", "two", "threeee");
    private Map<String, Object> mapData;

    @Before
    public void setupSharedState() {

        mapData = new TreeMap<String, Object>();
        mapData.put("foo", "bar");
        mapData.put("baz", "gnat");

        primitivesAndCollections = new ClassWithPrimitivesAndCollections(
                "aString",
                42,
                new HashSet<String>(data),
                mapData,
                "gnus",
                data);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullObject() {

        // Act & Assert
        XmlMarshallableFieldPredicate.getMarshallableFields(null);
    }

    @Test
    public void validateMarshallableFields() throws Exception {

        // Assemble
        final SortedSet<String> expected = new TreeSet<>(Collections.singletonList("aNonAnnotatedMap"));
        final SortedSet<Field> potentiallyXmlMarshallableFields =
                XmlMarshallableFieldPredicate.getMarshallableFields(primitivesAndCollections);

        // Act
        final SortedSet<String> marshallableFieldNames = potentiallyXmlMarshallableFields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        // Assert
        Assert.assertEquals(expected, marshallableFieldNames);
        Field mapField = potentiallyXmlMarshallableFields.first();
        mapField.setAccessible(true);
        Assert.assertSame(mapData, mapField.get(primitivesAndCollections));
    }

    @Test
    public void validateXmlTransientAnnotatedClassesAreNotMapped() {

        // Assemble
        final XmlTransientClass shouldNotBeMapped = new XmlTransientClass(data);
        final SortedSet<Field> potentiallyXmlMarshallableFields =
                XmlMarshallableFieldPredicate.getMarshallableFields(shouldNotBeMapped);

        // Act
        final SortedSet<String> marshallableFieldNames = potentiallyXmlMarshallableFields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        // Assert
        Assert.assertTrue(marshallableFieldNames.isEmpty());
    }
}
