/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.jguru.nazgul.core.xmlbinding.api;

import java.util.Map;
import java.util.Set;

/**
 * <p>Specification for how to map XML Namespace URIs to XML Namespace Prefixes.
 * XML namespaces are provided in the form of an URI-formatted String, such as
 * {@code http://www.jguru.se/nazgul/core}, and XML prefixes are simply identifiers
 * for these URIs (such as {@code core}).</p>
 * <p>The intention for this NamespacePrefixResolver is to inform the XML binder instance
 * of the relation between the namespace URI and prefix - illustrated below by the mapping
 * of the {@code http://foo/bar} namespace to the elements ParentElement and ChildElement.
 * Some XmlBinder implementations [notably JAXB] have rather sketchy implementations of
 * these mechanics, so we provide this NamespacePrefixResolver specification to provide
 * a unified specification for relating XML namespace URLs to Prefixes.</p>
 * <p>Example (desired output):</p>
 * <pre>
 *     &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
 *     &lt;foobar:ParentElement xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *         <strong>xmlns:foobar="http://foo/bar"</strong>
 *         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
 *
 *         &lt;foobar:ChildElement ... /&gt;
 *
 *         ...
 *     &lt;/foobar:ParentElement&gt;
 * </pre>
 * <p>The corresponding code to relate the {@code http://foo/bar} URI to the {@code foobar}
 * prefix is shown below:</p>
 * <pre>
 *     // Acquire the NamespacePrefixResolver in use by the XmlBinder
 *     XmlBinder binder = ...
 *     NamespacePrefixResolver resolver = binder.getNamespacePrefixResolver();
 *
 *     // Relate the URI to the desired prefix.
 *     resolver.put("http://foo/bar", "foobar");
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface NamespacePrefixResolver {

    /**
     * Empty/undeterminate namespace URI.
     */
    String EMPTY_NAMESPACE = "";

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
