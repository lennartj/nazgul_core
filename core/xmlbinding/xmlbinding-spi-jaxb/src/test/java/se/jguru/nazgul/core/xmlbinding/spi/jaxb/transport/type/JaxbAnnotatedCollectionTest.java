/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
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
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.AbstractStandardizedTimezoneTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("rawtypes")
public class JaxbAnnotatedCollectionTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private String fooBar = "FooBar!";
    private DateTime firstAprilThreePm = new DateTime(2012, 4, 1, 15, 0, DateTimeZone.UTC);
    private int meaningOfLife = 42;

    private List<Object> data1D;
    private List<Object> data2D;
    private List<Object> data3D;

    @Override
    public void setupSharedState() {

        // Create the data structures
        data1D = new ArrayList<Object>();
        data2D = new ArrayList<Object>();
        data3D = new ArrayList<Object>();

        // data1D
        data1D.add(fooBar);
        data1D.add(firstAprilThreePm);
        data1D.add(meaningOfLife);

        // data2D
        data2D.add(fooBar);
        data2D.add(firstAprilThreePm);
        data2D.add(data1D);
        data2D.add(meaningOfLife);
        data2D.add(null);

        // data3d
        data3D.add(fooBar);
        data3D.add(data2D);
    }

    @Test
    public void validate1DClassInformationAndUsage() {

        // Act
        final List<? extends Class<? extends Serializable>> expectedClasses = Arrays.asList(String.class,
                Integer.class, DateTime.class, JaxbAnnotatedDateTime.class);
        final JaxbAnnotatedCollection<List> unitUnderTest = new JaxbAnnotatedCollection<List>(data1D);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        final List result = unitUnderTest.getValue();
        Assert.assertEquals(data1D.size(), result.size());
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(data1D.get(i), result.get(i));
        }

        Assert.assertEquals(expectedClasses.size(), classInformation.size());
        for (Class<? extends Serializable> current : expectedClasses) {
            Assert.assertTrue(classInformation.contains(current.getName()));
        }
    }

    @Test
    public void validate2DClassInformationAndUsage() {

        // Act
        final List<? extends Class<? extends Serializable>> expectedClasses = Arrays.asList(String.class,
                Integer.class, DateTime.class, JaxbAnnotatedDateTime.class,
                JaxbAnnotatedNull.class, JaxbAnnotatedCollection.class);
        final JaxbAnnotatedCollection<List> unitUnderTest = new JaxbAnnotatedCollection<List>(data2D);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        final List result = unitUnderTest.getValue();
        Assert.assertEquals(data2D.size(), result.size());
        for (int i = 0; i < result.size(); i++) {
            Assert.assertEquals(data2D.get(i), result.get(i));
        }

        Assert.assertEquals(expectedClasses.size(), classInformation.size());
        for (Class<? extends Serializable> current : expectedClasses) {
            Assert.assertTrue(classInformation.contains(current.getName()));
        }
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final JaxbAnnotatedCollection<List> unitUnderTest = new JaxbAnnotatedCollection<List>(data1D);
        final List<Object> anotherList = new ArrayList<Object>();
        anotherList.add("FooBar!");

        // Act
        final boolean collectionEqualsResult1 = unitUnderTest.equals(data1D);
        final boolean collectionEqualsResult2 = unitUnderTest.equals(anotherList);
        final boolean jaxbEqualsResult1 = unitUnderTest.equals(new JaxbAnnotatedCollection<List>(data1D));
        final boolean jaxbEqualsResult2 = unitUnderTest.equals(new JaxbAnnotatedCollection<List>(anotherList));
        final boolean result1 = unitUnderTest.equals(null);
        final boolean result2 = unitUnderTest.equals(new Date());

        // Assert
        Assert.assertTrue(collectionEqualsResult1);
        Assert.assertFalse(collectionEqualsResult2);
        Assert.assertTrue(jaxbEqualsResult1);
        Assert.assertFalse(jaxbEqualsResult2);
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
        Assert.assertEquals(data1D.hashCode(), unitUnderTest.hashCode());
    }

    @Test(expected = ClassCastException.class)
    public void validateExceptionOnComparingWithNonCollectionType() {

        // Assemble
        final JaxbAnnotatedCollection<List> unitUnderTest = new JaxbAnnotatedCollection<List>(data1D);

        // Act & Assert
        unitUnderTest.compareTo(new Date());
    }

    @Test
    public void validateAcquireGenericTypes() {

        // Assemble
        final List<? extends Class<? extends Serializable>> expectedClasses =
                Arrays.asList(DateTime.class, JaxbAnnotatedCollection.class, JaxbAnnotatedDateTime.class);
        final DateTime dateTime = new DateTime(2012, 7, 6, 5, 4, 3);
        final List<List<DateTime>> dateTimeListList = new ArrayList<List<DateTime>>();
        final List<DateTime> dateTimes = new ArrayList<DateTime>();
        dateTimes.add(dateTime);
        dateTimeListList.add(dateTimes);

        final JaxbAnnotatedCollection<List<List<DateTime>>> unitUnderTest =
                new JaxbAnnotatedCollection<List<List<DateTime>>>(dateTimeListList);

        // Act
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(expectedClasses.size(), classInformation.size());
        for (Class<? extends Serializable> current : expectedClasses) {
            Assert.assertTrue(classInformation.contains(current.getName()));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnResurrectingNullValue() {

        // Assemble
        final JaxbAnnotatedCollection<List<String>> unitUnderTest = new JaxbAnnotatedCollection<List<String>>();

        // Act & Assert
        unitUnderTest.getValue();
    }
}
