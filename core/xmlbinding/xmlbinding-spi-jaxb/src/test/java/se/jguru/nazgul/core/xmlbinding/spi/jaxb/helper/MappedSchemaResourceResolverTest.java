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

import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MappedSchemaResourceResolverTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullNamespace() {

        // Assemble
        final MappedSchemaResourceResolver unitUnderTest = new MappedSchemaResourceResolver();

        // Act & Assert
        unitUnderTest.addNamespace2SchemaEntry(null, "irrelevant");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullSchema() {

        // Assemble
        final MappedSchemaResourceResolver unitUnderTest = new MappedSchemaResourceResolver();

        // Act & Assert
        unitUnderTest.addNamespace2SchemaEntry("irrelevant", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingEmptySchema() {

        // Assemble
        final MappedSchemaResourceResolver unitUnderTest = new MappedSchemaResourceResolver();

        // Act & Assert
        unitUnderTest.addNamespace2SchemaEntry("irrelevant", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnOverwritingExistingNamespace() {

        // Assemble
        final MappedSchemaResourceResolver unitUnderTest = new MappedSchemaResourceResolver();
        final String namespace = XmlBinder.CORE_NAMESPACE;

        // Act & Assert
        unitUnderTest.addNamespace2SchemaEntry(namespace, "someSchema");
        unitUnderTest.addNamespace2SchemaEntry(namespace, "anotherSchema");
    }
}
