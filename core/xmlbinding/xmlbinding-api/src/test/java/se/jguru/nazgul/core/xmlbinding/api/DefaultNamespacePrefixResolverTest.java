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

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DefaultNamespacePrefixResolverTest {

    // Shared state
    private WellKnownNamespace[] allNamespaces = WellKnownNamespace.values();

    @Test
    public void validateDefaultPrefixSetup() {

        // Assemble
        final DefaultNamespacePrefixResolver unitUnderTest = new DefaultNamespacePrefixResolver();

        // Act
        final Set<String> registeredNamespaceURIs = unitUnderTest.getRegisteredNamespaceURIs();
        final Set<String> registeredPrefixes = unitUnderTest.getRegisteredPrefixes();

        // Assert
        Assert.assertEquals(allNamespaces.length, registeredNamespaceURIs.size());
        Assert.assertEquals(allNamespaces.length, registeredPrefixes.size());

        for (WellKnownNamespace current : allNamespaces) {

            final String currentNamespace = current.getNameSpaceUrl();
            final String currentPrefix = current.getXsdPrefix();

            Assert.assertEquals(currentNamespace, unitUnderTest.getNamespaceUri(currentPrefix));
            Assert.assertEquals(currentPrefix, unitUnderTest.getXmlPrefix(currentNamespace));

            Assert.assertTrue(unitUnderTest.getRegisteredNamespaceURIs().contains(currentNamespace));
            Assert.assertTrue(unitUnderTest.getRegisteredPrefixes().contains(currentPrefix));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingNamespaceTwice() {

        // Assemble
        final DefaultNamespacePrefixResolver unitUnderTest = new DefaultNamespacePrefixResolver();
        final String namespaceURL = "http://some/namespace/url";

        // Act & Assert
        unitUnderTest.put(namespaceURL, "someValue");
        unitUnderTest.put(namespaceURL, "anotherValue");
    }

    @Test
    public void validateCompoundUri2PrefixAddition() {

        // Assemble
        final DefaultNamespacePrefixResolver unitUnderTest = new DefaultNamespacePrefixResolver();
        final Map<String, String> namespaceMap = new TreeMap<String, String>();
        namespaceMap.put("http://some/namespace/url", "prefix1");
        namespaceMap.put("http://some/other/namespace/url", "prefix2");

        // Act
        unitUnderTest.putAll(namespaceMap);

        // Assert
        final int expectedLength = namespaceMap.size() + WellKnownNamespace.values().length;
        Assert.assertEquals(expectedLength, unitUnderTest.getRegisteredNamespaceURIs().size());
        Assert.assertEquals(expectedLength, unitUnderTest.getRegisteredPrefixes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingIncorrectlyMappedNamespace() {

        // Assemble
        final DefaultNamespacePrefixResolver unitUnderTest = new DefaultNamespacePrefixResolver();
        final String prefix = "fooBar";

        final Map<String, String> namespaceMap = new TreeMap<String, String>();
        namespaceMap.put("http://some/namespace/url", prefix);
        namespaceMap.put("http://some/other/namespace/url", prefix);

        // Act & Assert
        unitUnderTest.putAll(namespaceMap);
    }

    //
    // Private helpers
    //

    private String getWellKnownXmlNamespace(final String xmlPrefix) {

        for (WellKnownNamespace current : allNamespaces) {
            if (current.getXsdPrefix().equals(xmlPrefix)) {
                return current.getNameSpaceUrl();
            }
        }

        throw new IllegalArgumentException("XmlPrefix [" + xmlPrefix + "] not found.");
    }

    private String getWellKnownXmlPrefix(final String xmlNamespace) {
        return WellKnownNamespace.valueOf(xmlNamespace).getNameSpaceUrl();
    }
}
