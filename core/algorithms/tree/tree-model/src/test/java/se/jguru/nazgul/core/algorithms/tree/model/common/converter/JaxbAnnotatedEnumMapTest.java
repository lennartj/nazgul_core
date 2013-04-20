/*
 * #%L
 * Nazgul Project: nazgul-core-tree-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.util.EnumMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedEnumMapTest {

    // Shared state
    private JaxbXmlBinder binder;
    private EnumMap<Adjustment, String> enumMap;

    @Before
    public void setupSharedState() {

        enumMap = new EnumMap<Adjustment, String>(Adjustment.class);
        enumMap.put(Adjustment.LEFT, "Left!");
        enumMap.put(Adjustment.CENTER, "Center!");

        binder = new JaxbXmlBinder();
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnGettingValueAfterDefaultConstruction() {

        // Assemble
        final JaxbAnnotatedEnumMap<Adjustment> unitUnderTest = new JaxbAnnotatedEnumMap<Adjustment>();

        // Act & Assert
        Assert.assertNull(unitUnderTest.getEnumMap());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/aJaxbAnnotatedEnumMapPath.xml");

        final JaxbAnnotatedEnumMap<Adjustment> unitUnderTest = new JaxbAnnotatedEnumMap<Adjustment>(
                enumMap, Adjustment.class);

        // Act
        final String result = binder.marshal(unitUnderTest);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    /*
    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final DateTime valueCopy = new DateTime(2012, 7, 6, 5, 4, 3);
        final DateTime anotherValue = new DateTime(2012, 8, 7, 6, 5, 4);
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);

        // Act & Assert
        Assert.assertTrue(unitUnderTest.equals(valueCopy));
        Assert.assertFalse(unitUnderTest.equals(anotherValue));

        Assert.assertEquals(new JaxbAnnotatedDateTime(valueCopy), unitUnderTest);
        Assert.assertFalse(unitUnderTest.equals(new JaxbAnnotatedDateTime(anotherValue)));

        Assert.assertFalse(unitUnderTest.equals(null));
        Assert.assertFalse(unitUnderTest.equals(new Date()));

        Assert.assertEquals(value.hashCode(), unitUnderTest.hashCode());
    }

    @Test(expected = ClassCastException.class)
    public void validateExceptionOnComparingWithNonStringType() {

        // Assemble
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);

        // Act & Assert
        unitUnderTest.compareTo(new Date());
    }
    */
}
