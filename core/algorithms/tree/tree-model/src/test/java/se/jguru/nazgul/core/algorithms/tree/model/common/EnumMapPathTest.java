/*
 * #%L
 * Nazgul Project: nazgul-core-algorithms-tree-model
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
package se.jguru.nazgul.core.algorithms.tree.model.common;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.CityIdentifier;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.IncorrectEmptyEnum;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.io.StringReader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnumMapPathTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnOverflowSegments() {

        // Assemble
        final String compoundPath = "Sweden/Västra Götaland/Göteborg/Haga";
        final Class<CityIdentifier> enumType = CityIdentifier.class;

        // Act & Assert
        new EnumMapPath<CityIdentifier>(compoundPath, enumType);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void validateExceptionOnOverflowSegmentsOnAppend() {

        // Assemble
        final String compoundPath = "Sweden/Halland/Kungsbacka";
        final EnumMapPath<CityIdentifier> cityPath = new EnumMapPath<CityIdentifier>(
                compoundPath, CityIdentifier.class);

        // Act & Assert
        cityPath.append("Haga");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullEnumType() {

        // Assemble
        final String compoundPath = "Sweden/Halland/Göteborg";
        final Class<CityIdentifier> enumType = null;

        // Act & Assert
        new EnumMapPath<CityIdentifier>(compoundPath, enumType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyEnumType() {

        // Assemble
        final String compoundPath = "Sweden/Halland/Göteborg";
        final Class<IncorrectEmptyEnum> enumType = IncorrectEmptyEnum.class;

        // Act & Assert
        new EnumMapPath<IncorrectEmptyEnum>(compoundPath, enumType);
    }

    @Test
    public void validateAccessingSegmentsUsingSemanticPathMethods() {

        // Assemble
        final String aRegion = "Sweden/Halland";
        final String city1 = "Sweden/Halland/Kungsbacka";

        final EnumMapPath<CityIdentifier> unitUnderTest1 = new EnumMapPath<CityIdentifier>(
                aRegion, CityIdentifier.class);
        final EnumMapPath<CityIdentifier> unitUnderTest2 = new EnumMapPath<CityIdentifier>(
                city1, CityIdentifier.class);

        // Act
        final EnumMapPath<CityIdentifier> unitUnderTest3 = unitUnderTest1.append("Kungsbacka");

        // Assert
        Assert.assertNotSame(unitUnderTest2, unitUnderTest3);
        Assert.assertEquals(unitUnderTest2, unitUnderTest3);

        Assert.assertEquals("Sweden", unitUnderTest2.get(CityIdentifier.COUNTRY));
        Assert.assertEquals("Halland", unitUnderTest2.get(CityIdentifier.REGION));
        Assert.assertEquals("Kungsbacka", unitUnderTest2.get(CityIdentifier.CITY));
        Assert.assertNull(unitUnderTest1.get(CityIdentifier.CITY));

        Assert.assertEquals(CityIdentifier.values().length, unitUnderTest1.getMaxSize());
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String compoundPath = "Sweden/Västra Götaland/Göteborg";
        final EnumMapPath<CityIdentifier> unitUnderTest = new EnumMapPath<CityIdentifier>(
                compoundPath, CityIdentifier.class);
        final String expected = XmlTestUtils.readFully("testdata/anEnumMapPath.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final String result = binder.marshal(unitUnderTest);
        // System.out.println("Result: " + result);

        // Assert
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        Assert.assertTrue("Detailed Diff: " + new DetailedDiff(diff), diff.identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String compoundPath = "Sweden/Västra Götaland/Göteborg";
        final EnumMapPath<CityIdentifier> unitUnderTest = new EnumMapPath<CityIdentifier>(
                compoundPath, CityIdentifier.class);
        final String data = XmlTestUtils.readFully("testdata/anEnumMapPath.xml");

        final JaxbXmlBinder binder = new JaxbXmlBinder();

        // Act
        final List<?> unmarshalled = binder.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.size());

        final EnumMapPath<CityIdentifier> emp = (EnumMapPath<CityIdentifier>) unmarshalled.get(0);
        // System.out.println("Got: " + emp);
        Assert.assertEquals(unitUnderTest, emp);
    }
}
