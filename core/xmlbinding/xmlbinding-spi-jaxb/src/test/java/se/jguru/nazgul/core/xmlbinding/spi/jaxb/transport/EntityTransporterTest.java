/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.types.Person;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EntityTransporterTest {

    @Before
    @After
    public void restoreTransportTypeConverterRegistry() {
        EntityTransporter.setTransportTypeConverterRegistry(new DefaultJaxbConverterRegistry());
    }

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
    public void validateManagingJaxbConverterRegistry() {

        // Assemble
        final JaxbConverterRegistry mockRegistry = getRegistry();

        // Act
        final JaxbConverterRegistry originalRegistry = EntityTransporter.getRegistry();
        EntityTransporter.setTransportTypeConverterRegistry(mockRegistry);
        final JaxbConverterRegistry afterSetRegistry = EntityTransporter.getRegistry();

        // Assert
        Assert.assertTrue(originalRegistry instanceof DefaultJaxbConverterRegistry);
        Assert.assertNotSame(originalRegistry, afterSetRegistry);
    }

    //
    // Private helpers
    //

    JaxbConverterRegistry getRegistry() {
        return new JaxbConverterRegistry() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addConverters(Object... converters) throws IllegalArgumentException {
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <TransportType, OriginalType> Class<TransportType> getTransportType(Class<OriginalType> originalType) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <OriginalType, TransportType> Class<OriginalType> getOriginalType(Class<TransportType> transportType) throws IllegalArgumentException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <OriginalType, TransportType> TransportType packageForTransport(OriginalType source) throws IllegalArgumentException {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <OriginalType, TransportType> OriginalType resurrectAfterTransport(TransportType toConvert) throws IllegalArgumentException {
                return null;
            }
        };
    }
}
