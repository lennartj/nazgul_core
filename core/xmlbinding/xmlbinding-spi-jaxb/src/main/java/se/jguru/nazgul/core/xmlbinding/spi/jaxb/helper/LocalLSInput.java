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
 *       http://www.jguru.se/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.apache.commons.lang3.Validate;
import org.w3c.dom.ls.LSInput;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Local implementation of the LSInput data source.
 *
 * @see org.w3c.dom.ls.LSInput
 */
public class LocalLSInput implements LSInput {

    // Internal state
    private String type;
    private String namespaceURI;
    private String publicId;
    private String systemId;
    private String baseURI;
    private String encoding = "UTF-8";

    private static Map<String, String> namespaceToSchemaURI = new HashMap<String, String>();

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
    public LocalLSInput(final String type,
                        final String namespaceURI,
                        final String publicId,
                        final String systemId,
                        final String baseURI) {

        // Check sanity
        Validate.notNull(publicId, "Cannot handle null publicId argument.");
        Validate.notNull(systemId, "Cannot handle null systemId argument.");

        // Assign internal state
        this.type = type;
        this.namespaceURI = namespaceURI;
        this.publicId = publicId;
        this.baseURI = baseURI;
        this.systemId = systemId;
    }

    /**
     * Adds a namespace to schema entry.
     *
     * @param namespaceURI The namespace URI.
     * @param schema       The XSD string to.
     */
    public void addSchema(final String namespaceURI, final String schema) {

        // Check sanity
        Validate.notNull(namespaceURI, "Cannot handle null namespaceURI argument.");
        Validate.notNull(schema, "Cannot handle null schema argument.");
        namespaceToSchemaURI.put(namespaceURI, schema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LocalLSInput: {type=" + type + ", namespaceURI=" + namespaceURI + ", publicId=" + publicId
                + ", systemId=" + systemId + ", baseURI=" + baseURI + "} <=> " + namespaceToSchemaURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getCharacterStream() {
        // Get the schema for the current namespace.
        return new StringReader(namespaceToSchemaURI.get(namespaceURI));
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
     * {@inheritDoc}
     */
    @Override
    public InputStream getByteStream() {
        // Get the schema for the current namespace.
        String schema = namespaceToSchemaURI.get(namespaceURI) == null ? "" : namespaceToSchemaURI.get(namespaceURI);
        return new ByteArrayInputStream(schema.getBytes());
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
        return namespaceToSchemaURI.get(namespaceURI);
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
     * {@inheritDoc}
     */
    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemId(final String systemId) {
        Validate.notNull(systemId, "Cannot handle null systemId argument.");
        this.systemId = systemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPublicId() {
        return publicId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPublicId(final String publicId) {
        Validate.notNull(publicId, "Cannot handle null publicId argument.");
        this.publicId = publicId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseURI() {
        return baseURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBaseURI(final String baseURI) {
        this.baseURI = baseURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEncoding() {
        return encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEncoding(final String encoding) {
        Validate.notNull(encoding, "Cannot handle null encoding argument.");
        this.encoding = encoding;
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
    public void setCertifiedText(final boolean certifiedText) {
        throw new UnsupportedOperationException("LocalLSInput does not support setting CertifiedText.");
    }
}
