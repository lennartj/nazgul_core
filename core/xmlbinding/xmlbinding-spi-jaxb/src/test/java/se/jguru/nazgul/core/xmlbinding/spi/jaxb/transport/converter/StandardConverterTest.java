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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.converter;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Test;
import se.jguru.nazgul.core.reflection.api.conversion.registry.DefaultConverterRegistry;
import se.jguru.nazgul.core.reflection.api.conversion.registry.PrioritizedTypeConverter;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedCollection;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedDateTime;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.type.JaxbAnnotatedNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardConverterTest {

    @Test
    public void validateConversionPairs() {

        // Assemble
        final List<Class<?>> expectedConverterTypes = Arrays.asList(
                JaxbAnnotatedCollection.class, JaxbAnnotatedDateTime.class, JaxbAnnotatedNull.class,
                Collection.class, DateTime.class, Object.class);
        final DefaultConverterRegistry registry = new DefaultConverterRegistry();
        final StandardConverters converters = new StandardConverters();

        final Map<Class<?>, PrioritizedTypeConverter> convertersMap = getConvertersMap(registry);

        // Act
        registry.add(converters);

        final Set<Class<?>> knownFromTypes = convertersMap.keySet();

        // Assert
        for (Class<?> current : expectedConverterTypes) {
            Assert.assertTrue("Class [" + current.getSimpleName() + "] was not a known FromType.",
                    knownFromTypes.contains(current));
        }
    }

    //
    // Private helpers
    //

    private Map<Class<?>, PrioritizedTypeConverter> getConvertersMap(final DefaultConverterRegistry instance) {

        try {
            Field mapField = DefaultConverterRegistry.class.getDeclaredField("sourceTypeToTypeConvertersMap");
            mapField.setAccessible(true);
            return (Map<Class<?>, PrioritizedTypeConverter>) mapField.get(instance);
        } catch (Exception e) {
            throw new IllegalArgumentException("could not acquire field.", e);
        }
    }
}
