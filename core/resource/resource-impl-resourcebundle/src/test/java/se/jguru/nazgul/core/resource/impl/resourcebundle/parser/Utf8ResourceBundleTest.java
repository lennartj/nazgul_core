/*
 * #%L
 * Nazgul Project: nazgul-core-resource-impl-resourcebundle
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

package se.jguru.nazgul.core.resource.impl.resourcebundle.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class Utf8ResourceBundleTest {

    @Test
    public void validateGettingKeysEnumeration() {

        // Assemble
        final ResourceBundle bundle = Utf8ResourceBundle.getBundle("test/resources/utf8chars", new Locale("sv"));
        final List<String> bundleKeys = new ArrayList<String>();

        // Act
        final Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            bundleKeys.add(keys.nextElement());
        }

        // Assert
        Assert.assertTrue(bundleKeys.contains("nonAsciiLetters"));
        Assert.assertTrue(bundleKeys.contains("emptyValue"));
    }
}
