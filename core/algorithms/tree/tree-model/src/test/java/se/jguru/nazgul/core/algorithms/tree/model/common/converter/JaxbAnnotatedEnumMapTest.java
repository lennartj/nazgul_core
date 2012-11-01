/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.algorithms.tree.model.common.converter;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.JaxbXmlBinder;

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
    public void validateMarshalling() {

        // Assemble
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<core:entityTransporter xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:core=\"http://www.jguru.se/nazgul/core\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <entityClasses>\n" +
                "        <entityClass>se.jguru.nazgul.core.algorithms.tree.model.common.converter.JaxbAnnotatedEnumMap</entityClass>\n" +
                "        <entityClass>se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter</entityClass>\n" +
                "    </entityClasses>\n" +
                "    <items>\n" +
                "        <item xsi:type=\"core:jaxbAnnotatedEnumMap\" enumType=\"se.jguru.nazgul.core.algorithms.tree.model.common.helpers.Adjustment\">\n" +
                "            <values xsi:type=\"xs:string\">Left!</values>\n" +
                "            <values xsi:type=\"xs:string\">Center!</values>\n" +
                "        </item>\n" +
                "    </items>\n" +
                "</core:entityTransporter>\n";

        final JaxbAnnotatedEnumMap<Adjustment> unitUnderTest = new JaxbAnnotatedEnumMap<Adjustment>(
                enumMap, Adjustment.class);

        // Act
        final String result = binder.marshal(unitUnderTest);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
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
