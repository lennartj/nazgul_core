/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
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
