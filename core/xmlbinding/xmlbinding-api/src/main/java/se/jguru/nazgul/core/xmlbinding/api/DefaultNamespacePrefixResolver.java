/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
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
package se.jguru.nazgul.core.xmlbinding.api;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Default POJO implementation of the NamespacePrefixResolver specification,
 * relating unique XML namespace URIs to unique XML prefixes.
 * Implemented for runtime lookup speed rather than minimal memory footprint.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see NamespacePrefixResolver
 */
public class DefaultNamespacePrefixResolver implements NamespacePrefixResolver {

    // Internal state
    private final Object lock = new Object();
    private SortedMap<String, String> url2PrefixMap = new TreeMap<String, String>();
    private SortedMap<String, String> prefix2UrlMap = new TreeMap<String, String>();

    /**
     * Default constructor, mapping in all URI to Prefix pairs defined within the WellKnownNamespace enum.
     */
    public DefaultNamespacePrefixResolver() {

        for (WellKnownNamespace current : WellKnownNamespace.values()) {
            put(current.getNameSpaceUrl(), current.getXsdPrefix());
        }
    }

    /**
     * Adds the provided mapping between a single xmlNamespaceUri and corresponding xmlPrefix.
     *
     * @param xmlNamespaceUri the unique URI of an XML namespace.
     * @param xmlPrefix       the unique prefix of the provided XML namespace.
     * @throws NullPointerException     if any argument was {@code null}.
     * @throws IllegalArgumentException if the xmlNamespaceUri was already registered to another prefix, or
     *                                  if any argument was empty.
     */
    @Override
    public final void put(final String xmlNamespaceUri, final String xmlPrefix)
            throws NullPointerException, IllegalArgumentException {

        // Check sanity
        Validate.notNull(xmlNamespaceUri, "Cannot handle null xmlNamespaceUri argument.");
        Validate.notEmpty(xmlPrefix, "Cannot handle null or empty xmlPrefix argument.");

        synchronized (lock) {
            validateNotRegistered(xmlNamespaceUri, xmlPrefix);
            url2PrefixMap.put(xmlNamespaceUri, xmlPrefix);
            prefix2UrlMap.put(xmlPrefix, xmlNamespaceUri);
        }
    }

    /**
     * Convenience method, adding all provided mappings between xmlNamespaceUris and corresponding xmlPrefixes.
     *
     * @param xmlUri2PrefixMap A non-null map relating XML namespace URIs to corresponding prefixes.
     * @throws NullPointerException     if any argument or element was {@code null}.
     * @throws IllegalArgumentException if any xmlNamespaceUri was already registered to another prefix, or
     *                                  if any argument was empty.
     */
    @Override
    public final void putAll(final Map<String, String> xmlUri2PrefixMap)
            throws NullPointerException, IllegalArgumentException {

        Validate.notNull(xmlUri2PrefixMap, "Cannot handle null uri2PrefixMap argument.");

        synchronized (lock) {

            // Validate the provided xmlUri2PrefixMap
            for(Map.Entry<String, String> current : xmlUri2PrefixMap.entrySet()) {

                // Check sanity
                Validate.notEmpty(current.getKey(), "Cannot handle null or empty xmlNamespaceUri.");
                Validate.notEmpty(current.getValue(), "Cannot handle null or empty prefix.");

                validateNotRegistered(current.getKey(), current.getValue());
            }

            // Add all key <-> value entries
            for(Map.Entry<String, String> current : xmlUri2PrefixMap.entrySet()) {
                put(current.getKey(), current.getValue());
            }
        }
    }

    /**
     * Retrieves the XML namespace URI for the provided xmlPrefix, or {@code null} if none was found.
     *
     * @param xmlPrefix The XML prefix for which to obtain the corresponding XML namespace.
     * @return the XML namespace URI for the provided xmlPrefix, or {@code null} if none was found.
     * @throws NullPointerException if the xmlPrefix argument was {@code null}.
     */
    @Override
    public final String getNamespaceUri(final String xmlPrefix) throws NullPointerException {

        Validate.notNull(xmlPrefix, "Cannot handle null xmlPrefix argument.");
        return prefix2UrlMap.get(xmlPrefix);
    }

    /**
     * Retrieves the XML prefix for the provided xmlNamespaceUri, or {@code null} if none was found.
     *
     * @param xmlNamespaceUri The XML namespace URI for which to obtain the corresponding XML prefix.
     * @return the XML prefix for the provided xmlNamespaceUri, or {@code null} if none was found.
     * @throws NullPointerException if the xmlPrefix argument was {@code null}.
     */
    @Override
    public final String getXmlPrefix(final String xmlNamespaceUri) throws NullPointerException {

        Validate.notNull(xmlNamespaceUri, "Cannot handle null xmlNamespaceUri argument.");
        return url2PrefixMap.get(xmlNamespaceUri);
    }

    /**
     * @return A non-modifiable List holding all currently registered XML namespaceURIs.
     */
    @Override
    public final Set<String> getRegisteredNamespaceURIs() {
        return Collections.unmodifiableSet(url2PrefixMap.keySet());
    }

    /**
     * @return A non-modifiable List holding all currently registered XML prefixes.
     */
    @Override
    public final Set<String> getRegisteredPrefixes() {
        return Collections.unmodifiableSet(prefix2UrlMap.keySet());
    }

    //
    // Private helpers
    //

    private void validateNotRegistered(final String xmlNamespaceUri,
                                       final String xmlPrefix)
            throws IllegalArgumentException {

        final String presentPrefix = url2PrefixMap.get(xmlNamespaceUri);
        final String presentNamespaceUri = prefix2UrlMap.get(xmlPrefix);

        // We already know that xmlNamespaceUri is not null.
        if (presentPrefix != null && !presentPrefix.equals(xmlPrefix)) {
            throw new IllegalArgumentException("URI [" + xmlNamespaceUri + "] already present with prefix ["
                    + xmlPrefix + "]. Aborting put operation.");
        }

        if (presentNamespaceUri != null && !presentNamespaceUri.equals(xmlNamespaceUri)) {
            throw new IllegalArgumentException("URI [" + xmlPrefix + "] already present with namespace URI ["
                    + presentNamespaceUri + "]. Aborting put operation.");
        }
    }
}
