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

/**
 * Convenience enum holding a set of well-known XML binding namespaces
 * and corresponding URLs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public enum WellKnownNamespace {

    /**
     * The URI of the XSD schema instance.
     */
    XSI(XmlBinder.XSD_INSTANCE_NAMESPACE, "xsi"),

    /**
     * The URI of the XSD schema.
     */
    XS(XmlBinder.XSD_NAMESPACE, "xs"),

    /**
     * The URI of Models, created from the Nazgul core framework principles.
     */
    CORE(XmlBinder.CORE_NAMESPACE, "core");

    // Internal state
    private String nameSpaceUrl;
    private String xsdPrefix;

    /**
     * Creates a new WellKnownNamespace with the provided URL and XSD prefix.
     *
     * @param nameSpaceUrl The URL corresponding to this well-known namespace.
     * @param xsdPrefix    The XSD prefix to be used for the given namespace.
     */
    WellKnownNamespace(final String nameSpaceUrl, final String xsdPrefix) {
        this.nameSpaceUrl = nameSpaceUrl;
        this.xsdPrefix = xsdPrefix;
    }

    /**
     * @return The URL corresponding to this well-known namespace.
     */
    public String getNameSpaceUrl() {
        return nameSpaceUrl;
    }

    /**
     * @return The prefix to be used within XSDs for the provided namespace.
     */
    public String getXsdPrefix() {
        return xsdPrefix;
    }
}
