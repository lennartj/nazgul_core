/*-
 * #%L
 * Nazgul Project: nazgul-core-xmlbinding-spi-jaxb
 * %%
 * Copyright (C) 2010 - 2018 jGuru Europe AB
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

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.InputStream;
import java.io.Reader;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * XML Schema resolver, internalizing all resolveResource calls to registered namespaces.
 * The map relating [registered] namespaces to XML Schema is wrapped inside this
 * MappedSchemaResourceResolver.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MappedSchemaResourceResolver implements LSResourceResolver {

    // Internal state
    private SortedMap<String, String> namespace2SchemaSnippetMap = new TreeMap<String, String>();

    /**
     * Adds a mapping between the supplied namespace and the given xmlSchemaSnippet.
     *
     * @param namespace        The non-null namespace which should be mapped.
     * @param xmlSchemaSnippet The non-empty xml Schema snippet which should be mapped.
     */
    public void addNamespace2SchemaEntry(@NotNull final String namespace,
                                         @NotNull @Size(min = 1) final String xmlSchemaSnippet) {

        // Check sanity
        Validate.notNull(namespace, "namespace");
        Validate.notEmpty(xmlSchemaSnippet, "xmlSchemaSnippet");
        Validate.isTrue(!namespace2SchemaSnippetMap.containsKey(namespace),
                "Cannot overwrite namespace [" + namespace + "]. Known namespaces: "
                        + namespace2SchemaSnippetMap.keySet()
                        .stream()
                        .reduce((l, r) -> l + ", " + r)
                        .orElse("<none>"));

        // Add the mapping.
        namespace2SchemaSnippetMap.put(namespace, xmlSchemaSnippet);
    }

    /**
     * Retrieves an LSInput which will respond with the xmlSchemaSnippet for the given namespaceURI
     * [if it is mapped within the namespace2SchemaSnippetMap] or {@code null} otherwise.
     */
    @Override
    public LSInput resolveResource(final String type,
                                   final String namespaceURI,
                                   final String publicId,
                                   final String systemId,
                                   final String baseURI) {

        // If the namespace is cached, return the cached value.
        // Otherwise, return null.
        return new ConstantValueLSInput(namespace2SchemaSnippetMap.get(namespaceURI));
    }

    //
    // Private helpers
    //

    /**
     * Dummy implementation of the LSInput interface, always returning the xmlSchema
     * value supplied at construction time.
     */
    class ConstantValueLSInput implements LSInput {

        // Internal state
        private String xmlSchema;

        ConstantValueLSInput(final String xmlSchema) {
            this.xmlSchema = xmlSchema;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getStringData() {
            return xmlSchema;
        }

        //
        // The methods below here are required by
        // the LSInput interface, but not used.
        //

        /**
         * {@inheritDoc}
         */
        @Override
        public Reader getCharacterStream() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCharacterStream(final Reader characterStream) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getByteStream() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setByteStream(final InputStream byteStream) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setStringData(final String stringData) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSystemId() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSystemId(final String systemId) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPublicId() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPublicId(final String publicId) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getBaseURI() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setBaseURI(final String baseURI) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getEncoding() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEncoding(final String encoding) {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean getCertifiedText() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCertifiedText(final boolean certifiedText) {
            // Do nothing
        }
    }
}
