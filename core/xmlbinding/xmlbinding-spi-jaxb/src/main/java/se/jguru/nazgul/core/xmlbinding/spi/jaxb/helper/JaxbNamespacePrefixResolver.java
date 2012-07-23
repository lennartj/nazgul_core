/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */
package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import se.jguru.nazgul.core.xmlbinding.api.DefaultNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.api.NamespacePrefixResolver;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

/**
 * NamespacePrefixResolver adapter of a JAXB NamespacePrefixMapper implementation; all relevant
 * calls are delegated to an internal NamespacePrefixResolver instance.
 * Must extend the JAXB NamespacePrefixMapper class due to poor JAXB design.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbNamespacePrefixResolver extends NamespacePrefixMapper
        implements NamespacePrefixResolver, LSResourceResolver {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(JaxbNamespacePrefixResolver.class);

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
     */
    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, final String publicId,
                                   final String systemId, final String baseURI) {

        return new LocalLSInput(type, namespaceURI, publicId, systemId, baseURI);
    }

    //
    // Private helpers
    //

    /**
     * Local implementation of the LSInput data source.
     *
     * @see LSInput
     */
    class LocalLSInput implements LSInput {

        // Internal state
        private String type;
        private String namespaceURI;
        private String publicId;
        private String systemId;
        private String baseURI;

        /**
         * Creates a new LocalLSInput instance using the given properties.
         *
         * @param type         The type of the resource being resolved.
         *                     For XML [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>] resources
         *                     (i.e. entities), applications must use the value
         *                     <code>"http://www.w3.org/TR/REC-xml"</code>. For XML Schema
         *                     [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>],
         *                     applications must use the value
         *                     <code>"http://www.w3.org/2001/XMLSchema"</code>. Other types of
         *                     resources are outside the scope of this specification and therefore
         *                     should recommend an absolute URI in order to use this method.
         * @param namespaceURI The namespace of the resource being resolved,
         *                     e.g. the target namespace of the XML Schema
         *                     [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>]
         *                     when resolving XML Schema resources.
         * @param publicId     The public identifier of the external entity being
         *                     referenced, or <code>null</code> if no public identifier was
         *                     supplied or if the resource is not an entity.
         * @param systemId     The system identifier, a URI reference
         *                     [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], of the
         *                     external resource being referenced, or <code>null</code> if no
         *                     system identifier was supplied.
         * @param baseURI      The absolute base URI of the resource being parsed, or
         *                     <code>null</code> if there is no base URI
         */
        private LocalLSInput(final String type,
                             final String namespaceURI,
                             final String publicId,
                             final String systemId,
                             final String baseURI) {

            // Assign internal state
            this.type = type;
            this.namespaceURI = namespaceURI;
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseURI = baseURI;

            JaxbNamespacePrefixResolver.log.info(toString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "LocalLSInput: {type=" + type + ", namespaceURI=" + namespaceURI + ", publicId=" + publicId
                    + ", systemId=" + systemId + ", baseURI=" + baseURI + "}";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Reader getCharacterStream() {
            return null;
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setCharacterStream(final Reader characterStream) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting CharacterStream.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public InputStream getByteStream() {
            throw new UnsupportedOperationException("LocalLSInput does not support getting a ByteStream.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setByteStream(final InputStream byteStream) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting a ByteStream.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getStringData() {
            return null;
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setStringData(final String stringData) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting StringData.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public String getSystemId() {
            throw new UnsupportedOperationException("LocalLSInput does not support getting SystemId.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setSystemId(String systemId) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting SystemId.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public String getPublicId() {
            throw new UnsupportedOperationException("LocalLSInput does not support getting PublicId.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setPublicId(String publicId) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting PublicId.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public String getBaseURI() {
            throw new UnsupportedOperationException("LocalLSInput does not support getting BaseURI.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setBaseURI(final String baseURI) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting BaseURI.");
        }

        /**
         * {@inheritDoc}
         *
         * @return "UTF-8"
         */
        @Override
        public String getEncoding() {
            return "UTF-8";
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setEncoding(final String encoding) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting encoding.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public boolean getCertifiedText() {
            throw new UnsupportedOperationException("LocalLSInput does not support getting CertifiedText.");
        }

        /**
         * <strong>Throws UnsupportedOperationException.</strong>
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void setCertifiedText(boolean certifiedText) {
            throw new UnsupportedOperationException("LocalLSInput does not support setting CertifiedText.");
        }
    }
}
