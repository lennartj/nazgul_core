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

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Wrapper utility lass which permits using a set of .property files encoded
 * in UTF-8 instead of the ISO-8859-1 which is the only supported encoding within
 * the ResourceBundle class.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class Utf8ResourceBundle {

    // Constants
    private static final String ISO_ENCODING = "ISO-8859-1";
    private static final String UTF_ENCODING = "UTF-8";

    /**
     * Factory method that creates a ResourceBundle for the provided baseName, and
     * wraps it within an ISO88591 --> UTF-8 transcoding shell in case the acquired
     * ResourceBundle is a PropertyResourceBundle.
     * <p/>
     * Due to the insanely poor coding of the ResourceBundle class, where the static
     * getBundle(String) method is final, we have to delegate the wrapping to a
     * private, inner class.
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name.
     * @param locale   the locale for which a resource bundle is desired.
     * @return Unicode friendly resource bundle.
     * @see java.util.ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getBundle(final String baseName, final Locale locale) {

        final ResourceBundle originalBundle = ResourceBundle.getBundle(baseName, locale);
        boolean wrappable = originalBundle instanceof PropertyResourceBundle;

        // Return the original bundle itself unless it is wrappable.
        return wrappable ? new Utf8PropertyResourceBundle((PropertyResourceBundle) originalBundle) : originalBundle;
    }

    //
    // Private helpers
    //

    private static final class Utf8PropertyResourceBundle extends ResourceBundle {

        // Internal state
        private final PropertyResourceBundle innerBundle;

        private Utf8PropertyResourceBundle(final PropertyResourceBundle innerBundle) {
            this.innerBundle = innerBundle;
        }

        @Override
        public Enumeration<String> getKeys() {
            return innerBundle.getKeys();
        }

        @Override
        protected Object handleGetObject(final String key) {

            String value = innerBundle.getString(key);
            if (value == null) {
                return null;
            }

            try {
                // Do a no-brained Transcoding from the PropertyResourceBundle hard-coded encoding to UTF-8.
                return new String(value.getBytes(ISO_ENCODING), UTF_ENCODING);
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException("Could not transcode ISO-8859-1 to UTF-8", e);
            }
        }
    }
}
