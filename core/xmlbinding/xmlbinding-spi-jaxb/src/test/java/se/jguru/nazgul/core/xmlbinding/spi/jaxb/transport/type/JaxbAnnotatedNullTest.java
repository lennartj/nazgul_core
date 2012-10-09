/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type;

import com.google.common.reflect.TypeToken;
import junit.framework.Assert;
import org.junit.Test;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        final Foo<String, ArrayList> foo = new Foo<String, ArrayList>(){};

        // Assert
        Assert.assertEquals(String.class.getName(), ((Class) foo.typeA.getType()).getName());
        Assert.assertEquals(ArrayList.class.getName(), "" + foo.typeB);
    }

    class Foo<A, B> {
        TypeToken<A> typeA = new TypeToken<A>(getClass()) {};
        TypeToken<B> typeB = new TypeToken<B>(getClass()) {};
    }
}
