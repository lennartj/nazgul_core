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


package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedDateTimeTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private DateTime value;

    @Before
    public void setupSharedState() {
        value = new DateTime(2012, 7, 6, 5, 4, 3, DateTimeZone.UTC);
    }

    @Test
    public void validateClassInformationAndUsage() {

        // Assemble

        // Act
        final JaxbAnnotatedDateTime unitUnderTest = new JaxbAnnotatedDateTime(value);
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(value, unitUnderTest.getValue());
        Assert.assertEquals(1, classInformation.size());

        Assert.assertTrue(classInformation.contains(DateTime.class.getName()));
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final DateTime valueCopy = new DateTime(2012, 7, 6, 5, 4, 3, DateTimeZone.UTC);
        final DateTime anotherValue = new DateTime(2012, 8, 7, 6, 5, 4, DateTimeZone.UTC);
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
}
