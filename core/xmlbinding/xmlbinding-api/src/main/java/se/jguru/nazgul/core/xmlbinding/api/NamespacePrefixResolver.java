/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.api;

import java.util.Map;
import java.util.Set;

/**
 * Specification for how to map XML Namespace URIs to XML Namespace Prefixes.
 * XML namespaces are provided in the form of an URI-formatted String, such as
 * {@code http://www.jguru.se/nazgul/core}, and XML prefixes are simply identifiers
 * for these URIs (such as {@code core}).
 * <p/>
 * The intention for this NamespacePrefixResolver is to inform the XML binder instance
 * of the relation between the namespace URI and prefix - illustrated below by the mapping
 * of the Nazul core namespace to the elements ParentElement and ChildElement. This is not
 * done by default by some XmlBinder implementations, so we provide this NamespacePrefixResolver
 * specification to facilitate XmlBinding for such implementations.
 * <p/>
 * <pre>
 *     &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
 *         &lt;core:ParentElement xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *         xmlns:core="http://www.jguru.se/nazgul/core"
 *         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
 *
 *         &lt;core:ChildElement ... /&gt;
 *
 *         ...
 *     &lt;/core:ParentElement&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface NamespacePrefixResolver {

    /**
     * Empty/undeterminate namespace URI.
     */
    public static final String EMPTY_NAMESPACE = "";

    /**
     * Adds the provided mapping between a single xmlNamespaceUri and corresponding xmlPrefix.
     *
     * @param xmlNamespaceUri the unique URI of an XML namespace.
     * @param xmlPrefix       the unique prefix of the provided XML namespace.
     * @throws NullPointerException     if any argument was {@code null}.
     * @throws IllegalArgumentException if the xmlNamespaceUri was already registered to another prefix, or
     *                                  if any argument was empty.
     */
    void put(String xmlNamespaceUri, String xmlPrefix) throws NullPointerException, IllegalArgumentException;

    /**
     * Convenience method, adding all provided mappings between xmlNamespaceUris and corresponding xmlPrefixes.
     *
     * @param xmlUri2PrefixMap A non-null map relating XML namespace URIs to corresponding prefixes.
     * @throws NullPointerException     if any argument or element was {@code null}.
     * @throws IllegalArgumentException if any xmlNamespaceUri was already registered to another prefix, or
     *                                  if any argument was empty.
     */
    void putAll(Map<String, String> xmlUri2PrefixMap) throws NullPointerException, IllegalArgumentException;

    /**
     * Retrieves the XML namespace URI for the provided xmlPrefix, or {@code null} if none was found.
     *
     * @param xmlPrefix The XML prefix for which to obtain the corresponding XML namespace.
     * @return the XML namespace URI for the provided xmlPrefix, or {@code null} if none was found.
     * @throws NullPointerException if the xmlPrefix argument was {@code null}.
     */
    String getNamespaceUri(String xmlPrefix) throws NullPointerException;

    /**
     * Retrieves the XML prefix for the provided xmlNamespaceUri, or {@code null} if none was found.
     *
     * @param xmlNamespaceUri The XML namespace URI for which to obtain the corresponding XML prefix.
     * @return the XML prefix for the provided xmlNamespaceUri, or {@code null} if none was found.
     * @throws NullPointerException if the xmlPrefix argument was {@code null}.
     */
    String getXmlPrefix(String xmlNamespaceUri) throws NullPointerException;

    /**
     * @return A non-modifiable List holding all currently registered XML namespaceURIs.
     */
    Set<String> getRegisteredNamespaceURIs();

    /**
     * @return A non-modifiable List holding all currently registered XML prefixes.
     */
    Set<String> getRegisteredPrefixes();
}
