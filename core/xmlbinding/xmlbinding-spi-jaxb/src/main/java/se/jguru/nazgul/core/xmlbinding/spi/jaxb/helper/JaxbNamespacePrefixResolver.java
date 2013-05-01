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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import se.jguru.nazgul.core.xmlbinding.api.DefaultNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.api.NamespacePrefixResolver;

import java.util.Map;
import java.util.Set;

/**
 * NamespacePrefixResolver adapter of a JAXB NamespacePrefixMapper implementation; all relevant
 * calls are delegated to an internal NamespacePrefixResolver instance.
 * Must extend the JAXB NamespacePrefixMapper class due to poor JAXB design.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbNamespacePrefixResolver extends NamespacePrefixMapper implements NamespacePrefixResolver {

    // Internal state
    private NamespacePrefixResolver namespacePrefixResolver;

    /**
     * Default constructor, using a DefaultNamespacePrefixResolver instance to which
     * all NamespacePrefixResolver calls are delegated.
     */
    public JaxbNamespacePrefixResolver() {
        this(new DefaultNamespacePrefixResolver());
    }

    /**
     * Compound constructor using the provided NamespacePrefixResolver instance to delegate all
     * possible calls dealing with XML namespace URI to prefix mappings.
     *
     * @param namespacePrefixResolver The NamespacePrefixResolver delegate to which all NamespacePrefixResolver
     *                                calls are delegated internally.
     */
    public JaxbNamespacePrefixResolver(final NamespacePrefixResolver namespacePrefixResolver) {

        Validate.notNull(namespacePrefixResolver, "Cannot handle null namespacePrefixResolver argument.");
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getPreferredPrefix(final String namespaceUri,
                                           final String suggestion,
                                           final boolean requirePrefix) {

        final String registeredPrefix = namespacePrefixResolver.getXmlPrefix(namespaceUri);
        if (registeredPrefix != null) {

            // The provided namespaceUri had a registered prefix. Return it.
            return registeredPrefix;
        }

        // Use the empty namespace if we are not required to return a prefix.
        return requirePrefix ? suggestion : NamespacePrefixResolver.EMPTY_NAMESPACE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {

        final Set<String> registeredNamespaceURIs = namespacePrefixResolver.getRegisteredNamespaceURIs();
        final String[] toReturn = new String[registeredNamespaceURIs.size()];

        return registeredNamespaceURIs.toArray(toReturn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String xmlNamespaceUri, final String xmlPrefix)
            throws NullPointerException, IllegalArgumentException {
        namespacePrefixResolver.put(xmlNamespaceUri, xmlPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<String, String> xmlUri2PrefixMap)
            throws NullPointerException, IllegalArgumentException {
        namespacePrefixResolver.putAll(xmlUri2PrefixMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNamespaceUri(final String xmlPrefix) throws NullPointerException {
        return namespacePrefixResolver.getNamespaceUri(xmlPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXmlPrefix(final String xmlNamespaceUri) throws NullPointerException {
        return namespacePrefixResolver.getXmlPrefix(xmlNamespaceUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getRegisteredNamespaceURIs() {
        return namespacePrefixResolver.getRegisteredNamespaceURIs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getRegisteredPrefixes() {
        return namespacePrefixResolver.getRegisteredPrefixes();
    }

    /**
     * {@inheritDoc}

    @Override
    public LSInput resolveResource(final String type,
                                   final String namespaceURI,
                                   final String publicId,
                                   final String systemId,
                                   final String baseURI) {

        return new LocalLSInput(type, namespaceURI, publicId, systemId, baseURI);
    }
     */
}
