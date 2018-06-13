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

import com.google.common.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbAnnotatedNullTest {

    @Test
    public void validateClassInformationAndUsage() {

        // Assemble
        final JaxbAnnotatedNull unitUnderTest = JaxbAnnotatedNull.getInstance();

        // Act
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(0, classInformation.size());
        Assert.assertEquals(new JaxbAnnotatedNull(), unitUnderTest);
        Assert.assertNotSame(new JaxbAnnotatedNull(), unitUnderTest);
    }

    @Test
    public void validateComparisonAndEquality() {

        // Assemble
        final JaxbAnnotatedNull unitUnderTest = JaxbAnnotatedNull.getInstance();

        // Act
        final boolean result1 = unitUnderTest.compareTo(null) == 0;
        final boolean result2 = unitUnderTest.compareTo(new JaxbAnnotatedNull()) == 0;
        final boolean result3 = unitUnderTest.compareTo(new Date()) == 0;

        // Assert
        Assert.assertEquals(new JaxbAnnotatedNull().hashCode(), unitUnderTest.hashCode());
        Assert.assertTrue(result1);
        Assert.assertTrue(result2);
        Assert.assertFalse(result3);
    }

    @Test
    public void validateTypeTokenUsage() {

        // Assemble

        // Act
        final Foo<String, ArrayList> foo = new Foo<String, ArrayList>() {
        };

        // Assert
        Assert.assertEquals(String.class.getName(), ((Class) foo.typeA.getType()).getName());
        Assert.assertEquals(ArrayList.class.getName(), "" + foo.typeB);
    }

    class Foo<A, B> {
        TypeToken<A> typeA = new TypeToken<A>(getClass()) {
        };
        TypeToken<B> typeB = new TypeToken<B>(getClass()) {
        };
    }
}
