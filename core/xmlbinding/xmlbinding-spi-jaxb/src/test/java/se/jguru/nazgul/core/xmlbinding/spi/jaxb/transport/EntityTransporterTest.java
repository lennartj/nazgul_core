/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import junit.framework.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EntityTransporterTest {

    @Test
    public void validateAddingTypedSingleObject() {

        // Assemble
        final Person toSerialize = new Person("Malin", 478);

        // Act
        final EntityTransporter<Person> unitUnderTest = new EntityTransporter<Person>(toSerialize);
        final List<Person> items = unitUnderTest.getItems();
        final SortedSet<String> classInformation = unitUnderTest.getClassInformation();

        // Assert
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(2, classInformation.size());

        Assert.assertTrue(classInformation.contains(EntityTransporter.class.getName()));
        Assert.assertTrue(classInformation.contains(Person.class.getName()));
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnSettingNullTypeConverterRegistry() {

        // Act & Assert
        EntityTransporter.setTransportTypeConverterRegistry(null);
    }

    @Test
    public void validateManagingTypeConverterRegistry() {

        // Assemble
        final TransportTypeConverterRegistry mockRegistry = getRegistry();

        // Act
        final TransportTypeConverterRegistry originalRegistry = EntityTransporter.getRegistry();
        EntityTransporter.setTransportTypeConverterRegistry(mockRegistry);
        final TransportTypeConverterRegistry afterSetRegistry = EntityTransporter.getRegistry();

        // Assert
        Assert.assertTrue(originalRegistry instanceof DefaultTransportTypeConverterRegistry);
        Assert.assertNotSame(originalRegistry, afterSetRegistry);
    }

    //
    // Private helpers
    //

    TransportTypeConverterRegistry getRegistry() {
        return new TransportTypeConverterRegistry() {
            @Override
            public void addTransportTypeConverter(TransportTypeConverter toAdd) throws IllegalArgumentException {
            }

            @Override
            public TransportTypeConverter getPackagingTransportTypeConverter(Object instance) {
                return null;
            }

            @Override
            public TransportTypeConverter getRevivingTypeConverter(Object instance) {
                return null;
            }
        };
    }
}
