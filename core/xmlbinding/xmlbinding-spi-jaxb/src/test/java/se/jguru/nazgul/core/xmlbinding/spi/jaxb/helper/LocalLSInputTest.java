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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalLSInputTest {

    // Shared state
    private static final String type = "testType";
    private static final String namespaceURI = "testNamespaceURI";
    private static final String publicId = "testPublicId";
    private static final String systemId = "testSystemId";
    private static final String baseURI = "testBaseURI";
    private LocalLSInput unitUnderTest;

    @Before
    public void setupLocalLSInput() {

        try {
            Field cache = LocalLSInput.class.getDeclaredField("namespaceToSchemaURI");
            cache.setAccessible(true);

            Map<String, String> ns2UriMap = (Map<String, String>) cache.get(unitUnderTest);
            ns2UriMap.clear();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not clear nsUriMap.", e);
        }

        unitUnderTest = new LocalLSInput(type, namespaceURI, publicId, systemId, baseURI);
        unitUnderTest.setBaseURI(baseURI);
        unitUnderTest.setPublicId(publicId);
        unitUnderTest.setSystemId(systemId);
    }

    @Test
    public void validateGettingDefaultValues() {

        // Act
        final String systemId = unitUnderTest.getSystemId();
        final String baseURI = unitUnderTest.getBaseURI();
        final String encoding = unitUnderTest.getEncoding();
        final String publicId = unitUnderTest.getPublicId();

        // Assert
        Assert.assertEquals(LocalLSInputTest.this.systemId, systemId);
        Assert.assertEquals(LocalLSInputTest.this.baseURI, baseURI);
        Assert.assertEquals("UTF-8", encoding);
        Assert.assertEquals(LocalLSInputTest.this.publicId, publicId);
        Assert.assertTrue(unitUnderTest.toString().length() > 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnSettingCharacterStream() {

        // Act & Assert
        unitUnderTest.setCharacterStream(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnSettingStringData() {

        // Act & Assert
        unitUnderTest.setStringData(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnSettingByteStream() {

        // Act & Assert
        unitUnderTest.setByteStream(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnSettingCertifiedText() {

        // Act & Assert
        unitUnderTest.setCertifiedText(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateExceptionOnGettingCertifiedText() {

        // Act & Assert
        unitUnderTest.getCertifiedText();
    }

    @Test
    public void validateGettingSchemaData() {

        // Assemble
        final String schema = "testSchema";

        // Act
        unitUnderTest.addSchema(namespaceURI, schema);
        final String result1 = unitUnderTest.getStringData();
        final Reader result2 = unitUnderTest.getCharacterStream();
        final InputStream result3 = unitUnderTest.getByteStream();

        // Assert
        Assert.assertEquals(schema, result1);
        Assert.assertNotNull(result2);
        Assert.assertNotNull(result3);

        Assert.assertEquals(schema, readFully(result2).trim());
        Assert.assertEquals(schema, readFully(new InputStreamReader(result3)).trim());
    }

    @Test
    public void validateGettingSchemaDataForNonRegisteredNamespace() {

        // Assemble
        final String schema = "testSchema";
        unitUnderTest = new LocalLSInput("type2", "namespaceURI2", "publicId2", "systemId2", "baseURI2");

        // Act
        unitUnderTest.addSchema(namespaceURI, schema);

        // Assert
        Assert.assertNull(unitUnderTest.getStringData());
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionSettingNullEncoding() {

        // Act & Assert
        unitUnderTest.setEncoding(null);
    }

    //
    // Private helpers
    //

    private String readFully(final Reader reader) {

        final BufferedReader tmp = new BufferedReader(reader);
        final StringBuilder toReturn = new StringBuilder();

        try {
            for (String line = tmp.readLine(); line != null; line = tmp.readLine()) {
                toReturn.append(line).append('\n');
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Problem reading data from Reader", e);
        }

        // All done.
        return toReturn.toString();
    }
}
