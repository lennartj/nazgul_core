/*
 * #%L
 * Nazgul Project: nazgul-core-resource-impl-resourcebundle
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

package se.jguru.nazgul.core.resource.impl.resourcebundle;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.core.resource.api.WellKnownLocale;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ResourceBundleLocalResourcesTest {

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullResourceBundleInConstruction() {

        // Act & Assert
        new ResourceBundleLocalResources(null);
    }

    @Test(expected = MissingResourceException.class)
    public void validateExceptionOnNonexistingResourceBundleBaseName() {

        // Act & Assert
        new ResourceBundleLocalResources("nonexistent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnNonexistentResourceBundleBaseNameWithProvidedResourceBundle() throws IOException {

        // Assemble
        final String propertyDefinition = "foo=bar\nbaz=gnat";
        final PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new StringReader(propertyDefinition));

        // Act & Assert
        new ResourceBundleLocalResources(resourceBundle, "nonExistent", Locale.getDefault());
    }

    /*
    @Test
    public void validateGettingNonStringResources() {

        // Assemble
        final DateTime then = new DateTime(2012, 3, 28, 10, 15, 5);
        final DateTime fallback = new DateTime(2012, 2, 27, 9, 14, 4);
        final String[] anArray = {"Smooth", "And", "Quick"};
        final ResourceBundle resourceBundle = new ListResourceBundle() {

            /**
             * Returns an array in which each item is a pair of objects in an
             * <code>Object</code> array. The first element of each pair is
             * the key, which must be a <code>String</code>, and the second
             * element is the value associated with that key.  See the class
             * description for details.
             *
             * @return an array of an <code>Object</code> array representing a
             * key-value pair.
             * /
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                        new Object[]{"aDate", then},
                        new Object[]{"aString", "Smooth"},
                        new Object[]{"aStringArray", anArray}
                };
            }
        };

        final DebugLocalResources unitUnderTest = new DebugLocalResources(
                resourceBundle, "notUsed", Locale.getDefault());

        // Act
        final DateTime result1 = unitUnderTest.getLocalized("aDate", fallback);
        final String result2 = unitUnderTest.getLocalized("unheardOf", "fallback");
        final String[] result3 = unitUnderTest.getLocalized("aStringArray", null);

        // Assert
        Assert.assertEquals(then, result1);
        Assert.assertEquals("fallback", result2);
        Assert.assertArrayEquals(anArray, result3);
    }
    */

    @Test
    public void validateLocalizedLookup() {

        // Assemble
        final String fooKey = "foo";
        final String bazKey = "baz";
        final Locale defaultLocale = Locale.ENGLISH;
        final Locale norwegian = WellKnownLocale.NORWEGIAN.getLocale();
        final Locale italian = Locale.ITALIAN;
        final ResourceBundleLocalResources unitUnderTest
                = new ResourceBundleLocalResources("test/resources/keyvalues", defaultLocale);

        // Act
        final String result1 = unitUnderTest.getLocalized(fooKey, "fooFallback");
        final String result2 = unitUnderTest.getLocalized(fooKey, "fooFallback", WellKnownLocale.SWEDISH.getLocale());
        final String result3 = unitUnderTest.getLocalized(fooKey, "fooFallback", norwegian);
        final String result4 = unitUnderTest.getLocalized(fooKey, "fooFallback", italian);

        final String result5 = unitUnderTest.getLocalized(bazKey, "bazFallback");
        final String result6 = unitUnderTest.getLocalized(bazKey, "bazFallback", WellKnownLocale.SWEDISH.getLocale());
        final String result7 = unitUnderTest.getLocalized(bazKey, "bazFallback", norwegian);

        final String result8 = unitUnderTest.getLocalized("noKey", "noKeyFallback", norwegian);
        final String result9 = unitUnderTest.getLocalized("noKey", "noKeyFallback", italian);

        // Assert
        Assert.assertEquals("bar_en", result1);
        Assert.assertEquals("bar_sv", result2);
        Assert.assertEquals("Morrn Da!", result3);
        Assert.assertEquals("bar_en", result4);

        Assert.assertEquals("gnat_en", result5);
        Assert.assertEquals("gnat_sv", result6);
        Assert.assertEquals("Hei Gutter!", result7);

        Assert.assertEquals("noKeyFallback", result8);
        Assert.assertEquals("noKeyFallback", result9);
    }

    @Test
    public void validateSwitchingDefaultLocales() {

        // Assemble
        final String fooKey = "foo";
        final Locale english = Locale.ENGLISH;
        final Locale norwegian = new Locale("no", "no");

        final ResourceBundleLocalResources unitUnderTest = new ResourceBundleLocalResources("test/resources/keyvalues", english);

        // Act
        final String englishFoo = unitUnderTest.getLocalized(fooKey, "fooDefault");
        unitUnderTest.setDefaultLocale(WellKnownLocale.SWEDISH.getLocale());
        final String swedishFoo = unitUnderTest.getLocalized(fooKey, "fooDefault");
        unitUnderTest.setDefaultLocale(norwegian);
        final String norwegianFoo = unitUnderTest.getLocalized(fooKey, "fooDefault");
        unitUnderTest.setDefaultLocale(english);
        final String anotherEnglishFoo = unitUnderTest.getLocalized(fooKey, "fooDefault");

        // Assert
        Assert.assertEquals("bar_en", englishFoo);
        Assert.assertEquals("bar_sv", swedishFoo);
        Assert.assertEquals("Morrn Da!", norwegianFoo);
        Assert.assertEquals("bar_en", anotherEnglishFoo);
    }

    @Test
    public void validateTokenization() {

        // Assemble
        final String barKey = "bar";
        final String defaultValue = "barDefault";
        final Locale fallback = Locale.ITALIAN;
        final Locale swedish = WellKnownLocale.SWEDISH.getLocale();

        final ResourceBundleLocalResources unitUnderTest = new ResourceBundleLocalResources(
                "test/tokenizedresources/keyvalues", fallback);

        // Act & Assert
        Assert.assertEquals("a ${nice} bar", unitUnderTest.getLocalized(barKey, defaultValue));
        Assert.assertEquals("a grand bar", unitUnderTest.getLocalized(barKey, defaultValue, "nice=grand"));
        Assert.assertEquals("en ${nice} bar", unitUnderTest.getLocalized(barKey, defaultValue, swedish));
        Assert.assertEquals("en kul bar", unitUnderTest.getLocalized(barKey, defaultValue, swedish, "nice=kul"));

        unitUnderTest.setDefaultLocale(WellKnownLocale.SWEDISH.getLocale());
        Assert.assertEquals("en ${nice} bar", unitUnderTest.getLocalized(barKey, defaultValue));
    }

    @Test
    public void validateNonAsciiCharacters() {

        // Assemble
        final String unicodeValue = "\u00E5\u00E4\u00F6\u00C5\u00C4\u00D6";
        final String stringValue = "åäöÅÄÖ";
        final Locale swedish = WellKnownLocale.SWEDISH.getLocale();

        final ResourceBundleLocalResources unitUnderTest = new ResourceBundleLocalResources("test/resources/utf8chars", Locale.US);

        // Act & Assert
        Assert.assertEquals(unicodeValue, unitUnderTest.getLocalized("nonAsciiLetters", null, swedish));
        Assert.assertEquals(stringValue, unitUnderTest.getLocalized("nonAsciiLetters", null, swedish));
        Assert.assertEquals("", unitUnderTest.getLocalized("emptyValue", null, swedish));
        Assert.assertNull(unitUnderTest.getLocalized("nonexistentKey", null, swedish));
    }
}
