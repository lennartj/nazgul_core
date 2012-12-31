/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.api;

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import java.io.Reader;
import java.util.List;

/**
 * Specification for XmlBinding, implying conversion of Java object graphs to XML formatted strings and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface XmlBinder<T> {

    /**
     * Nazgul core XSD namespace URI.
     */
    String CORE_NAMESPACE = "http://www.jguru.se/nazgul/core";

    /**
     * XSD instance namespace URI.
     */
    String XSD_INSTANCE_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * XSD default namespace URI.
     */
    String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    /**
     * Retrieves the NamespacePrefixResolver instance in use by this XmlBinder.
     *
     * @return the NamespacePrefixResolver instance in use by this XmlBinder,
     *         or {@code null} should no NamespacePrefixResolver be used by this
     *         XmlBinder.
     */
    NamespacePrefixResolver getNamespacePrefixResolver();

    /**
     * Converts/marshals the provided source java objects to an XML formatted String.
     * Order between the provided objects is preserved in the resulting XML.
     *
     * @param toConvert The java objects to convert to an XML formatted String.
     * @return An XML representation of the provided javaObjects.
     * @throws IllegalArgumentException If the conversion could not be completed successfully.
     * @throws InternalStateValidationException
     *                                  if the Java Object Graph contained Validatable objects
     *                                  that did not pass validation.
     */
    String marshal(T... toConvert) throws IllegalArgumentException, InternalStateValidationException;

    /**
     * Unmarshals the XML formatted string from the provided transportReader into a Java object graph
     * found within the transportReader.
     *
     * @param transportReader The Reader holding a single XML-formatted String being converted by the marshal
     *                        method in an XmlBinder of the same internal implementation as this one.
     * @return A fully unmarshalled List holding clones of the original objects written to the stream.
     * @throws IllegalArgumentException If the object graph could not be properly resurrected.
     * @throws InternalStateValidationException
     *                                  if any object resurrected was a Validatable which did not pass validation.
     */
    List<T> unmarshal(Reader transportReader) throws IllegalArgumentException, InternalStateValidationException;

    /**
     * Convenience {@code unmarshal} method which unmarshals a single object instance from XML to java.
     * Reads the XML formatted string from the provided transportReader, and resurrects the object
     * found within the transportReader.
     *
     * @param transportReader The Reader holding a single XML-formatted String being converted by the
     *                        marshal method in an XmlBinder of the same internal implementation type as this one.
     * @return A fully unmarshalled instance clone of the original object written to the stream.
     * @throws IllegalArgumentException If the object graph could not be properly resurrected.
     * @throws InternalStateValidationException
     *                                  if any object resurrected was a Validatable which did not pass validation.
     */
    <S> S unmarshalInstance(Reader transportReader) throws IllegalArgumentException,
            InternalStateValidationException;
}
