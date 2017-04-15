/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.api.collections.CollectionAlgorithms;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Transformer;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.marshal.ClassWithPrimitivesAndCollections;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.marshal.XmlTransientClass;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class XmlMarshallableFieldFilterTest {

    // Shared state
    public static final Transformer<Field, String> FIELDNAME_TRANSFORMER = new Transformer<Field, String>() {
        @Override
        public String transform(final Field input) {
            return input.getName();
        }
    };

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
        XmlMarshallableFieldFilter.getMarshallableFields(null);
    }

    @Test
    public void validateMarshallableFields() throws Exception {

        // Assemble
        final List<String> expected = Arrays.asList("aNonAnnotatedMap");
        final List<Field> potentiallyXmlMarshallableFields =
                XmlMarshallableFieldFilter.getMarshallableFields(primitivesAndCollections);

        // Act
        final List<String> marshallableFieldNames = CollectionAlgorithms.transform(
                potentiallyXmlMarshallableFields, FIELDNAME_TRANSFORMER);
        Collections.sort(marshallableFieldNames);

        // Assert
        Assert.assertEquals(expected, marshallableFieldNames);
        Field mapField = potentiallyXmlMarshallableFields.get(0);
        mapField.setAccessible(true);
        Assert.assertSame(mapData, mapField.get(primitivesAndCollections));
    }

    @Test
    public void validateXmlTransientAnnotatedClassesAreNotMapped() {

        // Assemble
        final XmlTransientClass shouldNotBeMapped = new XmlTransientClass(data);
        final List<Field> potentiallyXmlMarshallableFields =
                XmlMarshallableFieldFilter.getMarshallableFields(shouldNotBeMapped);

        // Act
        final List<String> marshallableFieldNames = CollectionAlgorithms.transform(
                potentiallyXmlMarshallableFields, FIELDNAME_TRANSFORMER);
        Collections.sort(marshallableFieldNames);

        // Assert
        Assert.assertTrue(marshallableFieldNames.isEmpty());
    }
}
