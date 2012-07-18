/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
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
