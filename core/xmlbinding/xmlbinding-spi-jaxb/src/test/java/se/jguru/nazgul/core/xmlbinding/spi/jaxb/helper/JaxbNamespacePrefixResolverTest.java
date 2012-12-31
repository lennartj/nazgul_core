/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.api.WellKnownNamespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JaxbNamespacePrefixResolverTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullNamespacePrefixResolver() {

        // Act & Assert
        new JaxbNamespacePrefixResolver(null);
    }

    @Test
    public void validateDefaultValues() {

        // Assemble
        final WellKnownNamespace[] namespaces = WellKnownNamespace.values();
        final JaxbNamespacePrefixResolver unitUnderTest = new JaxbNamespacePrefixResolver();

        // Act
        final String[] preDeclaredNamespaceUris = unitUnderTest.getPreDeclaredNamespaceUris();
        final List<String> preDeclaredNamespaceUriList = new ArrayList<String>();
        Collections.addAll(preDeclaredNamespaceUriList, preDeclaredNamespaceUris);

        final Set<String> registeredNamespaceURIs = unitUnderTest.getRegisteredNamespaceURIs();
        final Set<String> registeredPrefixes = unitUnderTest.getRegisteredPrefixes();

        // Assert
        Assert.assertEquals(namespaces.length, preDeclaredNamespaceUris.length);
        for (WellKnownNamespace current : namespaces) {
            Assert.assertTrue(registeredNamespaceURIs.contains(current.getNameSpaceUrl()));
            Assert.assertTrue(preDeclaredNamespaceUriList.contains(current.getNameSpaceUrl()));
            Assert.assertTrue(registeredPrefixes.contains(current.getXsdPrefix()));
        }
    }

    @Test
    public void validateAcquiringPreferredPrefix() {

        // Assemble
        final String unknown = "http://unknown";
        final String suggested = "suggested";
        final JaxbNamespacePrefixResolver unitUnderTest = new JaxbNamespacePrefixResolver();

        // Act
        final String corePrefix = unitUnderTest.getPreferredPrefix(
                WellKnownNamespace.CORE.getNameSpaceUrl(), suggested, true);
        final String unknownNamespaceRequirePrefix = unitUnderTest.getPreferredPrefix(unknown, suggested, true);
        final String unknownNamespaceDontRequirePrefix = unitUnderTest.getPreferredPrefix(unknown, suggested, false);

        // Assert
        Assert.assertEquals(WellKnownNamespace.CORE.getXsdPrefix(), corePrefix);
        Assert.assertEquals(suggested, unknownNamespaceRequirePrefix);
        Assert.assertEquals(JaxbNamespacePrefixResolver.EMPTY_NAMESPACE, unknownNamespaceDontRequirePrefix);
    }

    @Test
    public void validateAddingUriPrefixPair() {

        // Assemble
        final String namespaceUri = "http://foo/bar";
        final String prefix = "foobar";
        final JaxbNamespacePrefixResolver unitUnderTest = new JaxbNamespacePrefixResolver();

        // Act
        unitUnderTest.put(namespaceUri, prefix);

        // Assert
        Assert.assertEquals(prefix, unitUnderTest.getXmlPrefix(namespaceUri));
        Assert.assertEquals(namespaceUri, unitUnderTest.getNamespaceUri(prefix));
        Assert.assertEquals(prefix, unitUnderTest.getPreferredPrefix(namespaceUri, "irrelevant", true));
    }

    @Test
    public void validateAddingUriPrefixMap() {

        // Assemble
        final String namespaceUri = "http://foo/bar";
        final String prefix = "foobar";
        final Map<String, String> map = new TreeMap<String, String>();
        map.put(namespaceUri, prefix);

        final JaxbNamespacePrefixResolver unitUnderTest = new JaxbNamespacePrefixResolver();

        // Act
        unitUnderTest.putAll(map);

        // Assert
        Assert.assertEquals(prefix, unitUnderTest.getXmlPrefix(namespaceUri));
        Assert.assertEquals(namespaceUri, unitUnderTest.getNamespaceUri(prefix));
    }
}
