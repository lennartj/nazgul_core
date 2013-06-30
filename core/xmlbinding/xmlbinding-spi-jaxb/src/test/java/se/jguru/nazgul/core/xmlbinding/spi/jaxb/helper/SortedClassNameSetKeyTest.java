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

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Account;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SortedClassNameSetKeyTest {

    // Shared state
    private SortedSet<String> threeClassNames;
    private SortedClassNameSetKey unitUnderTest;

    @Before
    public void setupSharedState() {

        threeClassNames = new TreeSet<String>();
        threeClassNames.add(Person.class.getName());
        threeClassNames.add(Account.class.getName());
        threeClassNames.add(EntityTransporter.class.getName());

        unitUnderTest = new SortedClassNameSetKey(threeClassNames);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullClassNameSet() {

        // Act & Assert
        new SortedClassNameSetKey(null);
    }

    @Test
    public void validateEqualityAndHashcode() {

        // Assemble
        final SortedSet<String> someClassNames = new TreeSet<String>();
        someClassNames.add(Person.class.getName());
        someClassNames.add(Account.class.getName());

        final SortedClassNameSetKey unitUnderTest2 = new SortedClassNameSetKey(someClassNames);

        // Act
        final boolean result = unitUnderTest.equals(unitUnderTest2);
        final int hashcode1 = unitUnderTest.hashCode();
        final int hashcode2 = unitUnderTest2.hashCode();

        // Assert
        Assert.assertEquals(result, unitUnderTest.toString().equals(unitUnderTest2.toString()));
        Assert.assertEquals(hashcode1, unitUnderTest.toString().hashCode());
        Assert.assertEquals(hashcode2, unitUnderTest2.toString().hashCode());

        Assert.assertFalse(unitUnderTest.equals(null));
        Assert.assertFalse(unitUnderTest.equals("fooBar"));
    }

    @Test
    public void validateComparability() {

        // Assemble
        final SortedSet<String> someClassNames = new TreeSet<String>();
        someClassNames.add(Person.class.getName());
        someClassNames.add(Account.class.getName());

        final SortedClassNameSetKey unitUnderTest2 = new SortedClassNameSetKey(threeClassNames);
        final SortedClassNameSetKey unitUnderTest3 = new SortedClassNameSetKey(someClassNames);

        // Act
        final int result1 = unitUnderTest.compareTo(unitUnderTest2);
        final int result2 = unitUnderTest.compareTo(unitUnderTest3);

        // Assert
        Assert.assertEquals(Integer.MIN_VALUE, unitUnderTest.compareTo(null));
        Assert.assertEquals(0, result1);
        Assert.assertEquals(unitUnderTest.toString().compareTo(unitUnderTest2.toString()), result1);
        Assert.assertEquals(unitUnderTest.toString().compareTo(unitUnderTest3.toString()), result2);

        Assert.assertEquals(unitUnderTest, unitUnderTest2);
        Assert.assertNotSame(unitUnderTest, unitUnderTest2);
    }

    @Test
    public void validateContainability() {

        // Assemble
        final SortedSet<String> someClassNames = new TreeSet<String>();
        someClassNames.add(Person.class.getName());
        someClassNames.add(Account.class.getName());

        final SortedSet<String> someOtherClassNames = new TreeSet<String>();
        someOtherClassNames.add(Person.class.getName());
        someOtherClassNames.add(Account.class.getName());
        someOtherClassNames.add(String.class.getName());

        final SortedClassNameSetKey unitUnderTest2 = new SortedClassNameSetKey(someOtherClassNames);

        // Act
        final boolean result1 = unitUnderTest.containsAll(someClassNames);
        final boolean result2 = unitUnderTest.containsAll(someOtherClassNames);
        final boolean result4 = unitUnderTest2.containsAll(someClassNames);

        // Assert
        Assert.assertTrue(result1);
        Assert.assertFalse(result2);
        Assert.assertTrue(result4);
    }
}
