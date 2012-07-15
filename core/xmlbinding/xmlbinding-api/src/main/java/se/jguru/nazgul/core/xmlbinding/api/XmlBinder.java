/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.api;

import java.io.Reader;

/**
 * Specification for XmlBinding, implying conversion of Java object graphs to XML formatted strings and vice versa.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface XmlBinder {

    /**
     * Foundation XSD namespace URI.
     */
    String FOUNDATION_NAMESPACE = "http://www.jguru.se/nazgul/foundation";

    /**
     * XSD instance namespace URI.
     */
    String XSD_INSTANCE_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * XSD default namespace URI.
     */
    String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    /**
     * Converts the provided source java object to an XML formatted String, written to a Reader.
     *
     * @param source A java object graph which should be converted to XML form.
     * @param <R>    The reader result type.
     * @param <S>    The java object graph type.
     * @return A Reader wrapping the XML acquired from marshaling the provided source object graph.
     */
    <R extends Reader, S> R toXml(S source);

    /**
     * Re-creates a java object graph from the XML in the provided reader.
     *
     * @param xmlReader The reader from which the XML data should be read.
     * @param <R>       The Reader type.
     * @param <S>       The java object graph type.
     * @return A resurrected java object graph.
     */
    <S extends Reader, R> R fromXml(S xmlReader);

    /**
     * Convenience method returning a String instead of a Reader.
     * Converts the provided source java object to an XML formatted String, written to a Reader.
     *
     * @param source A java object graph which should be converted to XML form.
     * @param <S>    The java object graph type.
     * @return An XML formatted string, acquired from marshaling the provided source object graph.
     */
    <S> String toXml(S source);

    /**
     * Convenience method using a String instead of a Reader.
     * Re-creates a java object graph from the provided XML formatted String.
     *
     * @param xmlFormattedString The XML formatted string which should be converted to a java object graph.
     * @param <R>                The resulting type of the java object graph.
     * @return A re-created java object graph.
     */
    <R> R fromXml(String xmlFormattedString);
}
