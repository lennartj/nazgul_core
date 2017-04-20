/*
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-api
 * %%
 * Copyright (C) 2010 - 2017 jGuru Europe AB
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 *
 */
package se.jguru.nazgul.core.xmlbinding.api;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
     * {@inheritDoc}
     */
    @Override
    public final void put(@NotNull @Size(min = 1) final String xmlNamespaceUri,
                          @NotNull @Size(min = 1) final String xmlPrefix)
            throws NullPointerException, IllegalArgumentException {

        // Check sanity
        Validate.notNull(xmlNamespaceUri, "xmlNamespaceUri");
        Validate.notEmpty(xmlPrefix, "xmlPrefix");

        synchronized (lock) {
            validateNotRegistered(xmlNamespaceUri, xmlPrefix);
            url2PrefixMap.put(xmlNamespaceUri, xmlPrefix);
            prefix2UrlMap.put(xmlPrefix, xmlNamespaceUri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void putAll(@NotNull final Map<String, String> xmlUri2PrefixMap)
            throws NullPointerException, IllegalArgumentException {

        Validate.notNull(xmlUri2PrefixMap, "uri2PrefixMap");

        synchronized (lock) {

            // Validate the provided xmlUri2PrefixMap
            for (Map.Entry<String, String> current : xmlUri2PrefixMap.entrySet()) {

                // Check sanity
                Validate.notEmpty(current.getKey(), "xmlNamespaceUri");
                Validate.notEmpty(current.getValue(), "prefix");

                validateNotRegistered(current.getKey(), current.getValue());
            }

            // Add all key <-> value entries
            for (Map.Entry<String, String> current : xmlUri2PrefixMap.entrySet()) {
                put(current.getKey(), current.getValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getNamespaceUri(@NotNull final String xmlPrefix) throws NullPointerException {

        Validate.notNull(xmlPrefix, "xmlPrefix");
        return prefix2UrlMap.get(xmlPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getXmlPrefix(@NotNull final String xmlNamespaceUri) throws NullPointerException {

        Validate.notNull(xmlNamespaceUri, "xmlNamespaceUri");
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
