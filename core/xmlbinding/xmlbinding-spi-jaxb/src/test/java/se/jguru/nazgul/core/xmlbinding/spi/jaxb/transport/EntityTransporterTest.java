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
